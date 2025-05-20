package service;

import models.Datas.Transaction;
import models.Datas.Item;
import models.Datas.DailySalesHistory;
import models.Utils.QueryBuilder;

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
                    "" :
                    dailySalesHistories.get(0).getDailySalesHistoryID();

            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            return qb
                    .select()
                    .from(TRANS_FILE)
//                    .joins(DailySalesHistory.class, "dailySalesHistoryID")
                    .where("dailySalesHistoryID", "=", histId)
                    .getAsObjects();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load transactions for " + ds, e);
            return Collections.emptyList();
        }
    }

    /** Record a new sale locally. */
    public void recordSale(Item item, int quantity, LocalDate date) {
        System.out.println(date);
        String histId = getOrCreateHistory(date);
        try {
            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            qb
                .target(TRANS_FILE)
                .values(new String[]{
                        histId,
                        String.valueOf(quantity),
                        item.getItemID(),
                        "S1"  // salesID (unused)
                })
                .create();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to record sale", e);
        }
    }

    /** Update an existing transaction's quantity. */
    public void updateTransaction(Transaction transaction, Item item, int newQty) {
        try {
            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            qb
                .target(TRANS_FILE)
                .update(transaction.getTransactionID(), new String[]{
                        transaction.getDailySalesHistoryID(),
                        String.valueOf(newQty),
                        item.getItemID(),
                        transaction.getSalesID()
                    });
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
     * Export a date's transactions to a CSV file.
     * 
     * Basic CSV export without creating inventory update requests
     *
     * @return The full path to the exported CSV file
     */
    public String exportCsv(LocalDate date) {
        String ds = date.format(ISO);
        List<Transaction> txs = getTransactionsFor(date);
        String filename = "sales_" + ds + ".csv";
        try (FileWriter w = new FileWriter(filename)) {
            w.write("Item,Qty,Price,Subtotal\n");
            for (var t : txs) {
                w.write(String.format(
                        "%s,%d,%.2f,%.2f\n",
                        t.getItemName(),
                        t.getSoldQuantity(),
                        t.getUnitPrice(),
                        t.getSubtotal()
                ));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to export CSV", e);
        }
        return filename;
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
            // Write CSV header
            writer.write("Report Date: " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "\n");
            writer.write("Generated: " + LocalDateTime.now().format(DATETIME_FORMAT) + "\n\n");
            writer.write("Item ID,Item Name,Quantity,Unit Price,Subtotal\n");
            
            // Write transaction data
            double totalAmount = 0;
            for (Transaction tx : txs) {
                writer.write(String.format("%s,%s,%d,%.2f,%.2f\n",
                        tx.getItemID(),
                        tx.getItemName(),
                        tx.getSoldQuantity(),
                        tx.getUnitPrice(),
                        tx.getSubtotal()));
                totalAmount += tx.getSubtotal();
                
                // Create inventory update request for each transaction
                createInventoryUpdateRequest(tx);
            }
            
            // Write summary
            writer.write("\nTotal Items: " + txs.size() + "\n");
            writer.write("Total Amount: $" + String.format("%.2f", totalAmount) + "\n");
            
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
    private void createInventoryUpdateRequest(Transaction transaction) {
        try {
            // Create timestamp for the request
            String timestamp = LocalDateTime.now().format(DATETIME_FORMAT);
            
            // Query to create inventory update request
            // Assuming the structure of InventoryUpdateRequest has:
            // - requestID (auto-generated)
            // - itemID
            // - quantity
            // - requestType (e.g., "SALES_DEDUCTION")
            // - status (e.g., "PENDING")
            // - createdAt
            // - transactionID (reference back to the sales transaction)
            
            // Check if the InventoryUpdateRequest.txt file exists, create if not
            Path invReqPath = Paths.get(INV_UPDATE_REQUEST_FILE);
            if (!Files.exists(invReqPath.getParent())) {
                Files.createDirectories(invReqPath.getParent());
            }
            if (!Files.exists(invReqPath)) {
                Files.createFile(invReqPath);
            }
            
            // Using a simplified approach here since we don't have the actual InventoryUpdateRequest model
            // In a real implementation, you'd use the appropriate QueryBuilder for this model
            try (FileWriter writer = new FileWriter(INV_UPDATE_REQUEST_FILE, true)) {
                String requestLine = String.format("%s,%s,%d,%s,%s,%s\n",
                        transaction.getItemID(),
                        transaction.getSoldQuantity(),
                        "SALES_DEDUCTION",
                        "PENDING",
                        timestamp,
                        transaction.getTransactionID());
                writer.write(requestLine);
            }
            
            LOG.info("Created inventory update request for transaction: " + transaction.getTransactionID());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create inventory update request", e);
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

    /**
     * Find or create a DailySalesHistory for the given date.
     * We first query by createdAt; if none, we insert one and then re-query.
     */
    private String getOrCreateHistory(LocalDate date) {
        String ds = date.format(ISO);
        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            List<DailySalesHistory> list = qb
                    .select()
                    .from(HIST_FILE)
                    .where("createdAt", "=", ds)
                    .getAsObjects();

            if (!list.isEmpty()) {
                return list.get(0).getDailySalesHistoryID();
            }

            // Insert new history record with default status "Pending"
            qb.target(HIST_FILE)
                    .values(new String[]{ ds, ds, "Pending" })
                    .create();

            // Re-query to fetch its ID
            List<DailySalesHistory> created = qb
                    .select()
                    .from(HIST_FILE)
                    .where("createdAt", "=", ds)
                    .getAsObjects();

            return created.isEmpty()
                    ? "" // fallback
                    : created.get(0).getDailySalesHistoryID();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get/create history for " + ds, e);
            return "";
        }
    }

    /** Sum all subtotals for a given date. */
    public double calculateTotalFor(LocalDate date) {
        return getTransactionsFor(date).stream()
                .mapToDouble(Transaction::getSubtotal)
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

    // Delete all transactions under one daily sales history ID
    public void deleteTransactionsByDailySalesHistoryID(String dailySalesHistoryID) {
        try {
            new QueryBuilder<>(Transaction.class)
                    .target(TRANS_FILE)
                    .where("dailySalesHistoryID", "=", dailySalesHistoryID);
//                    .deleteAllMatching();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to delete transactions by DailySalesHistoryID " + dailySalesHistoryID, e);
        }
    }

    // Delete the daily sales history record by ID
    public void deleteDailySalesHistory(String dailySalesHistoryID) {
        try {
            new QueryBuilder<>(DailySalesHistory.class)
                    .target(HIST_FILE)
                    .delete(dailySalesHistoryID);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to delete daily sales history " + dailySalesHistoryID, e);
        }
    }

//    public boolean createInventoryUpdateRequest(Transaction transaction) {
//        try {
//
//        } catch (Exception e) {
//            LOG.log(Level.SEVERE, "Failed to create inventory update request", e);
//            return false;
//        }
//        return true;
//    }
}
