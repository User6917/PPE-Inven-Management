package com.projectinventorymanagement.gui;

import javafx.stage.Stage;
import com.projectinventorymanagement.database.SupplierDatabase;

public class SupplierGUI extends TableGUI {

    public SupplierGUI(Stage stage, SupplierDatabase supplierDatabase) {
        super(stage, supplierDatabase);
    }

    @Override
    protected String getDataSource() {
        return "supplier";
    }
}