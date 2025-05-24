package service;

import models.Datas.InventoryUpdateRequest;
import models.Datas.Transaction;
import models.Datas.Item;
import models.Datas.DailySalesHistory;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Daily Item Sales, purely file‐based.
 */
public class DailySalesService {
    private static final Logger LOG = Logger.getLogger(DailySalesService.class.getName());
    private static final String TRANS_FILE = "db/Transaction.txt";
    private static final String HIST_FILE = "db/DailySalesHistory.txt";
    private static final String INV_UPDATE_REQUEST_FILE = "db/InventoryUpdateRequest.txt";
    private static final String REPORTS_DIR = "reports";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Load all items from the local DB. */
    public List<Item> getAllItems() {
        try {
            QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
            return qb
                    .select()
                    .from("db/Item.txt")
                    .getAsObjects();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load items", e);
            return Collections.emptyList();
        }
    }

    /** Get all transactions for a given date. */
    public List<Transaction> getTransactionsFor(LocalDate date) {
        String ds = date.format(ISO);
        try {
            QueryBuilder<DailySalesHistory> dshQB = new QueryBuilder<>(DailySalesHistory.class);
            ArrayList<DailySalesHistory> dailySalesHistories = dshQB
                    .select()
                    .from(HIST_FILE)
                    .where("createdAt", "=", ds)
                    .getAsObjects();

            String histId = dailySalesHistories.isEmpty() ?
                    "none" :
                    dailySalesHistories.get(0).getDailySalesHistoryID();

            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            return qb
                    .select()
                    .from(TRANS_FILE)
                    .where("dailySalesHistoryID", "=", histId)
                    .getAsObjects();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load transactions for " + ds, e);
            return Collections.emptyList();
        }
    }

    /** Record a new sale locally. */
    public void recordSale(Item item, int quantity, LocalDate date) {
        if (date == null) {
            LOG.log(Level.SEVERE, "Date cannot be null");
            return;
        }
        
        String histId = getOrCreateHistory(date);
        if (histId.isEmpty()) {
            LOG.log(Level.SEVERE, "Failed to get/create history for date: " + date);
            return;
        }
        
        try {
            // Apply 15% markup to the unit price for profit
            double originalPrice = item.getUnitPrice();
            double markedUpPrice = originalPrice * 1.15;
            
            // Store the marked up price in the item for this transaction
            item.setUnitPrice(markedUpPrice);
            
            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            qb.target(TRANS_FILE)
                .values(new String[]{
                        histId,
                        String.valueOf(quantity),
                        item.getItemID(),
                        "S1"  // salesID (unused)
                })
                .create();
              
            // Reset to original price after transaction is created
            item.setUnitPrice(originalPrice);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to record sale for date: " + date, e);
        }
    }

    /** Update an existing transaction's quantity. */
    public void updateTransaction(Transaction transaction, Item item, int newQty) {
        try {
            // Apply 15% markup to the unit price for profit
            double originalPrice = item.getUnitPrice();
            double markedUpPrice = originalPrice * 1.15;
            
            // Store the marked up price in the item for this transaction
            item.setUnitPrice(markedUpPrice);
            
            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            qb
                .target(TRANS_FILE)
                .update(transaction.getTransactionID(), new String[]{
                        transaction.getDailySalesHistoryID(),
                        String.valueOf(newQty),
                        item.getItemID(),
                        transaction.getSalesID()
                    });
                    
            // Reset to original price after transaction is updated
            item.setUnitPrice(originalPrice);
            
            QueryBuilder<DailySalesHistory> dshQB = new QueryBuilder<>(DailySalesHistory.class);
            HashMap<String, String> map = new HashMap<>();
            map.put("updatedAt", LocalDate.now().format(ISO));
            dshQB
                .target(HIST_FILE)
                .update(transaction.getDailySalesHistoryID(), map);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to update transaction " + transaction.getTransactionID(), e);
        }
    }

    /** Delete a transaction by ID. */
    public void deleteTransaction(String txId) {
        try {
            new QueryBuilder<>(Transaction.class)
                    .target(TRANS_FILE)
                    .delete(txId);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to delete transaction " + txId, e);
        }
    }

    public void deleteAllTransactions(String histID, String[] transactionIDs) {
        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            qb.target(HIST_FILE).delete(histID);

            QueryBuilder<Transaction> qb2 = new QueryBuilder<>(Transaction.class);
            qb2
                .target(TRANS_FILE)
                .deleteMany(transactionIDs);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to delete all transactions", e);
        }
    }

    /**
     * Complete sales report - enhanced version that:
     * 1. Creates a more detailed CSV
     * 2. Creates inventory update requests
     * 3. Updates sales history status to "Completed"
     * 
     * @param date The date for which to complete the sales report
     * @return The full path to the exported report file
     */
    public String completeSalesReport(LocalDate date) {
        // Ensure reports directory exists
        try {
            Path reportsPath = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to create reports directory", e);
        }

        String ds = date.format(ISO);
        List<Transaction> txs = getTransactionsFor(date);

        if (txs.isEmpty()) {
            LOG.warning("No transactions found for date: " + ds);
            return null;
        }
        
        // Get the history ID for this date
        String historyId = txs.get(0).getDailySalesHistoryID();
        
        // Format report filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = REPORTS_DIR + File.separator + "sales_report_" + ds + "_" + timestamp + ".csv";
        
        try (FileWriter writer = new FileWriter(filename)) {
            // Write CSV header with professional formatting
            writer.write("\"OWSB - SALES REPORT\"\n");
            writer.write("\n");
            writer.write("\"Report Date:\",\"" + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "\"\n");
            writer.write("\"Generated On:\",\"" + LocalDateTime.now().format(DATETIME_FORMAT) + "\"\n");
            writer.write("\"Generated By:\",\"Sales Manager\"\n");
            writer.write("\"Report ID:\",\"SR-" + timestamp + "\"\n");
            writer.write("\n");
            
            // Column headers with proper formatting
            writer.write("\"Item ID\",\"Item Name\",\"Quantity\",\"Unit Price ($)\",\"Subtotal ($)\"\n");
            
            // Write transaction data
            double totalAmount = 0;
            int totalQuantity = 0;
            int uniqueItemCount = (int) txs.stream().map(Transaction::getItemID).distinct().count();
            
            for (Transaction tx : txs) {
                writer.write(String.format("\"%s\",\"%s\",%d,\"%.2f\",\"%.2f\"\n",
                        tx.getItemID(),
                        tx.getItemName(),
                        tx.getSoldQuantity(),
                        tx.getMarkedUpPrice(),
                        tx.getMarkedUpSubtotal()));
                totalAmount += tx.getMarkedUpSubtotal();
                totalQuantity += tx.getSoldQuantity();
                
                // Create inventory update request for each transaction
                createInventoryUpdateRequest(tx);
            }
            
            // Write summary section
            writer.write("\n");
            writer.write("\"SUMMARY\"\n");
            writer.write("\"Total Unique Items:\",\"" + uniqueItemCount + "\"\n");
            writer.write("\"Total Items Sold:\",\"" + totalQuantity + "\"\n");
            writer.write("\"Total Transactions:\",\"" + txs.size() + "\"\n");
            writer.write("\"Total Revenue ($):\",\"" + String.format("%.2f", totalAmount) + "\"\n");
            writer.write("\n");
            writer.write("\"REPORT NOTES\"\n");
            writer.write("\"This report was automatically generated by the OWSB Sales Management System.\"\n");
            writer.write("\"All sales transactions for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + " have been recorded and finalized.\"\n");
            writer.write("\"Inventory update requests have been created for these transactions.\"\n");
            
            // Update sales history status to "Completed"
            updateSalesHistoryStatus(historyId, "Completed");
            
            return filename;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to complete sales report", e);
            return null;
        }
    }

    /**
     * Create inventory update request for a transaction
     */
    private void createInventoryUpdateRequest(Transaction tx) {
        String userID = SessionManager.getInstance().getUserData().get("userID");
        try {
            QueryBuilder<InventoryUpdateRequest> qb = new QueryBuilder<>(InventoryUpdateRequest.class);
            boolean res = qb
                    .target(INV_UPDATE_REQUEST_FILE)
                    .values(new String[]{
                            tx.getItemID(),
                            userID,
                            String.valueOf(tx.getSoldQuantity()),
                            "Pending",
                    })
                    .create("REQ");
            if (!res) {
                LOG.log(Level.SEVERE, "Failed to create inventory update request for transaction " + tx.getTransactionID());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create inventory update requests", e);
        }
    }

    /**
     * Update the status of a sales history record
     */
    private void updateSalesHistoryStatus(String historyId, String status) {
        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            HashMap<String, String> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("updatedAt", LocalDate.now().format(ISO));
            
            qb.target(HIST_FILE)
              .update(historyId, updates);
            
            LOG.info("Updated sales history status to " + status + " for ID: " + historyId);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to update sales history status", e);
        }
    }

    public DailySalesHistory getDailySalesHistory(String dailySalesHistoryID) {
        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            ArrayList<DailySalesHistory> dailySalesHistories = qb
                    .select()
                    .from(HIST_FILE)
                    .where("dailySalesHistoryID", "=", dailySalesHistoryID)
                    .getAsObjects();
            return dailySalesHistories.isEmpty() ? null : dailySalesHistories.get(0);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get daily sales history", e);
            return null;
        }
    }

    /**
     * Get or create a DailySalesHistory for the given date.
     * We first query by createdAt; if none, we insert one and then re-query.
     */
    private String getOrCreateHistory(LocalDate date) {
        if (date == null) return "";
        
        String ds = date.format(ISO);
        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            
            // First try to find existing history
            List<DailySalesHistory> existing = qb
                    .select()
                    .from(HIST_FILE)
                    .where("createdAt", "=", ds)
                    .getAsObjects();

            if (!existing.isEmpty()) {
                return existing.get(0).getDailySalesHistoryID();
            }

            // Create new history if none exists
            boolean created = qb.target(HIST_FILE)
                    .values(new String[]{ ds, ds, "Pending" })
                    .create();

            if (!created) {
                LOG.log(Level.SEVERE, "Failed to create history record for " + ds);
                return "";
            }

            // Fetch the newly created history
            List<DailySalesHistory> newHistory = qb
                    .select()
                    .from(HIST_FILE)
                    .where("createdAt", "=", ds)
                    .getAsObjects();

            return newHistory.isEmpty() ? "" : newHistory.get(0).getDailySalesHistoryID();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get/create history for " + ds, e);
            return "";
        }
    }

    /** Sum all subtotals for a given date. */
    public double calculateTotalFor(LocalDate date) {
        return getTransactionsFor(date).stream()
                .mapToDouble(Transaction::getMarkedUpSubtotal)
                .sum();
    }

    /**
     * Get the status of sales history for a given date
     * 
     * @param date The date to check
     * @return The status (e.g., "Pending" or "Completed") or null if not found
     */
    public String getSalesHistoryStatus(LocalDate date) {
        String ds = date.format(ISO);
        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            List<DailySalesHistory> list = qb
                    .select()
                    .from(HIST_FILE)
                    .where("createdAt", "=", ds)
                    .getAsObjects();
            if (!list.isEmpty()) {
                return list.get(0).getStatus();
            }
            return null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get sales history status for " + ds, e);
            return null;
        }
    }
}
