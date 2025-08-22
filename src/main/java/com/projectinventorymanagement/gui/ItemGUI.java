package com.projectinventorymanagement.gui;

import javafx.stage.Stage;
import com.projectinventorymanagement.database.ItemDatabase;

public class ItemGUI extends TableGUI {
    
    public ItemGUI(Stage stage, ItemDatabase itemDatabase) {
        super(stage, new ItemDatabase());
    }

    @Override
    protected String getDataSource() {
        return "ppe";
    }
}
