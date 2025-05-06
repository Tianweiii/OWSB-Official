package models.DTO;

import models.Datas.Payment;
import models.Datas.PurchaseOrder;
import models.Datas.Supplier;
import models.Utils.FileIO;
import models.Utils.QueryBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionDTO {

    // Supplier name, supplier id
    private String supplierDetails;
    private String paymentReference;
    private double amount;
    private String status;

    private static List<String> paymentPOs;

    // qb returns all payments
    // qb returns all suppliers of PO
    // extract required data from payments + supplier data
    // combine extracted to transactionDTO

    public static List<PaymentDTO> getBasicPaymentDetails() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        QueryBuilder<Payment> qb = new QueryBuilder<>(Payment.class);
        ArrayList<Payment> data = qb.select().from("db/Payment").getAsObjects();

        List<PaymentDTO> paymentData = data.stream()
                                    .map(payment -> new PaymentDTO(payment.getPO_ID(), payment.getPaymentReference(), payment.getAmount(), "Success"))
                                    .collect(Collectors.toList());

        paymentPOs = data.stream().map(Payment::getPO_ID).toList();
        System.out.println(paymentPOs);

        return paymentData;
    }

    public static List<Supplier> getBasicSupplierDetails() throws IOException, ReflectiveOperationException {
        // getting ids only
        Set<String> PO_IDs = new HashSet<>(Arrays.asList("PO6", "PO10", "PO11"));
        Set<String> PR_IDs = FileIO.filterIDFileBelow("PurchaseOrder", 0, 1, PO_IDs);
        Set<String> ItemIDs = FileIO.filterIDFileBelow("PurchaseRequisitionItem", 1, 2, PR_IDs);
        Set<String> SupplierIDs = FileIO.filterIDFileBelow("Item", 0, 7, ItemIDs);

        return FileIO.getIDsAsObjects(Supplier.class, "Supplier", SupplierIDs);
    }

//    private ObservableList<TransactionDTO> getAllTransactions() {
//
//    }
//
//    private ObservableList<TransactionDTO> getRecentTransactions() {
//
//    }
}
