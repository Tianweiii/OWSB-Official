package service;

import models.Datas.PurchaseRequisition;
import models.Datas.PurchaseRequisitionItem;
import models.Utils.QueryBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurchaseRequisitionService extends Service<PurchaseRequisition> {
    private static final Logger LOGGER = Logger.getLogger(PurchaseRequisitionService.class.getName());
    private static final String DATA_FILE = "db/PurchaseRequisition.txt";
    private static final String ITEM_DATA_FILE = "db/PurchaseRequisitionItem.txt";

    /**
     * Fetches all purchase requisitions from the data source.
     *
     * @return List of PurchaseRequisition, or empty list if an error occurs.
     */
    @Override
    public List<PurchaseRequisition> getAll() {
        try {
            QueryBuilder<PurchaseRequisition> qb = new QueryBuilder<>(PurchaseRequisition.class);
            return qb.select().from(DATA_FILE).getAsObjects();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading purchase requisitions.", e);
            return Collections.emptyList();
        }
    }

    /**
     * Creates a new purchase requisition.
     *
     * @param pr the PurchaseRequisition to create
     * @return true if creation succeeds; false otherwise
     */
    public boolean create(PurchaseRequisition pr) {
        try {
            QueryBuilder<PurchaseRequisition> qb = new QueryBuilder<>(PurchaseRequisition.class);
            return qb.target(DATA_FILE)
                    .values(new String[]{
                            pr.getReceivedByDate(),
                            Optional.ofNullable(pr.getCreatedDate()).orElse(""),
                            pr.getUserID(),
                            pr.getPRStatus()
                    })
                    .create();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create purchase requisition: " + pr, e);
            return false;
        }
    }

    /**
     * Updates an existing purchase requisition.
     *
     * @param id the ID of the requisition to update
     * @param pr the PurchaseRequisition containing updated data
     * @return true if update succeeds; false otherwise
     */
    public boolean update(String id, PurchaseRequisition pr) {
        try {
            QueryBuilder<PurchaseRequisition> qb = new QueryBuilder<>(PurchaseRequisition.class);
            return qb.target(DATA_FILE)
                    .update(id, new String[]{
                            pr.getReceivedByDate(),
                            Optional.ofNullable(pr.getCreatedDate()).orElse(""),
                            pr.getUserID(),
                            pr.getPRStatus()
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update purchase requisition [id=" + id + "]", e);
            return false;
        }
    }

    /**
     * Deletes a purchase requisition by its ID.
     *
     * @param id the ID of the requisition to delete
     * @return true if deletion succeeds; false otherwise
     */
    public boolean delete(String id) {
        try {
            QueryBuilder<PurchaseRequisition> qb = new QueryBuilder<>(PurchaseRequisition.class);
            return qb.target(DATA_FILE).delete(String.valueOf(id));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete purchase requisition [id=" + id + "]", e);
            return false;
        }
    }

    /**
     * Updates the status of a purchase requisition (e.g., Pending, Rejected, Approved).
     *
     * @param id the ID of the requisition
     * @param status the status to set (Pending, Rejected, Approved)
     * @return true if the status update is successful; false otherwise
     */
    public boolean updateStatus(String id, String status) {
        try {
            if (!status.equals("Pending") && !status.equals("Rejected") && !status.equals("Approved")) {
                LOGGER.log(Level.SEVERE, "Invalid status for requisition [id=" + id + "]: " + status);
                return false;
            }

            QueryBuilder<PurchaseRequisition> qb = new QueryBuilder<>(PurchaseRequisition.class);
            List<PurchaseRequisition> requisitions = qb.select().from(DATA_FILE).getAsObjects();
            PurchaseRequisition pr = requisitions.stream()
                    .filter(r -> r.getPrRequisitionID().equals(String.valueOf(id)))
                    .findFirst()
                    .orElse(null);

            if (pr == null) {
                LOGGER.log(Level.SEVERE, "Purchase requisition not found for id: " + id);
                return false;
            }

            pr.setPRStatus(status);
            return qb.target(DATA_FILE)
                    .update(String.valueOf(id), new String[]{
                            pr.getReceivedByDate(),
                            Optional.ofNullable(pr.getCreatedDate()).orElse(""),
                            pr.getUserID(),
                            pr.getPRStatus()
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update status for requisition [id=" + id + "]", e);
            return false;
        }
    }

    /**
     * Adds an item to a purchase requisition.
     *
     * @param prItem the PurchaseRequisitionItem to add
     * @return true if addition succeeds; false otherwise
     */
    public boolean addItemToRequisition(PurchaseRequisitionItem prItem) {
        try {
            QueryBuilder<PurchaseRequisitionItem> qb = new QueryBuilder<>(PurchaseRequisitionItem.class);
            return qb.target(ITEM_DATA_FILE).values(new String[]{
                    String.valueOf(prItem.getPrRequisitionID()),
                    String.valueOf(prItem.getItemID()),
                    String.valueOf(prItem.getQuantity())
            }).create();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add item to requisition: " + prItem, e);
            return false;
        }
    }

    /**
     * Updates the quantity of an item in a purchase requisition.
     *
     * @param prItemId the ID of the item in the requisition
     * @param quantity the new quantity
     * @return true if update succeeds; false otherwise
     */
    public boolean updateItemQuantity(String prItemId, int quantity) {
        try {
            QueryBuilder<PurchaseRequisitionItem> qb = new QueryBuilder<>(PurchaseRequisitionItem.class);
            return qb.target(ITEM_DATA_FILE)
                    .update(String.valueOf(prItemId), new String[]{String.valueOf(quantity)});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update item quantity [id=" + prItemId + "]", e);
            return false;
        }
    }
}
