package service;

import models.Datas.Transaction;
import models.Datas.Item;
import models.Datas.DailySalesHistory;
import models.Utils.QueryBuilder;

import java.io.FileWriter;
import java.time.LocalDate;
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
    private static final Logger LOG       = Logger.getLogger(DailySalesService.class.getName());
    private static final String TRANS_FILE = "db/Transaction.txt";
    private static final String HIST_FILE  = "db/DailySalesHistory.txt";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;

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
        String histId = getOrCreateHistory(date);
        System.out.println(item.getItemID());
        System.out.println(histId);
        try {
            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            qb
                .target(TRANS_FILE)
                .values(new String[]{
                        histId,
                        String.valueOf(quantity),
                        item.getItemID(),
                        "S1"                           // salesID (unused)
                })
                .create();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to record sale", e);
        }
    }

    /** Update an existing transaction’s quantity. */
    public void updateTransaction(Transaction transaction, Item item, int newQty) {
        try {
            System.out.println(transaction.getTransactionID() + item.getItemName() + newQty);
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
     * Export a date’s transactions to a CSV file.
     *
     * @return
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
//                        t.getUnitPrice(),
                        t.getSubtotal()
                ));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to export CSV", e);
        }
        return ds;
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

            // Insert new history record
            qb.target(HIST_FILE)
                    .values(new String[]{ ds, ds })
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

}
