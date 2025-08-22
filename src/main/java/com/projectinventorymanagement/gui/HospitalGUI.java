package com.projectinventorymanagement.gui;

import javafx.stage.Stage;
import com.projectinventorymanagement.database.HospitalDatabase;

public class HospitalGUI extends TableGUI {
    
    public HospitalGUI(Stage stage, HospitalDatabase hospitalDatabase) {
        // Pass both the stage and a new HospitalDatabase instance to the superclass
        super(stage, hospitalDatabase);
    }

    @Override
    protected String getDataSource() {
        return "hospital";
    }
}
