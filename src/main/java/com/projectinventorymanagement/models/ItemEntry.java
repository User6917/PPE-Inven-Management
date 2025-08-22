package com.projectinventorymanagement.models;

public class ItemEntry {
    private Item item;
    private Supplier supplier;
    private int quantity;

    public ItemEntry(Item item, Supplier supplier, int quantity) {
        this.item = item;
        this.supplier = supplier;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public int getQuantity() {
        return quantity;
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }
}