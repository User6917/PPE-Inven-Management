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
import com.projectinventorymanagement.utils.SessionManager;

public class LoginGUI extends Application {
    
    private UserDatabase userDatabase;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        // Initialize UserDatabase
        userDatabase = new UserDatabase();

        // Container Layout
        BorderPane root = new BorderPane();
        VBox loginBox = new VBox(10);
        loginBox.setPadding(new Insets(20));
        loginBox.setAlignment(Pos.CENTER);
        loginBox.getStyleClass().add("login-box"); // CSS class

        // UI Components
        Label lblTitle = new Label("Login");
        lblTitle.getStyleClass().add("title");

        Label lblUsername = new Label("Username:");
        TextField txtUsername = new TextField();
        txtUsername.getStyleClass().add("text-field");

        Label lblPassword = new Label("Password:");
        PasswordField txtPassword = new PasswordField();
        txtPassword.getStyleClass().add("password-field");

        Label lblMessage = new Label();
        lblMessage.setVisible(false);

        // Buttons
        Button btnLogin = new Button("Login");
        Button btnRegister = new Button("Register");
        Button btnExit = new Button("Exit");

        // Button Layout (Grid for alignment)
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(btnLogin, btnRegister);

        VBox exitBox = new VBox();
        exitBox.setAlignment(Pos.CENTER);
        exitBox.getChildren().add(btnExit);

        // Login Action
        btnLogin.setOnAction(_ -> {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();

            User user = userDatabase.authenticate(username, password);
            if (user != null) {
                lblMessage.setText("Login successful! Welcome, " + user.getName());
                lblMessage.setVisible(true);
                try {
                    SessionManager.setCurrentUser(user);
                    new MainMenuGUI().start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                lblMessage.setText("Invalid credentials. Try again.");
                lblMessage.getStyleClass().add("error-text");
                lblMessage.setVisible(true);
            }
        });

        // Navigate to Registration Page
        btnRegister.setOnAction(_ -> {
            RegisterGUI registerGUI = new RegisterGUI();
            try {
                registerGUI.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Exit Application
        btnExit.getStyleClass().add("exit-button");
        btnExit.setOnAction(_ -> primaryStage.close());

        // Layout
        loginBox.getChildren().addAll(lblTitle, lblUsername, txtUsername, lblPassword, txtPassword, lblMessage, buttonRow, exitBox);
        root.setCenter(loginBox);

        // Scene Setup
        Scene scene = new Scene(root, 400, 350);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}