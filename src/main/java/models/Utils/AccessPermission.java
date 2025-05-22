package models.Utils;

import java.util.HashMap;

public class AccessPermission {
    public enum AccessType {
        NO_ACCESS, CREATE_PR, VIEW_PR, EDIT_PR, APPROVE_PR, EDIT_AND_APPROVE_PR, VIEW_PO, EDIT_PO, APPROVE_PO, VERIFY_PO
    }

    public static HashMap<String, HashMap<String, AccessType>> PRPermissionMap = new HashMap<>();
    public static HashMap<String, HashMap<String, AccessType>> POPermissionMap = new HashMap<>();

    static {
        // Setup PR permission for each role, and for each pr status
        HashMap<String, AccessType> adminPRAccessMap = new HashMap<>();
        adminPRAccessMap.put("pending", AccessType.EDIT_AND_APPROVE_PR);
        adminPRAccessMap.put("late", AccessType.EDIT_AND_APPROVE_PR);
        adminPRAccessMap.put("approved", AccessType.VIEW_PR);

        HashMap<String, AccessType> salesPRAccessMap = new HashMap<>();
        salesPRAccessMap.put("pending", AccessType.EDIT_PR);
        salesPRAccessMap.put("late", AccessType.EDIT_PR);
        salesPRAccessMap.put("approved", AccessType.VIEW_PR);

        HashMap<String, AccessType> purchasePRAccessMap = new HashMap<>();
        purchasePRAccessMap.put("pending", AccessType.APPROVE_PR);
        purchasePRAccessMap.put("late", AccessType.APPROVE_PR);
        purchasePRAccessMap.put("approved", AccessType.VIEW_PR);

        HashMap<String, AccessType> inventoryPRAccessMap = new HashMap<>();
        inventoryPRAccessMap.put("pending", AccessType.NO_ACCESS);
        inventoryPRAccessMap.put("late", AccessType.NO_ACCESS);
        inventoryPRAccessMap.put("approved", AccessType.NO_ACCESS);

        HashMap<String, AccessType> financePRAccessMap = new HashMap<>();
        financePRAccessMap.put("pending", AccessType.VIEW_PR);
        financePRAccessMap.put("late", AccessType.VIEW_PR);
        financePRAccessMap.put("approved", AccessType.VIEW_PR);

        // Setup PO permission for each role, and for each pr status
        HashMap<String, AccessType> adminPOAccessMap = new HashMap<>();
        adminPOAccessMap.put("pending", AccessType.APPROVE_PO);
        adminPOAccessMap.put("approved", AccessType.VERIFY_PO);
        adminPOAccessMap.put("verified", AccessType.VIEW_PO); // TODO:  Admin should be able to pay for verified PO as well
        adminPOAccessMap.put("deleted", AccessType.VIEW_PO);
        adminPOAccessMap.put("returned", AccessType.VIEW_PO);
        adminPOAccessMap.put("paid", AccessType.VIEW_PO);

        HashMap<String, AccessType> salesPOAccessMap = new HashMap<>();
        salesPOAccessMap.put("pending", AccessType.VIEW_PO);
        salesPOAccessMap.put("approved", AccessType.VIEW_PO);
        salesPOAccessMap.put("verified", AccessType.VIEW_PO);
        salesPOAccessMap.put("deleted", AccessType.VIEW_PO);
        salesPOAccessMap.put("returned", AccessType.VIEW_PO);
        salesPOAccessMap.put("paid", AccessType.VIEW_PO);

        HashMap<String, AccessType> purchasePOAccessMap = new HashMap<>();
        purchasePOAccessMap.put("pending", AccessType.EDIT_PO);
        purchasePOAccessMap.put("approved", AccessType.VIEW_PO);
        purchasePOAccessMap.put("verified", AccessType.VIEW_PO);
        purchasePOAccessMap.put("deleted", AccessType.VIEW_PO);
        purchasePOAccessMap.put("returned", AccessType.VIEW_PO);
        purchasePOAccessMap.put("paid", AccessType.VIEW_PO);

        HashMap<String, AccessType> inventoryPOAccessMap = new HashMap<>();
        inventoryPOAccessMap.put("pending", AccessType.VIEW_PO);
        inventoryPOAccessMap.put("approved", AccessType.VERIFY_PO);
        inventoryPOAccessMap.put("verified", AccessType.VIEW_PO);
        inventoryPOAccessMap.put("deleted", AccessType.VIEW_PO);
        inventoryPOAccessMap.put("returned", AccessType.VIEW_PO);
        inventoryPOAccessMap.put("paid", AccessType.VIEW_PO);

        HashMap<String, AccessType> financePOAccessMap = new HashMap<>();
        financePOAccessMap.put("pending", AccessType.APPROVE_PO);
        financePOAccessMap.put("approved", AccessType.VIEW_PO);
        financePOAccessMap.put("verified", AccessType.VIEW_PO);
        financePOAccessMap.put("deleted", AccessType.VIEW_PO);
        financePOAccessMap.put("returned", AccessType.VIEW_PO);
        financePOAccessMap.put("paid", AccessType.VIEW_PO);

        // Load up PR Permission Map
        PRPermissionMap.put("1", adminPRAccessMap);
        PRPermissionMap.put("2", salesPRAccessMap);
        PRPermissionMap.put("3", purchasePRAccessMap);
        PRPermissionMap.put("4", inventoryPRAccessMap);
        PRPermissionMap.put("5", financePRAccessMap);

        // Load up PO Permission Map
        POPermissionMap.put("1", adminPOAccessMap);
        POPermissionMap.put("2", salesPOAccessMap);
        POPermissionMap.put("3", purchasePOAccessMap);
        POPermissionMap.put("4", inventoryPOAccessMap);
        POPermissionMap.put("5", financePOAccessMap);

    }

}
