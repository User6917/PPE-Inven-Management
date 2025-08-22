package com.projectinventorymanagement.models;

public class Item {
    private String itemCode;
    private String itemName;

    public Item(String code, String name) {
        this.itemCode = code;
        this.itemName = name;
    }

    // Getter
    public String getItemCode() {return itemCode;}
    public String getItemName() {return itemName;}

    // Setter
    public void setItemCode(String itemCode) {this.itemCode = itemCode;}
    public void setItemName(String itemName) {this.itemName = itemName;}

}
