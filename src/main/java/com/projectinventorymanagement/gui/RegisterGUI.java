package com.projectinventorymanagement.gui;

import com.projectinventorymanagement.database.UserDatabase;
import com.projectinventorymanagement.models.User;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class RegisterGUI extends Application {

    private final UserDatabase userDatabase = new UserDatabase(); // Instantiate UserDatabase

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Registration");

        // Main layout
        VBox registerBox = new VBox(12);
        registerBox.setPadding(new Insets(30, 40, 30, 40));
        registerBox.setAlignment(Pos.CENTER);
        registerBox.getStyleClass().add("register-box");

        // Title Label
        Label lblTitle = new Label("Register");
        lblTitle.getStyleClass().add("title");

        // Form Grid
        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(10);
        formGrid.setVgap(12);

        // Input Fields
        TextField txtName = createTextField("Enter your name");
        TextField txtUsername = createTextField("Enter your username");
        PasswordField txtPassword = createPasswordField("Enter your password");
        TextField txtEmail = createTextField("Enter your email");
        TextField txtPhone = createTextField("Enter your phone number");

        // Labels
        Label lblName = createLabel("Full Name:");
        Label lblUsername = createLabel("Username:");
        Label lblPassword = createLabel("Password:");
        Label lblEmail = createLabel("Email:");
        Label lblPhone = createLabel("Phone:");

        // Add labels & fields to Grid
        formGrid.add(lblName, 0, 0);
        formGrid.add(txtName, 1, 0);
        formGrid.add(lblUsername, 0, 1);
        formGrid.add(txtUsername, 1, 1);
        formGrid.add(lblPassword, 0, 2);
        formGrid.add(txtPassword, 1, 2);
        formGrid.add(lblEmail, 0, 3);
        formGrid.add(txtEmail, 1, 3);
        formGrid.add(lblPhone, 0, 4);
        formGrid.add(txtPhone, 1, 4);

        // Message Label
        Label lblMessage = new Label();

        // Buttons
        Button btnRegister = new Button("Register");
        Button btnBack = new Button("Back");

        btnRegister.getStyleClass().add("green-button");
        btnBack.getStyleClass().add("gray-button");

        // Button Layout
        HBox buttonRow = new HBox(20);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(btnRegister, btnBack);

        // Registration Action
        btnRegister.setOnAction(_ -> {
            String name = txtName.getText();
            String username = txtUsername.getText();
            String password = txtPassword.getText();
            String email = txtEmail.getText();
            String phone = txtPhone.getText();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                lblMessage.setText("All fields are required!");
                lblMessage.getStyleClass().add("error-text");
                return;
            }

            User newUser = new User(name, username, password, "Staff", email, phone, true);
            if (userDatabase.addUser(newUser)) { // Use instance method instead of static call
                lblMessage.setText("User registered successfully!");
                lblMessage.getStyleClass().add("success-text");
            } else {
                lblMessage.setText("Username already exists!");
                lblMessage.getStyleClass().add("error-text");
            }
        });

        // Back to Login
        btnBack.setOnAction(_ -> {
            LoginGUI loginGUI = new LoginGUI();
            try {
                loginGUI.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Layout
        registerBox.getChildren().addAll(lblTitle, formGrid, lblMessage, buttonRow);

        // Scene Setup
        Scene scene = new Scene(registerBox, 500, 550);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TextField createTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setMaxWidth(300);
        textField.setPrefHeight(40);
        textField.getStyleClass().add("text-field");
        return textField;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(prompt);
        passwordField.setMaxWidth(300);
        passwordField.setPrefHeight(40);
        passwordField.getStyleClass().add("password-field");
        return passwordField;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label");
        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}