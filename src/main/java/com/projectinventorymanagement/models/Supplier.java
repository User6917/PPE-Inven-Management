package com.projectinventorymanagement.models;

public class Supplier{
    private String supplierCode;
    private String supplierName;
    private boolean isActive;

    Supplier(String supplierCode, String supplierName, boolean isActive) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.isActive = isActive;
    }

    // Getter
    public String getSupplierCode() {return supplierCode;}
    public String getSupplierName() {return supplierName;}
    public boolean getIsActive() {return isActive;}

    // Setter
    public void setSupplierCode(String supplierCode) {this.supplierCode = supplierCode;}
    public void setSupplierName(String supplierName) {this.supplierName = supplierName;}
    public void setIsActive(boolean isActive) {this.isActive = isActive;}
}