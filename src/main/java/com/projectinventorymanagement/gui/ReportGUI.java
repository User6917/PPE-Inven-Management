package com.projectinventorymanagement.gui;

import javafx.stage.Stage;
import com.projectinventorymanagement.database.TransactionDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.FileChooser;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.projectinventorymanagement.models.Transaction;

public class ReportGUI extends TableGUI {

    private DatePicker dpStart;
    private DatePicker dpEnd;
    private TabPane tabPane;
    private Tab tableTab;
    private Tab chartTab;
    private ComboBox<String> exportFormatComboBox;

    public ReportGUI(Stage stage, TransactionDatabase transactionDatabase) {
        super(stage, transactionDatabase);
        setupDatePickers();
        setupTabs();
        setTableStyle();
        loadData(); // Add this line
    }

    private void setupDatePickers() {
        dpStart = new DatePicker(LocalDate.now().minusWeeks(1));
        dpEnd = new DatePicker(LocalDate.now());
    }

    private void setupTabs() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        // Table Tab
        tableTab = new Tab("Table View");
        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        tableTab.setContent(scrollPane);

        // Chart Tab
        chartTab = new Tab("Chart View");
        VBox chartContainer = new VBox(10);
        chartContainer.setAlignment(Pos.CENTER);

        // Add a tab selection listener
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == chartTab) {
                // When switching to chart tab, ensure charts reflect current data
                updateCharts();
            }
        });

        // Initialize pie chart for transaction types
        PieChart pieChart = createTransactionTypePieChart();
        pieChart.setPrefSize(600, 400);

        // Initialize bar chart for quantities
        BarChart<String, Number> barChart = createQuantityBarChart();
        barChart.setPrefSize(600, 400);

        chartContainer.getChildren().addAll(pieChart, barChart);
        chartTab.setContent(chartContainer);

        tabPane.getTabs().addAll(tableTab, chartTab);
    }

    @Override
    protected String getDataSource() {
        return "transactions";
    }

    private void setTableStyle() {
        tableView.setStyle("-fx-table-cell-border-color: transparent; -fx-background-color: white;");
        tableView.getStyleClass().add("modern-table");
    }

    @Override
    protected BorderPane createLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f0f8ff, #e6f2ff);");

        // Create a header with title
        Text title = new Text("Transaction Reports");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setFill(Color.web("#2c3e50"));

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        headerBox.getChildren().add(title);

        // Create TabPane for different views
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("floating-tabs");

        // Table Tab
        tableTab = new Tab("Table View");

        // Remove the conflicting prefHeight setting
        // tableView.setPrefHeight(400); // Remove this line

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("edge-to-edge");

        VBox tableContent = new VBox(10, scrollPane);
        tableContent.prefHeightProperty().bind(primaryStage.heightProperty().multiply(0.7));
        tableTab.setContent(tableContent);

        // Chart Tab
        chartTab = new Tab("Chart View");
        StackPane chartPane = new StackPane();
        chartPane.setPadding(new Insets(20));
        chartPane.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        chartTab.setContent(chartPane);

        tabPane.getTabs().addAll(tableTab, chartTab);

        // Create filter section and action buttons
        VBox filterSection = createFilterSection();
        HBox actionButtons = createActionButtons();

        // Main layout structure
        VBox centerContent = new VBox(15, filterSection, tabPane, actionButtons);
        centerContent.setAlignment(Pos.TOP_CENTER);

        root.setTop(headerBox);
        root.setCenter(centerContent);

        return root;
    }

    private VBox createFilterSection() {
        // Create modern search box
        TextField searchField = new TextField();
        searchField.setPromptText("Search transactions...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");

        Button btnSearch = new Button("Search");
        styleButton(btnSearch, "#3498db");
        btnSearch.setOnAction(_ -> filterTable(searchField.getText()));

        HBox searchBox = new HBox(10, searchField, btnSearch);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Remove the quick filter buttons and HBox
        // ...quickFilterBox code removed...

        // Combine remaining filter components (without quickFilterBox)
        VBox filterContainer = new VBox(15, searchBox);
        return filterContainer;
    }

    private HBox createActionButtons() {
        // Print Report button with better styling
        Button btnPrintReport = new Button("Generate Report");
        styleButton(btnPrintReport, "#27ae60");
        btnPrintReport.setGraphic(createButtonIcon("/images/print_icon.png"));
        btnPrintReport.setOnAction(_ -> printReport());

        // Export format selector
        Label exportLabel = new Label("Export As:");
        exportFormatComboBox = new ComboBox<>();
        exportFormatComboBox.getItems().addAll("TXT", "CSV", "PDF", "Excel");
        exportFormatComboBox.setValue("TXT");

        // Back button
        Button btnBack = new Button("Back");
        styleButton(btnBack, "#7f8c8d");
        btnBack.setOnAction(_ -> goBack(primaryStage));

        // Refresh button
        Button btnRefresh = new Button("Refresh");
        styleButton(btnRefresh, "#3498db");
        btnRefresh.setOnAction(_ -> {
            // Ensure we're reloading data from the database
            if (database instanceof TransactionDatabase) {
                ((TransactionDatabase) database).reloadData();
            }

            // Clear the current observable data and reload it
            observableData.clear();
            loadData();

            // Make sure the table view refreshes with the new data
            tableView.setItems(observableData);
            tableView.refresh();

            // Update the charts with the refreshed data
            updateCharts();
        });

        // Delete button (preserving existing functionality)
        btnDelete = new Button("Delete");
        styleButton(btnDelete, "#e74c3c");
        btnDelete.setOnAction(_ -> deleteSelectedRow());
        btnDelete.setDisable(true);

        enableDisableButton = new Button("Enable/Disable");
        styleButton(enableDisableButton, "#f39c12");
        enableDisableButton.setDisable(true);
        enableDisableButton.setOnAction(_ -> toggleEnableDisable());

        HBox exportBox = new HBox(10, exportLabel, exportFormatComboBox);
        exportBox.setAlignment(Pos.CENTER);

        HBox buttonContainer = new HBox(15, btnPrintReport, exportBox, btnRefresh, btnDelete, enableDisableButton,
                btnBack);
        buttonContainer.setAlignment(Pos.CENTER);

        return buttonContainer;
    }

    private void styleButton(Button button, String color) {
        button.setPrefWidth(120);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;");

        // Add hover effect
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", 20%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"));
    }

    private ImageView createButtonIcon(String path) {
        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(path)));
            icon.setFitHeight(16);
            icon.setFitWidth(16);
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void loadData() {
        if (database instanceof TransactionDatabase) {
            ((TransactionDatabase) database).reloadData(); // <-- Add this method to reload from file
        }
        super.loadData(); // This populates observableData
        updateCharts();
    }

    private void updateCharts() {
        if (chartTab != null) {
            VBox chartContainer = new VBox(20); // Increased spacing between charts
            chartContainer.setAlignment(Pos.CENTER);
            chartContainer.setPadding(new Insets(20));

            if (observableData.isEmpty()) {
                Label noDataLabel = new Label("No transactions found for the selected date range");
                noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-font-weight: bold;");

                // Add date range info
                Label dateRangeLabel = new Label(
                        String.format("Date Range: %s to %s",
                                dpStart.getValue().toString(),
                                dpEnd.getValue().toString()));
                dateRangeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

                VBox messageBox = new VBox(10, noDataLabel, dateRangeLabel);
                messageBox.setAlignment(Pos.CENTER);
                chartContainer.getChildren().add(messageBox);
            } else {
                // Add date range header
                Label dateRangeLabel = new Label(
                        String.format("Showing transactions from %s to %s",
                                dpStart.getValue().toString(),
                                dpEnd.getValue().toString()));
                dateRangeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                // Create charts
                PieChart pieChart = createTransactionTypePieChart();
                pieChart.setPrefSize(600, 400);

                BarChart<String, Number> barChart = createQuantityBarChart();
                barChart.setPrefSize(600, 400);

                chartContainer.getChildren().addAll(dateRangeLabel, pieChart, barChart);
            }

            chartTab.setContent(chartContainer);
        }
    }

    private PieChart createTransactionTypePieChart() {
        // Count transactions by type (Receive vs Distribute)
        Map<String, Integer> typeCounts = new HashMap<>();

        for (ObservableList<String> row : observableData) {
            if (row.size() > 3) {
                String type = row.get(3);
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
            }
        }

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Transaction Types");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);

        // Add tooltips to pie chart slices
        for (final PieChart.Data data : chart.getData()) {
            Tooltip tooltip = new Tooltip(String.format(
                    "%s: %d transactions (%.1f%%)",
                    data.getName(),
                    (int) data.getPieValue(),
                    (data.getPieValue() / pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum()) * 100));
            Tooltip.install(data.getNode(), tooltip);

            // Optional: Add hover effect
            data.getNode().setOnMouseEntered(e -> data.getNode().setStyle("-fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
            data.getNode().setOnMouseExited(e -> data.getNode().setStyle("-fx-scale-x: 1; -fx-scale-y: 1;"));
        }

        return chart;
    }

    private BarChart<String, Number> createQuantityBarChart() {
        // Sum quantities by item code
        Map<String, Integer> itemQuantities = new HashMap<>();

        for (ObservableList<String> row : observableData) {
            if (row.size() > 4) {
                String itemCode = row.get(1);
                int quantity;
                try {
                    quantity = Integer.parseInt(row.get(4));
                } catch (NumberFormatException e) {
                    quantity = 0;
                }
                itemQuantities.put(itemCode, itemQuantities.getOrDefault(itemCode, 0) + quantity);
            }
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Item Code");
        yAxis.setLabel("Total Quantity");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Quantity by Item Code");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Items");

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
        }

        barChart.getData().add(series);

        // Add tooltips to bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip(String.format(
                    "Item Code: %s\nTotal Quantity: %d",
                    data.getXValue(),
                    data.getYValue().intValue()));
            Tooltip.install(data.getNode(), tooltip);

            // Optional: Add hover effect
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle("-fx-background-color: derive(-fx-bar-fill, -10%);");
                tooltip.show(data.getNode(), e.getScreenX() + 10, e.getScreenY() + 10);
            });
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setStyle("");
                tooltip.hide();
            });
        }

        return barChart;
    }

    /**
     * Enhanced report generation with format selection and file chooser
     */
    private void printReport() {
        LocalDate startDate = dpStart.getValue();
        LocalDate endDate = dpEnd.getValue();

        if (startDate == null || endDate == null) {
            showModernAlert("Error", "Please select both start and end dates.", Alert.AlertType.ERROR);
            return;
        }

        if (endDate.isBefore(startDate)) {
            showModernAlert("Error", "End date cannot be before start date.", Alert.AlertType.ERROR);
            return;
        }

        ObservableList<ObservableList<String>> filteredData = FXCollections.observableArrayList();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ObservableList<String> row : observableData) {
            if (row.size() > 5) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(row.get(5), dtf);
                    LocalDate date = dateTime.toLocalDate();
                    if ((date.isEqual(startDate) || date.isAfter(startDate)) &&
                            (date.isEqual(endDate) || date.isBefore(endDate))) {
                        filteredData.add(row);
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing date: " + row.get(5));
                }
            }
        }

        // Let user select where to save the report
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");

        // Set extension based on chosen format
        String formatExtension = ".txt";
        String formatName = exportFormatComboBox.getValue();

        switch (formatName) {
            case "CSV":
                formatExtension = ".csv";
                break;
            case "PDF":
                formatExtension = ".pdf";
                break;
            case "Excel":
                formatExtension = ".xlsx";
                break;
        }

        fileChooser.setInitialFileName("transactions_report" + formatExtension);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(formatName + " Files", "*" + formatExtension));

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                generateReportFile(filteredData, file.getAbsolutePath(), formatName);
                showModernAlert("Success",
                        "Report has been generated in " + file.getName() +
                                "\nTotal transactions: " + filteredData.size(),
                        Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showModernAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void generateReportFile(ObservableList<ObservableList<String>> filteredData, String filePath,
            String format) {
        try {
            switch (format) {
                case "TXT":
                case "CSV":
                    // Common code for text-based formats
                    String delimiter = format.equals("CSV") ? "," : "\t";
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                        // Write report header with timestamp and summary
                        writer.write("Transaction Report");
                        writer.newLine();
                        writer.write("Generated on: "
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        writer.newLine();
                        writer.write("Date Range: " + dpStart.getValue() + " to " + dpEnd.getValue());
                        writer.newLine();
                        writer.write("Total Transactions: " + filteredData.size());
                        writer.newLine();
                        writer.newLine();

                        // Write header line
                        writer.write("TransactionID" + delimiter + "Item Code" + delimiter + "Code" + delimiter +
                                "Details" + delimiter + "Quantity" + delimiter + "Date-Time");
                        writer.newLine();

                        // Write each filtered transaction row.
                        for (ObservableList<String> row : filteredData) {
                            String line = String.join(delimiter, row);
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                    break;

                case "PDF":
                case "Excel":
                    // For PDF and Excel, in a real implementation we would use libraries like
                    // Apache POI for Excel or iText for PDF. Since we don't have those libraries
                    // imported, we'll just create a CSV file as a fallback
                    showModernAlert("Format Notice",
                            "To generate " + format
                                    + " files, you would need to add libraries like Apache POI (Excel) or iText (PDF).\n"
                                    +
                                    "Creating a CSV file instead.",
                            Alert.AlertType.INFORMATION);

                    generateReportFile(filteredData, filePath.replace(format.toLowerCase(), "csv"), "CSV");
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    protected void showModernAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Try to add styling only if the resource exists
        try {
            DialogPane dialogPane = alert.getDialogPane();
            if (getClass().getResource("inventorymanagement\\src\\main\\resources\\modern-alert.css") != null) {
                dialogPane.getStylesheets().add(getClass()
                        .getResource("inventorymanagement\\src\\main\\resources\\modern-alert.css").toExternalForm());
            }
        } catch (Exception e) {
            // Silently continue if the CSS file isn't available
        }

        alert.showAndWait();
    }
}