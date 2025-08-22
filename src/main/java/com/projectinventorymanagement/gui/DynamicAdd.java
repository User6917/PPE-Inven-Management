package com.projectinventorymanagement.gui;

import com.projectinventorymanagement.database.DatabaseBase;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;

public class DynamicAdd {
    private final DatabaseBase database;
    private Stage popupStage;
    private final Stage ownerStage;

    public DynamicAdd(DatabaseBase database, Stage ownerStage) {
        this.database = database;
        this.ownerStage = ownerStage;
    }

    public void showForm() {
        this.popupStage = new Stage();

        // Set the owner before making the stage visible
        popupStage.initOwner(ownerStage);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Add New Entry");

        // Add close handler to ensure overlay is hidden
        popupStage.setOnHidden(event -> {
            if (popupStage.getOwner() != null) {
                popupStage.getOwner().getScene().getRoot().setEffect(null);
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(10));

        // Get the first row to determine the number of fields
        HashMap<Integer, ArrayList<String>> entries = database.getEntries();
        if (entries.isEmpty()) {
            showAlert("Error", "No structure found in database.");
            return;
        }

        int numFields = entries.values().iterator().next().size();
        TextField[] textFields = new TextField[numFields];

        for (int i = 0; i < numFields; i++) {
            TextField textField = new TextField();
            textField.setPromptText("Field " + (i + 1));
            textFields[i] = textField;
            layout.getChildren().add(textField);
        }

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(_ -> {
            ArrayList<String> newEntry = new ArrayList<>();
            boolean isValid = true;

            for (TextField field : textFields) {
                if (field.getText().trim().isEmpty()) {
                    showAlert("Input Error", "All fields must be filled out.");
                    isValid = false;
                    break;
                }
                newEntry.add(field.getText());
            }

            if (isValid) {
                database.addEntry(database.getEntries().size(), newEntry);
                database.saveData();
                popupStage.close();
            }
        });

        layout.getChildren().add(submitButton);

        // Adjust window size dynamically to fit all fields
        double windowHeight = 70 + (numFields * 45);
        Scene scene = new Scene(layout, 300, Math.max(windowHeight, 250));
        popupStage.setScene(scene);

        // Show the stage at the end
        popupStage.show();
    }

    // Method to get the stage
    public Stage getStage() {
        return popupStage;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
