package com.projectinventorymanagement.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SupplierDatabase extends DatabaseBase {
    
    public SupplierDatabase() {
        super("supplier");
    }

    // Add a new supplier
    public void addSupplier(SupplierEntry supplier) {
        ArrayList<String> entry = convertToEntryFormat(supplier);
        int index = boundList.size() + 1;
        addEntry(index, entry);
    }

    private ArrayList<String> convertToEntryFormat(SupplierEntry supplier) {
        ArrayList<String> entry = new ArrayList<>();
        entry.add(supplier.getSupplierCode());
        entry.add(supplier.getSupplierName());
        entry.add(String.valueOf(supplier.isActive()));

        // Convert item list to a single string format
        StringBuilder itemsString = new StringBuilder();
        for (ItemData item : supplier.getItemsProvided()) {
            itemsString.append(item.getItemCode()).append(":").append(item.getQuantity()).append(";");
        }
        entry.add(itemsString.toString());

        return entry;
    }

    // Retrieves Supplier Names
    public ArrayList<String> getNames() {
        ArrayList<String> supplierNames = new ArrayList<>();
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty()) {
                supplierNames.add(row.get(1));
            }
        }
        return supplierNames;
    }

    // Retrieves Supplier Code via Supplier Name
    public String getCodeByName(String supplierName) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(1).equals(supplierName)) {
                return row.get(0);
            }
        }
        return null;
    }

    // Retrieves Supplier Name via Supplier Code
    public String getNameByCode(String supplierCode) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(0).equals(supplierCode)) {
                return row.get(1);
            }
        }
        return null;
    }

        /**
     * Updates the supplier record when items are received.
     * 
     * Each supplier record has the following fields:
     * Supplier Code, Supplier Name, Item Code, Quantity, isActive
     *
     * This method finds the record matching the given supplierCode and itemCode,
     * adds the received quantity to the existing quantity, and saves the changes.
     * If no record exists, it creates a new record.
     */
    public void updateSupplierReceivedItems(String supplierCode, String itemCode, int quantity) {
        
        for (Map.Entry<Integer, ArrayList<String>> entry : getEntries().entrySet()) { // <- Iterate over all entries in the supplier database.
            ArrayList<String> row = entry.getValue();
            
            if (!row.isEmpty() && row.get(0).equals(supplierCode) && row.get(2).equals(itemCode)) { // <- Check if this row matches the given supplier and item code.
                
                int currentQuantity = Integer.parseInt(row.get(3)); // <- Parse current quantity and add the new quantity.
                row.set(3, String.valueOf(currentQuantity + quantity));
                
                updateEntry(entry.getKey(), row); // <- Update the row using the integer row index. Since format is HashMap<Integer(Row), <ArrayList<String>>.
                saveData();
                return;
            }
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

    public static class SupplierEntry {
        private String supplierCode;
        private String supplierName;
        private boolean isActive;
        private List<ItemData> itemsProvided;

        public SupplierEntry(String supplierCode, String supplierName, List<ItemData> items, boolean isActive) {
            this.supplierCode = supplierCode;
            this.supplierName = supplierName;
            this.itemsProvided = new ArrayList<>(items);
            this.isActive = isActive;
        }

        public String getSupplierCode() {
            return supplierCode;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public List<ItemData> getItemsProvided() {
            return itemsProvided;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }

        @Override
        public String toString() {
            return "SupplierCode: " + supplierCode + ", SupplierName: " + supplierName +
                   ", Items: " + itemsProvided + ", Active: " + isActive;
        }
    }

    public static class ItemData {
        private String itemCode;
        private int quantity;

        public ItemData(String itemCode, int quantity) {
            this.itemCode = itemCode;
            this.quantity = quantity;
        }

        public String getItemCode() {
            return itemCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return itemCode + ": " + quantity;
        }
    }
}