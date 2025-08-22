package com.projectinventorymanagement.gui;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.animation.ScaleTransition;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;
import javafx.scene.layout.*;
import com.projectinventorymanagement.database.UserDatabase;

public class UserGUI extends TableGUI {

    private Button modifyUserButton;

    public UserGUI(Stage stage, UserDatabase userDatabase) {
        super(stage, userDatabase);
        this.modifyUserButton = new Button("Modify User");
    }

    @Override
    protected String getDataSource() {
        return "user";
    }

    @Override
    protected BorderPane createLayout() {
        BorderPane mainLayout = super.createLayout(); // Get the existing layout

        // Initialize the modify user button
        modifyUserButton = createIconButton("Modify User", "/icons/edit.png");
        modifyUserButton.setDisable(true);
        modifyUserButton.setOnAction(_ -> openModifyUserDialog());

        // Navigate through the component hierarchy to find the button row
        StackPane stackPane = (StackPane) mainLayout.getCenter();
        BorderPane innerBorderPane = (BorderPane) stackPane.getChildren().get(0);
        HBox contentSection = (HBox) innerBorderPane.getCenter();
        VBox tableSection = (VBox) contentSection.getChildren().get(0);
        VBox controlsSection = (VBox) tableSection.getChildren().get(1);
        HBox buttonRow = (HBox) controlsSection.getChildren().get(1);

        // Add the button to the button row
        buttonRow.getChildren().add(1, modifyUserButton);

        return mainLayout;
    }

    private void openModifyUserDialog() {
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            showAlert("No Selection", "Please select a user to modify.");
            return;
        }

        // Get the isActive column value (last column, index 7)
        String isActive = selectedRow.get(7);
        if ("-".equals(isActive)) {
            showAlert("Modification Restricted", "This user is a default Admin and cannot be modified.");
            return;
        }

        Dialog<ArrayList<String>> dialog = new Dialog<>();
        dialog.setTitle("Modify User");
        dialog.setHeaderText("Edit user details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(selectedRow.get(1));
        TextField userTypeField = new TextField(selectedRow.get(4));
        TextField emailField = new TextField(selectedRow.get(5));
        TextField phoneField = new TextField(selectedRow.get(6));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("User Type:"), 0, 1);
        grid.add(userTypeField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                ArrayList<String> updatedData = new ArrayList<>();
                updatedData.add(selectedRow.get(0)); // <- Keep UserID
                updatedData.add(nameField.getText());
                updatedData.add(selectedRow.get(2)); // <- Keep Username
                updatedData.add(selectedRow.get(3)); // <- Keep Password
                updatedData.add(userTypeField.getText());
                updatedData.add(emailField.getText());
                updatedData.add(phoneField.getText());
                updatedData.add(selectedRow.get(7)); // <- Keep isActive status
                return updatedData;
            }
            return null;
        });

        Optional<ArrayList<String>> result = dialog.showAndWait();
        result.ifPresent(updatedData -> updateUserInDatabase(selectedRow, updatedData));
    }

    private void updateUserInDatabase(ObservableList<String> oldRow, ArrayList<String> updatedData) {
        int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            HashMap<Integer, ArrayList<String>> dbData = database.getEntries();
            if (dbData.containsKey(selectedIndex + 1)) {
                dbData.put(selectedIndex + 1, updatedData);
            }
            database.saveData();
            loadData();
            tableView.refresh();
        }
    }

    @Override
    protected void updateDeleteButton() {
        super.updateDeleteButton();
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();

        // Disable modifyUserButton if isActive is "-"
        if (selectedRow == null || "-".equals(selectedRow.get(7))) {
            modifyUserButton.setDisable(true);
        } else {
            modifyUserButton.setDisable(false);
        }
    }
}