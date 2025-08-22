package com.projectinventorymanagement.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String transactionID;
    private String itemCode;
    private String code; // Can be HospitalCode or SupplierCode
    private String details; // "Distribute" or "Receive"
    private int quantity;
    private LocalDateTime dateTime;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction(String transactionID, String itemCode, String code, String details, int quantity) {
        this.transactionID = transactionID;
        this.itemCode = itemCode;
        this.code = code;
        this.details = details;
        this.quantity = quantity;
        this.dateTime = LocalDateTime.now();
    }

    public Transaction(String transactionID, String itemCode, String code, String details, int quantity,
            String dateTimeStr) {
        this(transactionID, itemCode, code, details, quantity);
        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
            try {
                this.dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e) {
                System.err.println("Error parsing date time: " + dateTimeStr);
                // Keep the default current time
            }
        }
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDateTimeString() {
        return dateTime != null ? dateTime.format(formatter) : "";
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setDateTime(String dateTimeStr) {
        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
            try {
                this.dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e) {
                System.err.println("Error setting date time: " + dateTimeStr);
            }
        }
    }

    @Override
    public String toString() {
        return transactionID + "," + itemCode + "," + code + "," + details + "," + quantity + "," + dateTime;
    }
}
