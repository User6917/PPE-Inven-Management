
---

### **README.txt**
```txt
PROJECT INVENTORY MANAGEMENT SYSTEM
====================================

Overview:
---------
The Project Inventory Management System is a JavaFX-based application designed for tracking inventory transactions, managing suppliers, and monitoring distribution to hospitals.

Features:
---------
- User Management (Add, Modify, Authenticate Users)
- Inventory Tracking (Item Stocks & Supplier Management)
- Transaction Logging (Record Incoming & Outgoing Stock)
- Report Generation (Date-Based Analysis)
- Access Control (Restrict User Permissions)

Technology Stack:
-----------------
- Java 23
- JavaFX (GUI)
- Maven (Dependency Management)
- JBCrypt (Secure Password Storage)
- File-Based Database (CSV-like persistence)

Installation Guide:
-------------------
1. Install **JDK 23** and **Maven**.
2. Clone the repository: `git clone https://github.com/User6917/PPE-Inven-Management.git`
3. Navigate to the project directory: `cd inventory-management`
4. Build the project: `mvn clean install`
5. Run the application: `mvn javafx:run`
6. Alternatively Run the application: `mvn exec:java`
7. Default Users credential: admin:abcd1234 | User:password | Cat:password 

How to Use:
-----------
- **Login Screen:** Enter credentials to access the system.
- **Main Menu:** Navigate between different management sections.
- **Transactions:** Add inventory records (Distribute/Receive).
- **Suppliers & Hospitals:** Track inventory movement.
- **User Management:** Modify user details (restrictions apply).
- **Generate Reports:** Export transaction records.

Developer:
----------
- Wong Chee Soon (Lead Developer)
- Lee Yong Han

License:
--------
This project is licensed under the **MIT License**.