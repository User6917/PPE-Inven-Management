package com.projectinventorymanagement.gui;

import com.projectinventorymanagement.database.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionGUI {
    private ComboBox<String> transactionTypeDropdown;
    private ComboBox<String> entityDropdown;
    private ComboBox<String> itemDropdown;
    private TextField quantityField;
    private Button confirmButton;
    private final TransactionDatabase transactionDatabase;
    private final ItemDatabase itemDatabase;
    private final HospitalDatabase hospitalDatabase;
    private final SupplierDatabase supplierDatabase;

    private ObservableList<String> hospitals;
    private ObservableList<String> suppliers;
    private ObservableList<String> items;
    private HashMap<String, List<String>> supplierToItemsMap;

    // Color scheme for dark mode - matching MainMenuGUI
    private final String PRIMARY_COLOR = "#3498db"; // Accent color (blue)
    private final String SECONDARY_COLOR = "#2a2a3f"; // Secondary background
    private final String BACKGROUND_COLOR = "#1e1e2f"; // Main background
    private final String TEXT_COLOR = "#ecf0f1"; // Light gray text
    private final String SUCCESS_COLOR = "#2ecc71"; // Green for success
    private final String ERROR_COLOR = "#e74c3c"; // Red for errors

    public TransactionGUI(TransactionDatabase transactionDatabase, ItemDatabase itemDatabase,
            HospitalDatabase hospitalDatabase, SupplierDatabase supplierDatabase) {
        this.transactionDatabase = transactionDatabase;
        this.itemDatabase = itemDatabase;
        this.hospitalDatabase = hospitalDatabase;
        this.supplierDatabase = supplierDatabase;

        this.hospitals = FXCollections.observableArrayList(hospitalDatabase.getNames());
        this.suppliers = FXCollections.observableArrayList(getUniqueSuppliers());
        this.items = FXCollections.observableArrayList();
        this.supplierToItemsMap = mapSupplierToItems();
    }

    public void showTransactionPopup(Stage parentStage) {
        Stage popupStage = new Stage();
        popupStage.initOwner(parentStage);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Transaction Management");

        // Create header
        Label headerLabel = new Label("Create New Transaction");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        headerLabel.setTextFill(Color.web(TEXT_COLOR));

        HBox header = new HBox(headerLabel);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");

        // Create styled transaction type dropdown
        Label typeLabel = createLabel("Transaction Type");
        transactionTypeDropdown = createStyledComboBox("Select Transaction Type");
        transactionTypeDropdown.setItems(FXCollections.observableArrayList("Distribute", "Receive"));

        // Try to load icons for dropdown items
        try {
            ImageView distributeIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/distribute.png")));
            ImageView receiveIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/receive.png")));
            distributeIcon.setFitHeight(16);
            distributeIcon.setFitWidth(16);
            receiveIcon.setFitHeight(16);
            receiveIcon.setFitWidth(16);

            // Add icons to dropdown (if available)
            // Using a custom cell factory would go here
        } catch (Exception e) {
            // Fallback - no icons
        }

        // Create styled entity dropdown
        Label entityLabel = createLabel("Hospital/Supplier");
        entityDropdown = createStyledComboBox("Select Hospital/Supplier");

        // Create styled item dropdown
        Label itemLabel = createLabel("Item");
        itemDropdown = createStyledComboBox("Select Item");

        // Create styled quantity field
        Label quantityLabel = createLabel("Quantity");
        quantityField = createStyledTextField("Enter Quantity");

        // Event listeners
        transactionTypeDropdown.setOnAction(_ -> updateEntityDropdown());
        entityDropdown.setOnAction(_ -> updateItemDropdown());

        // Create confirm button with icon
        confirmButton = createStyledButton("Confirm Transaction", SUCCESS_COLOR);
        confirmButton.setOnAction(_ -> handleTransaction(popupStage));

        // Create cancel button
        Button cancelButton = createStyledButton("Cancel", ERROR_COLOR);
        cancelButton.setOnAction(_ -> popupStage.close());

        // Button container
        HBox buttonBox = new HBox(20, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 10, 0));

        // Form layout with proper spacing and alignment
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setAlignment(Pos.CENTER);

        // Add components to grid
        formGrid.add(typeLabel, 0, 0);
        formGrid.add(transactionTypeDropdown, 1, 0);
        formGrid.add(entityLabel, 0, 1);
        formGrid.add(entityDropdown, 1, 1);
        formGrid.add(itemLabel, 0, 2);
        formGrid.add(itemDropdown, 1, 2);
        formGrid.add(quantityLabel, 0, 3);
        formGrid.add(quantityField, 1, 3);

        // Set column constraints
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setHgrow(Priority.NEVER);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        fieldColumn.setFillWidth(true);
        fieldColumn.setPrefWidth(200);

        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        // Create a styled card for the form
        VBox formCard = new VBox(formGrid);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Add shadow effect to card
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setOffsetY(3);
        formCard.setEffect(shadow);

        // Main layout
        VBox mainLayout = new VBox(20, header, formCard, buttonBox);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setBackground(new Background(new BackgroundFill(
                Color.web(BACKGROUND_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        mainLayout.setPadding(new Insets(0, 0, 20, 0));

        // Create scene
        Scene scene = new Scene(mainLayout, 450, 500);

        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.showAndWait();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        label.setTextFill(Color.rgb(70, 70, 70));
        return label;
    }

    private ComboBox<String> createStyledComboBox(String promptText) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText(promptText);
        comboBox.setPrefWidth(250);
        comboBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 5;");
        return comboBox;
    }

    private TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefWidth(250);
        textField.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 5;");
        return textField;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefSize(180, 40);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 5px;");

        // Hover effects
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", -10%);" +
                        "-fx-background-radius: 5px;"));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 5px;"));

        return button;
    }

    private void updateEntityDropdown() {
        if ("Distribute".equals(transactionTypeDropdown.getValue())) {
            entityDropdown.setItems(hospitals);
            entityDropdown.setPromptText("Select Hospital");
        } else if ("Receive".equals(transactionTypeDropdown.getValue())) {
            entityDropdown.setItems(suppliers);
            entityDropdown.setPromptText("Select Supplier");
        }
    }

    private void updateItemDropdown() {
        String selectedEntity = entityDropdown.getValue();
        if (selectedEntity == null) {
            itemDropdown.setItems(FXCollections.observableArrayList());
            itemDropdown.setPromptText("Please select a entity first");
            return;
        }

        List<String> filteredItems = new ArrayList<>();
        if ("Distribute".equals(transactionTypeDropdown.getValue())) {
            for (ArrayList<String> itemEntry : itemDatabase.getItems()) {
                String itemName = itemEntry.get(1);
                int quantity = Integer.parseInt(itemEntry.get(2));
                if (quantity > 0) {
                    filteredItems.add(itemName);
                }
            }
            itemDropdown.setPromptText("Select Item to Distribute");
        } else {
            filteredItems = supplierToItemsMap.getOrDefault(selectedEntity, Collections.emptyList());
            itemDropdown.setPromptText("Select Item to Receive");
        }

        itemDropdown.setItems(FXCollections.observableArrayList(filteredItems));
    }

    private List<String> getUniqueSuppliers() {
        Set<String> uniqueSuppliers = new HashSet<>(supplierDatabase.getNames());
        return new ArrayList<>(uniqueSuppliers);
    }

    private HashMap<String, List<String>> mapSupplierToItems() {
        HashMap<String, List<String>> map = new HashMap<>();
        for (ArrayList<String> itemEntry : itemDatabase.getItems()) {
            String itemName = itemEntry.get(1);
            String supplierCode = itemEntry.get(3);
            String supplierName = supplierDatabase.getNameByCode(supplierCode);

            map.computeIfAbsent(supplierName, _ -> new ArrayList<>()).add(itemName);
        }
        return map;
    }

    private void handleTransaction(Stage popupStage) {
        String type = transactionTypeDropdown.getValue();
        String entity = entityDropdown.getValue();
        String item = itemDropdown.getValue();
        String quantityStr = quantityField.getText();

        if (type == null || entity == null || item == null || quantityStr.isEmpty()) {
            showAlert("Error", "Please fill in all fields.", Alert.AlertType.ERROR);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                showAlert("Error", "Quantity must be greater than zero.", Alert.AlertType.ERROR);
                return;
            }

            String itemCode = itemDatabase.getCodeByName(item);
            String entityCode = ("Distribute".equals(type)) ? hospitalDatabase.getCodeByName(entity)
                    : supplierDatabase.getCodeByName(entity);

            int availableQuantity = itemDatabase.getItemQuantity(itemCode);
            if ("Distribute".equals(type) && quantity > availableQuantity) {
                showAlert("Error", "Insufficient stock. Available: " + availableQuantity, Alert.AlertType.ERROR);
                return;
            }

            int transactionID = new Random().nextInt(1000000);
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ArrayList<String> transactionEntry = new ArrayList<>();
            transactionEntry.add(String.valueOf(transactionID));
            transactionEntry.add(itemCode);
            transactionEntry.add(entityCode);
            transactionEntry.add(type);
            transactionEntry.add(quantityStr);
            transactionEntry.add(dateTime);

            transactionDatabase.addEntry(transactionDatabase.getEntries().size(), transactionEntry);
            transactionDatabase.saveData();

            if ("Distribute".equals(type)) {
                itemDatabase.updateItemQuantity(itemCode, -quantity);
                updateHospitalReceivedItems(entityCode, itemCode, quantity);
            } else if ("Receive".equals(type)) {
                itemDatabase.updateItemQuantity(itemCode, quantity);
                supplierDatabase.updateSupplierReceivedItems(entityCode, itemCode, quantity);
            }

            showSuccessDialog(type, entity, item, quantity);
            popupStage.close();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for quantity.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateHospitalReceivedItems(String hospitalCode, String itemCode, int quantity) {
        // Find the hospital entry by its code.
        ArrayList<String> hospitalEntry = hospitalDatabase.findHospitalByCode(hospitalCode);
        if (hospitalEntry == null) {
            System.out.println("Hospital not found: " + hospitalCode);
            return;
        }

        // Define the allowed item codes (Match Item Column Headers in hospital.txt)
        List<String> allowedCodes = Arrays.asList("HC", "FS", "MS", "GL", "GW", "SC");

        // Use the item code directly to find the index in allowedCodes.
        int itemIndexInList = allowedCodes.indexOf(itemCode);
        if (itemIndexInList == -1) {
            System.out.println("Invalid item code: " + itemCode);
            return;
        }

        // In the hospital row:
        // "Items Received (Total Boxes)" is at index 2.
        // The specific item columns start at index 3:
        // For example, allowedCodes[0] ("HC") is at index 3, allowedCodes[1] ("FS") is
        // at index 4, etc.
        int totalReceivedIndex = 2;
        int specificItemColumnIndex = 3 + itemIndexInList;

        // Parse the current values and update them.
        int currentTotal = Integer.parseInt(hospitalEntry.get(totalReceivedIndex));
        int currentSpecific = Integer.parseInt(hospitalEntry.get(specificItemColumnIndex));

        hospitalEntry.set(totalReceivedIndex, String.valueOf(currentTotal + quantity));
        hospitalEntry.set(specificItemColumnIndex, String.valueOf(currentSpecific + quantity));

        // Retrieve the integer row index for this hospital.
        int hospitalRowIndex = getHospitalRowIndex(hospitalCode);
        if (hospitalRowIndex != -1) {
            hospitalDatabase.updateEntry(hospitalRowIndex, hospitalEntry);
            hospitalDatabase.saveData();
        } else {
            System.out.println("Hospital row not found for: " + hospitalCode);
        }
    }

    private int getHospitalRowIndex(String hospitalCode) {
        for (Map.Entry<Integer, ArrayList<String>> entry : hospitalDatabase.getEntries().entrySet()) {
            ArrayList<String> row = entry.getValue();
            if (!row.isEmpty() && row.get(0).equals(hospitalCode)) {
                return entry.getKey();
            }
        }
        return -1; // Not found
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
        ((Button) dialogPane.lookupButton(ButtonType.OK)).setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5px;");

        alert.showAndWait();
    }

    private void showSuccessDialog(String type, String entity, String item, int quantity) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Successful");
        alert.setHeaderText(null);

        // Create a more visually appealing success message
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);

        Label successLabel = new Label("Transaction Completed Successfully");
        successLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        successLabel.setTextFill(Color.web(SUCCESS_COLOR));

        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(5);
        details.add(new Label("Type:"), 0, 0);
        details.add(new Label("Entity:"), 0, 1);
        details.add(new Label("Item:"), 0, 2);
        details.add(new Label("Quantity:"), 0, 3);

        details.add(new Label(type), 1, 0);
        details.add(new Label(entity), 1, 1);
        details.add(new Label(item), 1, 2);
        details.add(new Label(String.valueOf(quantity)), 1, 3);

        content.getChildren().addAll(successLabel, details);

        // Try to load a success icon
        try {
            ImageView successIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/success.png")));
            successIcon.setFitHeight(48);
            successIcon.setFitWidth(48);
            content.getChildren().add(0, successIcon);
        } catch (Exception e) {
            // No icon available
        }

        alert.getDialogPane().setContent(content);

        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        ((Button) dialogPane.lookupButton(ButtonType.OK)).setStyle(
                "-fx-background-color: " + SUCCESS_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5px;");

        alert.showAndWait();
    }
}