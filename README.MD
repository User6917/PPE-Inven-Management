# Project Inventory Management System

## Overview
The **Project Inventory Management System** is a JavaFX-based application that enables users to manage inventory transactions, track suppliers, and monitor hospital item distribution efficiently.

## Features
- **User Management:** Add, modify, and manage user accounts with authentication.
- **Inventory Tracking:** Manage item stocks, transactions, and supplier distributions.
- **Transaction Logging:** Record all incoming and outgoing inventory transactions.
- **Report Generation:** Generate transaction reports based on a date range.
- **Access Control:** Restrict user access based on `isActive` status and `UserType`.

## Technologies Used
- **Java 23** (Latest version for compatibility)
- **JavaFX** (GUI framework)
- **Maven** (Dependency & build management)
- **JBCrypt** (Password encryption)
- **FXML** (For UI layout)
- **File-Based Database** (Persisting inventory data)

## Installation
1. Ensure **JDK 23** and **Maven** are installed.
2. Clone the repository:
   ```sh
    git clone https://github.com/User6917/PPE-Inven-Management.git

    Navigate to the project folder:

    cd inventory-management
    
    Build the project:
    
    `mvn clean install`
    
    Run the application:
    
        `mvn javafx:run`
        `mvn exec:java`

Usage

    Login Screen: Authenticate with credentials.
    Main Menu: Access different management modules.
    Transaction Management: Record inventory transactions.
    Supplier & Hospital Management: Track items received and distributed.
    User Management: Modify user details (restricted based on permissions).
    Reports: Generate transaction reports for analysis.
    Default Users credential: admin:abcd1234 | User:password | Cat:password 

## Contributors

- @User6917

- @WebSinker

License

This project is licensed under the MIT License.