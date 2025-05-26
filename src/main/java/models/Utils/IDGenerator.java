package models.Utils;

public class IDGenerator {
    public static String generateLogID() {
        return "LOG-" + System.currentTimeMillis();
    }
    public static String generateBatchID() {
        return "Batch-" + System.currentTimeMillis();
    }
//    public static String generateItemID() {
//
//    }
}
