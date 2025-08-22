package com.projectinventorymanagement.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.control.Alert;

import com.projectinventorymanagement.utils.*;
import com.projectinventorymanagement.database.*;

public class MainMenuGUI extends Application {
    TransactionDatabase transactionDatabase = new TransactionDatabase();
    ItemDatabase itemDatabase = new ItemDatabase();
    HospitalDatabase hospitalDatabase = new HospitalDatabase();
    SupplierDatabase supplierDatabase = new SupplierDatabase();
    UserDatabase userDatabase = new UserDatabase();

    // Color scheme for dark mode
    private final String PRIMARY_COLOR = "#3498db"; // Accent color (blue)
    private final String SECONDARY_COLOR = "#2a2a3f"; // Main background (darker gray)
    private final String BACKGROUND_COLOR = "#1e1e2f"; // Main background (darker gray)
    private final String UPPER_TEXT_COLOR = "#ecf0f1"; // Light gray text

    @Override
    public void start(Stage primaryStage) {
        // Create header with system name and user info
        Label lblSystemName = new Label("Inventory Management System");
        lblSystemName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblSystemName.setTextFill(Color.web(UPPER_TEXT_COLOR));

        String userName = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUsername()
                : "Guest";
        String userType = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUserType() : "";

        Label lblUserInfo = new Label("Welcome, " + userName + " (" + userType + ")");
        lblUserInfo.setFont(Font.font("Segoe UI", 14));
        lblUserInfo.setTextFill(Color.web(UPPER_TEXT_COLOR));

        VBox headerInfo = new VBox(5, lblSystemName, lblUserInfo);

        Button btnLogout = createStyledButton("Logout", "#e74c3c", 100, 40);
        btnLogout.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnLogout.setTextFill(Color.web(UPPER_TEXT_COLOR));

        // Add icon to logout button
        try {
            ImageView logoutIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/logout.png")));
            logoutIcon.setFitHeight(20);
            logoutIcon.setFitWidth(20);
            btnLogout.setGraphic(logoutIcon);
        } catch (Exception e) {
            // Fallback if icon isn't available
            btnLogout.setText("⬅ Logout");
        }

        // Create top panel with header and logout
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(20));
        topPanel.setSpacing(10);
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.getChildren().addAll(headerInfo);
        HBox.setHgrow(headerInfo, Priority.ALWAYS);

        HBox logoutBox = new HBox(btnLogout);
        logoutBox.setAlignment(Pos.CENTER_RIGHT);
        topPanel.getChildren().add(logoutBox);

        // Apply style to top panel
        topPanel.setStyle(
                "-fx-background-color:" + SECONDARY_COLOR + ";"
                        + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Create menu buttons with icons
        Button btnSupplier = createMenuButton("Supplier\nManagement", "/icons/supplier.png");
        Button btnItem = createMenuButton("Item\nManagement", "/icons/item.png");
        Button btnHospitals = createMenuButton("Hospital\nManagement", "/icons/hospital.png");
        Button btnDistribute = createMenuButton("Distribute\nItems", "/icons/distribute.png");
        Button btnReports = createMenuButton("Reports &\nAnalytics", "/icons/report.png");
        Button btnUsers = createMenuButton("User\nManagement", "/icons/users.png");

        // Create tooltips for buttons
        setTooltip(btnSupplier, "Manage suppliers and vendor information");
        setTooltip(btnItem, "Add, edit and track inventory items");
        setTooltip(btnHospitals, "Manage hospital client information");
        setTooltip(btnDistribute, "Distribute inventory to hospitals");
        setTooltip(btnReports, "Generate reports and analytics");
        setTooltip(btnUsers, "Manage system users and permissions");

        // Create card layout for menu items using VBox containers
        VBox cardSupplier = createMenuCard(btnSupplier);
        VBox cardItem = createMenuCard(btnItem);
        VBox cardHospitals = createMenuCard(btnHospitals);
        VBox cardDistribute = createMenuCard(btnDistribute);
        VBox cardReports = createMenuCard(btnReports);
        VBox cardUsers = createMenuCard(btnUsers);

        // First row
        HBox row1 = new HBox(30, cardSupplier, cardItem, cardHospitals);
        row1.setAlignment(Pos.CENTER);

        // Second row
        HBox row2 = new HBox(30, cardDistribute, cardReports);

        // Add users button only for admin
        if (SessionManager.getCurrentUser() != null &&
                "Admin".equals(SessionManager.getCurrentUser().getUserType())) {
            row2.getChildren().add(cardUsers);
        }

        row2.setAlignment(Pos.CENTER);

        // Dashboard area
        VBox dashboardLayout = new VBox(30, row1, row2);
        dashboardLayout.setPadding(new Insets(40));
        dashboardLayout.setAlignment(Pos.CENTER);

        // Add background color to main content area
        dashboardLayout.setBackground(new Background(new BackgroundFill(
                Color.web(BACKGROUND_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));

        // Main layout
        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(dashboardLayout);

        // Get screen size
        double width = Screen.getPrimary().getBounds().getWidth();
        double height = Screen.getPrimary().getBounds().getHeight();

        // Button Logic - all the same as original
        btnSupplier.setOnAction(_ -> {
            try {
                SupplierDatabase supplierDatabase = new SupplierDatabase();
                SupplierGUI supplierGUI = new SupplierGUI(primaryStage, supplierDatabase);
                supplierGUI.show(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to open Supplier GUI: " + e.getMessage());
            }
        });

        btnItem.setOnAction(_ -> {
            ItemDatabase itemDatabase = new ItemDatabase();
            ItemGUI itemGUI = new ItemGUI(primaryStage, itemDatabase);
            itemGUI.show(primaryStage);
        });

        btnHospitals.setOnAction(_ -> {
            HospitalDatabase hospitalDatabase = new HospitalDatabase();
            HospitalGUI hospitalGUI = new HospitalGUI(primaryStage, hospitalDatabase);
            hospitalGUI.show(primaryStage);
        });

        btnDistribute.setOnAction(_ -> {
            TransactionGUI transactionGUI = new TransactionGUI(transactionDatabase, itemDatabase, hospitalDatabase,
                    supplierDatabase);
            transactionGUI.showTransactionPopup(primaryStage);
        });

        btnReports.setOnAction(_ -> {
            try {
                // Use the existing transactionDatabase instance instead of creating a new one
                ReportGUI reportGUI = new ReportGUI(primaryStage, this.transactionDatabase);
                reportGUI.show(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to open Reports: " + e.getMessage());
            }
        });

        btnUsers.setOnAction(_ -> {
            UserDatabase userDatabase = new UserDatabase();
            UserGUI userGUI = new UserGUI(primaryStage, userDatabase);
            userGUI.show(primaryStage);
        });

        btnLogout.setOnAction(_ -> {
            LoginGUI loginGUI = new LoginGUI();
            try {
                SessionManager.clearSession();
                loginGUI.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Create scene with auto-resizing 100% screen size
        Scene scene = new Scene(root, width, height);

        // Add CSS stylesheet (optional, for additional styling)
        // scene.getStylesheets().add(getClass().getResource("/styles/modern-style.css").toExternalForm());

        // Stage setup
        primaryStage.setTitle("Inventory Management System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // Helper method to create styled regular buttons
    private Button createStyledButton(String text, String color, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: " + UPPER_TEXT_COLOR + ";" + // Changed to use light color for visibility
                        "-fx-content-display: top;" +
                        "-fx-alignment: center;" +
                        "-fx-background-radius: 10px;");

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", -10%);" +
                        "-fx-text-fill: " + UPPER_TEXT_COLOR + ";" + // Keep text visible on hover
                        "-fx-content-display: top;" +
                        "-fx-alignment: center;" +
                        "-fx-background-radius: 10px;"));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: " + UPPER_TEXT_COLOR + ";" +
                        "-fx-content-display: top;" +
                        "-fx-alignment: center;" +
                        "-fx-background-radius: 10px;"));

        return button;
    }

    // Helper method to create larger menu buttons with icons
    private Button createMenuButton(String text, String iconPath) {
        Button button = new Button(text);
        button.setPrefSize(180, 180);
        button.setWrapText(true);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #2e2e3e;" + // Ensure it's always dark gray
                        "-fx-content-display: top;" +
                        "-fx-alignment: center;" +
                        "-fx-background-radius: 10px;");

        // Try to load icon
        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitHeight(64);
            icon.setFitWidth(64);
            button.setGraphic(icon);
        } catch (Exception e) {
            // Use a text-based icon as fallback
            Label iconText = new Label(getTextIconFallback(text));
            iconText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
            iconText.setTextFill(Color.web(PRIMARY_COLOR));
            button.setGraphic(iconText);
        }

        // Add hover effect
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + PRIMARY_COLOR + ", 80%);" +
                        "-fx-text-fill: #2e2e3e;" + // Keep dark gray on hover
                        "-fx-content-display: top;" +
                        "-fx-alignment: center;" +
                        "-fx-background-radius: 10px;"));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #2e2e3e;" + // Keep dark gray when not hovering
                        "-fx-content-display: top;" +
                        "-fx-alignment: center;" +
                        "-fx-background-radius: 10px;"));

        return button;
    }

    // Helper method to create card layout for menu buttons
    private VBox createMenuCard(Button button) {
        VBox card = new VBox(button);
        card.setAlignment(Pos.CENTER);

        // Add shadow effect to card
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(4);
        button.setEffect(shadow);

        return card;
    }

    // Helper method to set tooltips
    private void setTooltip(Button button, String tooltipText) {
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(Font.font("Segoe UI", 14));
        tooltip.setStyle(
                "-fx-background-color: " + SECONDARY_COLOR + ";" + // Dark tooltip background
                        "-fx-text-fill: " + UPPER_TEXT_COLOR + ";");
        button.setTooltip(tooltip);
    }

    // Helper to create text-based icon fallbacks
    private String getTextIconFallback(String buttonName) {
        if (buttonName.contains("Supplier"))
            return "🏭";
        if (buttonName.contains("Item"))
            return "📦";
        if (buttonName.contains("Hospital"))
            return "🏥";
        if (buttonName.contains("Distribute"))
            return "🚚";
        if (buttonName.contains("Reports"))
            return "📊";
        if (buttonName.contains("User"))
            return "👥";
        return "🔧";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}