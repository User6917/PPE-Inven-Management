package com.projectinventorymanagement.database;

import com.projectinventorymanagement.models.Item;
import com.projectinventorymanagement.models.Supplier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemDatabase extends DatabaseBase {

    public ItemDatabase() {
        super("ppe"); // Loads data from "data/ppe.txt"
        // Ensure that header row exists. Row 0 holds header fields.
        if (!getEntries().containsKey(0) || getEntries().get(0) == null || getEntries().get(0).isEmpty()) {
            ArrayList<String> header = new ArrayList<>(Arrays.asList("ItemCode", "ItemName", "Quantity", "SupplierCode"));
            getEntries().put(0, header);
            saveData();
        }
    }

    /**
     * Adds a new item entry.
     * @param item The item (providing item code and name)
     * @param supplier The supplier (providing supplier code)
     * @param quantity The quantity (in boxes)
     */
    public void addItem(Item item, Supplier supplier, int quantity) {
        ArrayList<String> row = new ArrayList<>(Arrays.asList(
            item.getItemCode(),
            item.getItemName(),
            Integer.toString(quantity),
            supplier.getSupplierCode()
        ));

        int newRow = getEntries().size();
        addEntry(newRow, row);
    }
    
    /**
     * Retrieves a list of item entries (skipping the header row).
     */
    public List<ArrayList<String>> getItems() {
        List<ArrayList<String>> list = new ArrayList<>();
        for (int i = 1; i < getEntries().size(); i++) {
            list.add(getEntries().get(i));
        }
        return list;
    }

    // Retrieves Item Names
    public ArrayList<String> getNames() {
        ArrayList<String> itemNames = new ArrayList<>();
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty()) {
                itemNames.add(row.get(1));
            }
        }
        return itemNames;
    }
    
    /**
     * Displays all item entries in the console.
     */
    public void displayItems() {
        for (int i = 1; i < getEntries().size(); i++) {
            ArrayList<String> row = getEntries().get(i);
            System.out.println("Item Code: " + row.get(0) +
                               ", Item Name: " + row.get(1) +
                               ", Quantity: " + row.get(2) +
                               ", Supplier Code: " + row.get(3));
        }
    }

    // Retrieves Item Name via Item Code
    public String getCodeByName(String itemName) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(1).equals(itemName)) {
                return row.get(0);
            }
        }
        return null;
    }

    // Item Qty update method
    public boolean updateItemQuantity(String itemCode, int quantityChange) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(0).equals(itemCode)) {
                int currentQuantity = Integer.parseInt(row.get(2));
                int newQuantity = currentQuantity + quantityChange;
    
                if (newQuantity < 0) {
                    return false; // <- Prevent negative stock
                }
    
                row.set(2, Integer.toString(newQuantity));
                saveData();
                return true;
            }
        }
        return false;
    }

    // Retrieves Item Qty
    public int getItemQuantity(String itemCode) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(0).equals(itemCode)) {
                return Integer.parseInt(row.get(2));
            }
        }
        return 0; // Item not found, return 0
    }

    // Retrieves Item Name via Item Code
    public String getNameByCode(String itemCode) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(0).equals(itemCode)) {
                return row.get(1);
            }
        }
        return null;
    }

    @Override
    protected String toStringFormat(ArrayList<String> entry) {
        return String.join(",", entry);
    }

    @Override
    protected ArrayList<String> fromStringFormat(String line) {
        return new ArrayList<>(Arrays.asList(line.split(",")));
    }
}