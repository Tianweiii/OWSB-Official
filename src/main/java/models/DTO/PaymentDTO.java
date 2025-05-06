package models.DTO;

public record PaymentDTO(String PO_ID, String paymentReference, double amount, String status) {}
