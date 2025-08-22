package com.projectinventorymanagement.database;

import java.util.ArrayList;
import java.util.List;

import com.projectinventorymanagement.models.Item;
import com.projectinventorymanagement.models.Transaction;

public class TransactionDatabase extends DatabaseBase {

    public TransactionDatabase() {
        super("transactions");
    }

    public void addTransaction(String transactionID, Item item, String code, String details, int quantity) {
        ArrayList<String> entry = convertToEntryFormat(transactionID, item.getItemCode(), code, details, quantity,
                null);
        int index = boundList.size() + 1;
        addEntry(index, entry);
    }

    private ArrayList<String> convertToEntryFormat(String transactionID, String itemCode, String code, String details,
            int quantity, String dateTime) {
        ArrayList<String> entry = new ArrayList<>();
        entry.add(transactionID);
        entry.add(itemCode);
        entry.add(code);
        entry.add(details);
        entry.add(String.valueOf(quantity));
        if (dateTime != null) {
            entry.add(dateTime);
        }
        return entry;
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for (ArrayList<String> entry : boundList.values()) {
            transactions.add(convertToTransaction(entry));
        }
        return transactions;
    }

    public void reloadData() {
        loadData(); // This is from DatabaseBase (your parent class)
    }

    private Transaction convertToTransaction(ArrayList<String> entry) {
        // Check if we have date-time information (6th column)
        if (entry.size() >= 6) {
            return new Transaction(
                    entry.get(0), // transactionID
                    entry.get(1), // itemCode
                    entry.get(2), // code
                    entry.get(3), // details
                    Integer.parseInt(entry.get(4)), // quantity
                    entry.get(5) // date-time
            );
        } else {
            // Fallback to constructor without date-time (it will use current time)
            return new Transaction(
                    entry.get(0), // transactionID
                    entry.get(1), // itemCode
                    entry.get(2), // code
                    entry.get(3), // details
                    Integer.parseInt(entry.get(4)) // quantity
            );
        }
    }

    @Override
    protected String toStringFormat(ArrayList<String> entry) {
        return String.join(",", entry);
    }

    @Override
    protected ArrayList<String> fromStringFormat(String line) {
        ArrayList<String> entry = new ArrayList<>();
        String[] parts = line.split(",");
        for (String part : parts) {
            entry.add(part);
        }
        return entry;
    }
}