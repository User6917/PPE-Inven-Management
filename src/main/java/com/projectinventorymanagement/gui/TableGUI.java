package com.projectinventorymanagement.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.chart.PieChart;
import javafx.geometry.Rectangle2D;
import javafx.application.Platform;
import javafx.scene.Node;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.projectinventorymanagement.database.DatabaseBase;

public abstract class TableGUI {

    // Existing fields
    protected ObservableList<ObservableList<String>> observableData;
    protected ArrayList<String> headers;
    protected TableView<ObservableList<String>> tableView;
    protected Stage primaryStage;
    protected Button enableDisableButton;
    protected Button btnDelete;
    protected DatabaseBase database;

    // New fields for modern design
    protected VBox actionPane;
    protected StackPane overlayContainer;
    protected BorderPane root;
    protected String cssPath = "/modern-table.css";
    protected String currentTheme = LIGHT_THEME;
    protected Map<String, Double> columnWidths = new HashMap<>();

    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
    private static final String LIGHT_THEME = "light-theme";
    private static final String DARK_THEME = "dark-theme";

    // Add this field at the top with other fields
    protected PieChart chart;

    public TableGUI(Stage primaryStage, DatabaseBase database) {
        this.primaryStage = primaryStage;
        this.database = database;
        this.observableData = FXCollections.observableArrayList();
        this.headers = new ArrayList<>();

        // Initialize UI components
        this.tableView = new TableView<>();
        this.enableDisableButton = new Button("Toggle");
        this.btnDelete = new Button("Delete");
        this.actionPane = new VBox(10);
        this.root = new BorderPane();
        this.root.getStyleClass().addAll("root", currentTheme);
        this.overlayContainer = new StackPane();
    }

    protected abstract String getDataSource();

    public void show(Stage primaryStage) {
        try {
            // Get screen dimensions
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            // Initialize components first
            setupGUI();

            // Load data after components are ready
            loadData();

            // Create scene with initialized components
            BorderPane layout = createLayout();
            Scene scene = new Scene(layout);

            // Add CSS styling
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());

            // Set stage properties to maximize properly
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());
            primaryStage.setMaximized(true);

            // Add window resize listener
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                layout.setPrefWidth(newVal.doubleValue());
                double padding = 40; // Total horizontal padding
                double contentWidth = newVal.doubleValue() - padding;
                double tableWidth = contentWidth * 0.6; // 60% for table
                double chartWidth = contentWidth * 0.4; // 40% for chart

                // Update column widths
                if (!headers.isEmpty()) {
                    double columnWidth = tableWidth / headers.size();
                    tableView.getColumns().forEach(col -> {
                        col.setPrefWidth(columnWidth);
                    });
                }
            });

            primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                layout.setPrefHeight(newVal.doubleValue());
            });

            // Show the stage
            primaryStage.setScene(scene);
            primaryStage.show();

            // Force initial layout update
            layout.applyCss();
            layout.layout();

            // Add entrance animation after showing
            animateEntrance();
        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert("Error", "Failed to initialize GUI: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void animateEntrance() {
        // Fade in animation for the table
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), tableView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Slide in animation for the action buttons
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), actionPane);
        slideIn.setFromY(50);
        slideIn.setToY(0);
        slideIn.setDelay(Duration.millis(400));
        slideIn.play();
    }

    protected void setupGUI() {
        // Initialize buttons first
        enableDisableButton = createIconButton("Toggle", "/icons/toggle.png");
        btnDelete = createIconButton("Delete", "/icons/delete.png");

        tableView = new TableView<>();
        tableView.setItems(observableData);
        tableView.setPrefHeight(Screen.getPrimary().getBounds().getHeight() / 2);
        // Set table properties
        tableView.setFixedCellSize(40);
        tableView.prefHeightProperty().bind(primaryStage.heightProperty().divide(2));
        // Ensure table uses full width
        tableView.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.6));

        tableView.setFixedCellSize(40); // Consistent row height
        tableView.prefHeightProperty().bind(primaryStage.heightProperty().divide(2));

        // Initialize action pane
        actionPane = new VBox(10);
        actionPane.setPadding(new Insets(10));
        actionPane.setAlignment(Pos.CENTER);

        // Initialize root container
        root = new BorderPane();
        root.getStyleClass().add(currentTheme);

        // Create columns and setup listeners
        createColumns();
        setupRowSelectionListener();

        // Set initial button states
        enableDisableButton.setOnAction(event -> toggleEnableDisable());
        enableDisableButton.setDisable(true);

        btnDelete.setOnAction(event -> deleteSelectedRow());
        btnDelete.setDisable(true);

        // Apply modern styling
        tableView.getStyleClass().add("modern-table");

        // Add hover effect to rows
        tableView.setRowFactory(tv -> {
            TableRow<ObservableList<String>> row = new TableRow<>();

            // Add hover effect
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.getStyleClass().add("highlighted-row");
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), row);
                    st.setToX(1.01);
                    st.setToY(1.01);
                    st.play();
                }
            });

            row.setOnMouseExited(event -> {
                row.getStyleClass().remove("highlighted-row");
                ScaleTransition st = new ScaleTransition(Duration.millis(150), row);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            return row;
        });
    }

    private void setupRowSelectionListener() {
        tableView.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) -> {
            updateEnableDisableButton();
            updateDeleteButton();

            // Add selection animation
            if (oldValue != null && newValue != null) {
                int newIndex = observableData.indexOf(newValue);
                if (newIndex >= 0) {
                    TableRow<?> row = getRowByIndex(tableView, newIndex);
                    if (row != null) {
                        flashRowSelection(row);
                    }
                }
            }
        });
    }

    private TableRow<?> getRowByIndex(TableView<?> tableView, int index) {
        for (int i = 0; i < tableView.lookupAll(".table-row-cell").size(); i++) {
            TableRow<?> row = (TableRow<?>) tableView.lookupAll(".table-row-cell").toArray()[i];
            if (row.getIndex() == index) {
                return row;
            }
        }
        return null;
    }

    private void flashRowSelection(TableRow<?> row) {
        // Apply a quick highlight flash effect when selecting a row
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), row);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    protected void loadData() {
        HashMap<Integer, ArrayList<String>> data = database.getEntries();
        if (data == null || data.isEmpty()) {
            return;
        }

        // Clear existing data
        observableData.clear();
        headers.clear();

        // Load headers (from row 0)
        if (data.containsKey(0)) {
            headers.addAll(data.get(0));
        }

        // Load data rows (starting from row 1)
        for (int row = 1; row < data.size(); row++) {
            if (data.containsKey(row)) {
                observableData.add(FXCollections.observableArrayList(data.get(row)));
            }
        }

        // Sort the data initially by first column
        FXCollections.sort(observableData, (row1, row2) -> {
            if (row1.isEmpty() || row2.isEmpty()) {
                return 0;
            }
            // Compare first column values
            return row1.get(0).compareTo(row2.get(0));
        });

        // Create columns after loading data
        createColumns();

        // Restore column widths
        tableView.getColumns().forEach(col -> {
            if (columnWidths.containsKey(col.getText())) {
                col.setPrefWidth(columnWidths.get(col.getText()));
            }
        });

        // Update chart with new data
        Platform.runLater(this::updateChart);
    }

    private void createColumns() {
        tableView.getColumns().clear();

        for (int col = 0; col < headers.size(); col++) {
            final int columnIndex = col;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(headers.get(col));

            // Style the header
            column.getStyleClass().add("modern-column");

            // Set fixed width based on number of columns
            double columnWidth = primaryStage.getWidth() / headers.size();
            column.setPrefWidth(columnWidth);
            column.setMinWidth(100); // Minimum width to ensure readability

            // Prevent column resizing if you want fixed widths
            column.setResizable(false);

            // Set cell value factory
            column.setCellValueFactory(cellData -> {
                if (cellData.getValue().size() > columnIndex) {
                    return new javafx.beans.property.SimpleStringProperty(cellData.getValue().get(columnIndex));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            // Set cell factory for custom styling
            column.setCellFactory(tc -> new TableCell<ObservableList<String>, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);

                        // Apply specific styling for isActive column
                        getStyleClass().removeAll("status-active", "status-inactive");
                        if (columnIndex < headers.size() && "isActive".equals(headers.get(columnIndex))) {
                            if ("true".equalsIgnoreCase(item)) {
                                getStyleClass().add("status-active");
                            } else if ("false".equalsIgnoreCase(item)) {
                                getStyleClass().add("status-inactive");
                            }
                        }
                    }
                }
            });

            // Add column to table
            tableView.getColumns().add(column);
        }

        // Force table refresh
        tableView.refresh();
    }

    protected BorderPane createLayout() {
        if (tableView == null || enableDisableButton == null || btnDelete == null) {
            throw new IllegalStateException("UI components not properly initialized");
        }

        // Create main container
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().addAll("root", currentTheme);
        root = mainLayout; // Set the root field

        // Create left section for table and controls
        VBox tableSection = new VBox(10);
        tableSection.setPrefWidth(primaryStage.getWidth() * 0.6); // 60% of window width
        tableSection.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.6));

        // Create table container with shadow effect
        StackPane tableContainer = new StackPane();
        tableContainer.getStyleClass().add("table-container");

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.prefHeightProperty().bind(primaryStage.heightProperty().multiply(0.8));
        scrollPane.prefWidthProperty().bind(tableSection.widthProperty());
        scrollPane.getStyleClass().add("modern-scroll");

        // Set minimum width for the table
        tableView.setMinWidth(500);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Force the table to use available width
        tableView.prefWidthProperty().bind(scrollPane.widthProperty().multiply(0.98));

        // Add shadow effect to the table container
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setRadius(10);
        scrollPane.setEffect(dropShadow);

        tableContainer.getChildren().add(scrollPane);

        // Create controls section below table
        VBox controlsSection = new VBox(10);
        controlsSection.setPadding(new Insets(10));
        controlsSection.getStyleClass().add("controls-section");

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("search-container");

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("modern-search");

        // Add search field animations
        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), searchField);
                st.setToX(1.02);
                st.setToY(1.02);
                st.play();
            } else {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), searchField);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            }
        });

        Button btnSearch = createIconButton("", "/icons/search.png");
        btnSearch.getStyleClass().add("search-button");
        btnSearch.setOnAction(_ -> filterTable(searchField.getText()));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));
        searchBox.getChildren().addAll(searchField, btnSearch);
        searchBox.setAlignment(Pos.CENTER);

        // Buttons
        HBox buttonRow = new HBox(15);
        Button btnAdd = createIconButton("Add", "/icons/add.png");
        btnAdd.setOnAction(_ -> {
            showOverlayEffect();
            DynamicAdd formPopup = new DynamicAdd(database, primaryStage); // Pass primaryStage here
            formPopup.showForm();
            Stage popupStage = formPopup.getStage();

            // Add handlers for both close and hide events
            popupStage.setOnHidden(event -> {
                hideOverlayEffect();
                loadData(); // Refresh data after form closes
                updateChart(); // Update chart with new data
            });

            popupStage.setOnCloseRequest(event -> {
                hideOverlayEffect();
            });
        });

        Button btnRefresh = createIconButton("Refresh", "/icons/refresh.png");
        btnRefresh.setOnAction(_ -> {
            animateRefresh(btnRefresh);

            // Sort the observableData by first column
            FXCollections.sort(observableData, (row1, row2) -> {
                if (row1.isEmpty() || row2.isEmpty()) {
                    return 0;
                }
                // Compare first column values
                return row1.get(0).compareTo(row2.get(0));
            });

            // Refresh the view
            tableView.refresh();
            updateChart();
        });

        buttonRow.getChildren().addAll(
                btnAdd,
                btnDelete,
                enableDisableButton,
                btnRefresh);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getStyleClass().add("button-container");

        controlsSection.getChildren().addAll(searchBox, buttonRow);

        // Add all components to table section
        tableSection.getChildren().addAll(tableContainer, controlsSection);

        // Create right section for chart
        VBox chartSection = new VBox(10);
        chartSection.setPrefWidth(primaryStage.getWidth() * 0.4); // 40% of window width
        chartSection.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.4));
        chartSection.getStyleClass().add("chart-section");

        // Create chart (example with PieChart - modify according to your needs)
        PieChart chart = createChart();
        chart.prefHeightProperty().bind(primaryStage.heightProperty().multiply(0.8));

        // Theme toggle and back button at top of chart section
        HBox chartControls = new HBox(10);
        Button btnToggleTheme = createIconButton("Theme", "/icons/theme.png");
        btnToggleTheme.setOnAction(_ -> toggleTheme());

        Button btnBack = createIconButton("Back", "/icons/back.png");
        btnBack.setOnAction(_ -> goBack(primaryStage));

        chartControls.getChildren().addAll(btnToggleTheme, btnBack);
        chartControls.setAlignment(Pos.CENTER_RIGHT);

        chartSection.getChildren().addAll(chartControls, chart);
        chartSection.setPadding(new Insets(20));

        // Add sections to main layout
        HBox contentSection = new HBox(20);
        contentSection.getChildren().addAll(tableSection, chartSection);
        contentSection.setPadding(new Insets(20));

        // Bind content section width to window width
        contentSection.prefWidthProperty().bind(primaryStage.widthProperty().subtract(40)); // Subtract padding

        // Add header
        Label header = new Label(capitalizeFirst(getDataSource()) + " Management");
        header.getStyleClass().addAll("table-header", currentTheme);
        HBox headerBox = new HBox(header);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(15, 0, 5, 0));
        headerBox.getStyleClass().add(currentTheme);

        // Setup main layout
        mainLayout.setTop(headerBox);
        mainLayout.setCenter(contentSection);
        mainLayout.getStyleClass().addAll("root", currentTheme);

        // Add theme class to content sections
        contentSection.getStyleClass().add(currentTheme);
        tableSection.getStyleClass().add(currentTheme);
        chartSection.getStyleClass().add(currentTheme);

        // Create overlay container
        overlayContainer = new StackPane();
        overlayContainer.setVisible(false);

        StackPane finalStack = new StackPane(mainLayout, overlayContainer);
        finalStack.getStyleClass().addAll("root", currentTheme);

        BorderPane container = new BorderPane(finalStack);
        container.getStyleClass().addAll("root", currentTheme);

        return container;
    }

    private PieChart createChart() {
        chart = new PieChart(); // Use the class field instead of local variable
        chart.setTitle(getDataSource() + " Statistics");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.getStyleClass().add("modern-chart");

        // Set better defaults for visualization
        chart.setLabelsVisible(true);
        chart.setLabelLineLength(20);
        chart.setStartAngle(90);

        // Set size constraints
        chart.setMinSize(300, 300);
        chart.setPrefSize(400, 400);

        return chart;
    }

    protected void updateChart() {
        if (chart != null && !observableData.isEmpty()) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            String dataSource = getDataSource().toLowerCase();

            switch (dataSource) {
                case "user":
                    // Show distribution of user types
                    int userTypeIndex = headers.indexOf("UserType");
                    int isActiveIndex = headers.indexOf("isActive");

                    if (userTypeIndex != -1) {
                        Map<String, Long> userTypes = observableData.stream()
                                .map(row -> row.get(userTypeIndex))
                                .collect(Collectors.groupingBy(
                                        type -> type == null ? "Unspecified" : type,
                                        Collectors.counting()));

                        userTypes.forEach((type, count) -> pieChartData.add(new PieChart.Data(
                                type + " Users (" + count + ")",
                                count)));
                        chart.setTitle("User Distribution by Type");
                    } else if (isActiveIndex != -1) {
                        // Fallback to active/inactive distribution if UserType not found
                        long activeUsers = observableData.stream()
                                .filter(row -> "true".equalsIgnoreCase(row.get(isActiveIndex)))
                                .count();
                        long inactiveUsers = observableData.stream()
                                .filter(row -> "false".equalsIgnoreCase(row.get(isActiveIndex)))
                                .count();
                        long defaultUsers = observableData.stream()
                                .filter(row -> "-".equals(row.get(isActiveIndex)))
                                .count();

                        if (activeUsers > 0) {
                            pieChartData.add(new PieChart.Data("Active Users (" + activeUsers + ")", activeUsers));
                        }
                        if (inactiveUsers > 0) {
                            pieChartData
                                    .add(new PieChart.Data("Inactive Users (" + inactiveUsers + ")", inactiveUsers));
                        }
                        if (defaultUsers > 0) {
                            pieChartData.add(new PieChart.Data("Default Users (" + defaultUsers + ")", defaultUsers));
                        }
                        chart.setTitle("User Status Distribution");
                    }
                    break;

                case "supplier":
                    // Group by Supplier Name and count their items
                    int nameIndex = headers.indexOf("Supplier Name");
                    int itemIndex = headers.indexOf("Item Code");

                    if (nameIndex != -1 && itemIndex != -1) {
                        Map<String, Long> supplierItems = observableData.stream()
                                .collect(Collectors.groupingBy(
                                        row -> row.get(nameIndex),
                                        Collectors.counting()));

                        supplierItems.forEach((name, count) -> pieChartData.add(new PieChart.Data(
                                name + " (" + count + " items)",
                                count)));
                        chart.setTitle("Items Distribution by Supplier");
                    }
                    break;

                case "ppe":
                    // Show quantity distribution of PPE items
                    int nameIdxPPE = headers.indexOf("Item Name");
                    int qtyIndex = headers.indexOf("Quantity (Boxes)");

                    if (nameIdxPPE != -1 && qtyIndex != -1) {
                        Map<String, Integer> itemQuantities = new HashMap<>();

                        for (ObservableList<String> row : observableData) {
                            String itemName = row.get(nameIdxPPE);
                            int qty = Integer.parseInt(row.get(qtyIndex));
                            itemQuantities.merge(itemName, qty, Integer::sum);
                        }

                        itemQuantities.forEach((item, qty) -> pieChartData.add(new PieChart.Data(
                                item + " (" + qty + " boxes)",
                                qty)));
                        chart.setTitle("PPE Items Distribution");
                    }
                    break;

                case "hospital":
                    // Show distribution of items received by hospitals
                    int hospitalIndex = headers.indexOf("Hospital Name");
                    int totalBoxesIndex = headers.indexOf("Items Recieved (Total Boxes)");

                    if (hospitalIndex != -1 && totalBoxesIndex != -1) {
                        for (ObservableList<String> row : observableData) {
                            String hospitalName = row.get(hospitalIndex);
                            int boxes = Integer.parseInt(row.get(totalBoxesIndex));
                            pieChartData.add(new PieChart.Data(
                                    hospitalName + " (" + boxes + " boxes)",
                                    boxes > 0 ? boxes : 1 // Use 1 as minimum to show hospitals with no items
                            ));
                        }
                        chart.setTitle("Items Distribution Across Hospitals");
                    }
                    break;
            }

            // Update chart data
            Platform.runLater(() -> {
                chart.setData(pieChartData);

                // Set theme class
                chart.getStyleClass().removeAll("chart-text-light", "chart-text-dark");
                chart.getStyleClass().add(DARK_THEME.equals(currentTheme) ? "chart-text-dark" : "chart-text-light");

                // Add tooltips and effects to chart pieces
                chart.getData().forEach(data -> {
                    // Calculate percentage
                    double total = pieChartData.stream()
                            .mapToDouble(PieChart.Data::getPieValue)
                            .sum();
                    double percentage = (data.getPieValue() / total) * 100;

                    // Create tooltip
                    Tooltip tooltip = new Tooltip(String.format(
                            "%s\n%.1f%%",
                            data.getName(),
                            percentage));
                    Tooltip.install(data.getNode(), tooltip);

                    // Add hover effects
                    data.getNode().setOnMouseEntered(e -> {
                        data.getNode().setEffect(new DropShadow(10, Color.GRAY));
                        ScaleTransition st = new ScaleTransition(Duration.millis(200), data.getNode());
                        st.setToX(1.1);
                        st.setToY(1.1);
                        st.play();
                    });

                    data.getNode().setOnMouseExited(e -> {
                        data.getNode().setEffect(null);
                        ScaleTransition st = new ScaleTransition(Duration.millis(200), data.getNode());
                        st.setToX(1.0);
                        st.setToY(1.0);
                        st.play();
                    });

                    // Set text color based on current theme
                    if (DARK_THEME.equals(currentTheme)) {
                        // Update all text elements to white for dark theme
                        chart.lookupAll(".chart-title")
                                .forEach(title -> title.setStyle("-fx-text-fill: white;"));
                        chart.lookupAll(".chart-legend")
                                .forEach(legend -> legend.setStyle("-fx-text-fill: white;"));
                        chart.lookupAll(".chart-pie-label")
                                .forEach(label -> label.setStyle("-fx-fill: white;"));
                        chart.lookupAll(".chart-legend-item-text")
                                .forEach(text -> text.setStyle("-fx-fill: white;"));
                        chart.lookupAll(".chart-pie-label-line")
                                .forEach(line -> line.setStyle("-fx-stroke: white;"));
                    }
                });

                // Force theme update
                chart.lookupAll(".chart-pie-label")
                        .forEach(node -> {
                            node.getStyleClass().removeAll("chart-text-light", "chart-text-dark");
                            node.getStyleClass()
                                    .add(DARK_THEME.equals(currentTheme) ? "chart-text-dark" : "chart-text-light");
                        });

                chart.lookupAll(".chart-legend-item-text")
                        .forEach(node -> {
                            node.getStyleClass().removeAll("chart-text-light", "chart-text-dark");
                            node.getStyleClass()
                                    .add(DARK_THEME.equals(currentTheme) ? "chart-text-dark" : "chart-text-light");
                        });

                chart.lookupAll(".chart-title")
                        .forEach(node -> {
                            node.getStyleClass().removeAll("chart-text-light", "chart-text-dark");
                            node.getStyleClass()
                                    .add(DARK_THEME.equals(currentTheme) ? "chart-text-dark" : "chart-text-light");
                        });

                // Disable animation temporarily
                chart.setAnimated(false);

                // Apply theme styles
                if (DARK_THEME.equals(currentTheme)) {
                    Platform.runLater(() -> {
                        applyDarkThemeToChart(chart);

                        // Re-apply styles to data nodes after a short delay
                        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                                Duration.millis(100));
                        delay.setOnFinished(event -> applyDarkThemeToChart(chart));
                        delay.play();
                    });
                }

                // Re-enable animation
                chart.setAnimated(true);
            });
        }
    }

    // Add this helper method
    private void applyDarkThemeToChart(PieChart chart) {
        chart.setStyle("-fx-background-color: #2d2d2d;");

        // Function to apply white text style
        Runnable applyWhiteText = () -> {
            // Update title
            chart.lookupAll(".chart-title")
                    .forEach(node -> node.setStyle("-fx-text-fill: white !important; -fx-font-size: 14px;"));

            // Update legend
            chart.lookupAll(".chart-legend-item-text")
                    .forEach(node -> node.setStyle("-fx-fill: white !important; -fx-text-fill: white !important;"));

            // Update pie labels
            chart.lookupAll(".chart-pie-label")
                    .forEach(node -> node.setStyle("-fx-fill: white !important; -fx-text-fill: white !important;"));

            // Update pie label lines
            chart.lookupAll(".chart-pie-label-line").forEach(node -> node.setStyle("-fx-stroke: white !important;"));

            // Update data nodes
            chart.getData().forEach(data -> {
                Node node = data.getNode();
                if (node != null) {
                    String pieColor = node.getStyle().replaceAll(".*-fx-pie-color: ([^;]+).*", "$1");
                    node.setStyle("-fx-pie-color: " + pieColor + "; -fx-text-fill: white !important;");
                }
            });
        };

        // Initial application
        Platform.runLater(applyWhiteText);

        // Add listeners to each data item to maintain white text
        chart.getData().forEach(data -> {
            data.nameProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(applyWhiteText);
            });
        });

        // Add a listener to the chart's data property
        chart.dataProperty().addListener((obs, oldData, newData) -> {
            if (newData != null) {
                Platform.runLater(() -> {
                    // Wait for JavaFX to update the nodes
                    PauseTransition delay = new PauseTransition(Duration.millis(100));
                    delay.setOnFinished(e -> applyWhiteText.run());
                    delay.play();
                });
            }
        });

        // Set up a periodic refresh to ensure styles persist
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (DARK_THEME.equals(currentTheme)) {
                Platform.runLater(applyWhiteText);
            }
        }));
        timeline.setCycleCount(3); // Run 3 times to ensure styles are applied
        timeline.play();
    }

    protected Button createIconButton(String text, String iconPath) {
        Button button = new Button(text);
        button.getStyleClass().add("modern-button");

        try {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            button.setGraphic(imageView);
        } catch (Exception e) {
            // If icon can't be loaded, just use text
            System.out.println("Could not load icon: " + iconPath);
        }

        // Add hover animation
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return button;
    }

    private void animateRefresh(Button button) {
        // Rotate animation for refresh button
        javafx.animation.RotateTransition rotateTransition = new javafx.animation.RotateTransition(Duration.millis(750),
                button);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.play();
    }

    protected void toggleTheme() {
        // Toggle theme variable
        currentTheme = LIGHT_THEME.equals(currentTheme) ? DARK_THEME : LIGHT_THEME;

        // Update root and scene
        if (root != null && root.getScene() != null) {
            Scene scene = root.getScene();

            // Update scene root
            scene.getRoot().getStyleClass().removeAll(LIGHT_THEME, DARK_THEME);
            scene.getRoot().getStyleClass().add(currentTheme);

            // Update all nodes
            updateThemeRecursively(scene.getRoot());

            // Update chart theme
            Platform.runLater(() -> {
                if (chart != null) {
                    if (LIGHT_THEME.equals(currentTheme)) {
                        // Light theme chart updates
                        applyLightThemeToChart(chart);

                        // Update chart title
                        chart.lookupAll(".chart-title").forEach(
                                node -> node.setStyle("-fx-text-fill: black !important; -fx-font-size: 14px;"));

                        // Update legend items
                        chart.lookupAll(".chart-legend-item-text").forEach(
                                node -> node.setStyle("-fx-fill: black !important; -fx-text-fill: black !important;"));

                        // Update pie labels
                        chart.lookupAll(".chart-pie-label").forEach(
                                node -> node.setStyle("-fx-fill: black !important; -fx-text-fill: black !important;"));

                        // Update pie label lines
                        chart.lookupAll(".chart-pie-label-line")
                                .forEach(node -> node.setStyle("-fx-stroke: black !important;"));

                    } else {
                        // Dark theme chart updates
                        applyDarkThemeToChart(chart);
                    }

                    // Force refresh chart
                    PauseTransition delay = new PauseTransition(Duration.millis(100));
                    delay.setOnFinished(event -> {
                        if (LIGHT_THEME.equals(currentTheme)) {
                            // Reapply light theme styles after delay
                            chart.lookupAll(".chart-title").forEach(
                                    node -> node.setStyle("-fx-text-fill: black !important; -fx-font-size: 14px;"));
                            chart.lookupAll(".chart-legend-item-text").forEach(node -> node
                                    .setStyle("-fx-fill: black !important; -fx-text-fill: black !important;"));
                            chart.lookupAll(".chart-pie-label").forEach(node -> node
                                    .setStyle("-fx-fill: black !important; -fx-text-fill: black !important;"));
                        }
                    });
                    delay.play();
                }
            });

            // Force CSS refresh
            Platform.runLater(() -> {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                scene.getRoot().applyCss();
                scene.getRoot().layout();
            });
        }
    }

    private void applyLightThemeToChart(PieChart chart) {
        chart.setStyle("-fx-background-color: white;");

        Runnable applyBlackText = () -> {
            chart.lookupAll(".chart-title")
                    .forEach(node -> node.setStyle("-fx-text-fill: black !important; -fx-font-size: 14px;"));
            chart.lookupAll(".chart-legend-item-text")
                    .forEach(node -> node.setStyle("-fx-fill: black !important; -fx-text-fill: black !important;"));
            chart.lookupAll(".chart-pie-label")
                    .forEach(node -> node.setStyle("-fx-fill: black !important; -fx-text-fill: black !important;"));
            chart.lookupAll(".chart-pie-label-line")
                    .forEach(node -> node.setStyle("-fx-stroke: black !important;"));

            chart.getData().forEach(data -> {
                Node node = data.getNode();
                if (node != null) {
                    String pieColor = node.getStyle().replaceAll(".*-fx-pie-color: ([^;]+).*", "$1");
                    node.setStyle("-fx-pie-color: " + pieColor + "; -fx-text-fill: black !important;");
                }
            });
        };

        Platform.runLater(applyBlackText);

        chart.getData().forEach(data -> {
            data.nameProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(applyBlackText);
            });
        });

        chart.dataProperty().addListener((obs, oldData, newData) -> {
            if (newData != null) {
                Platform.runLater(() -> {
                    PauseTransition delay = new PauseTransition(Duration.millis(100));
                    delay.setOnFinished(e -> applyBlackText.run());
                    delay.play();
                });
            }
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (LIGHT_THEME.equals(currentTheme)) {
                Platform.runLater(applyBlackText);
            }
        }));
        timeline.setCycleCount(3);
        timeline.play();
    }

    private void updateThemeRecursively(javafx.scene.Node node) {
        if (node == null)
            return;

        // Remove old theme classes and add new one
        node.getStyleClass().removeAll(LIGHT_THEME, DARK_THEME);
        node.getStyleClass().add(currentTheme);

        // Special handling for text-based components
        if (node instanceof Labeled) {
            Labeled labeled = (Labeled) node;
            if (!(labeled instanceof Button)) { // Skip buttons
                labeled.getStyleClass().add(currentTheme);
                if (DARK_THEME.equals(currentTheme)) {
                    labeled.setTextFill(javafx.scene.paint.Color.WHITE);
                } else {
                    labeled.setTextFill(javafx.scene.paint.Color.BLACK);
                }
            }
        }

        // Special handling for TableView
        if (node instanceof TableView) {
            TableView<?> table = (TableView<?>) node;
            table.refresh();

            // Update column headers text color and keep background
            table.getColumns().forEach(column -> {
                if (column instanceof TableColumn) {
                    ((TableColumn) column).setStyle(
                            DARK_THEME.equals(currentTheme)
                                    ? "-fx-text-fill: white; -fx-background-color: #2d2d2d; -fx-border-color: white;"
                                    : "-fx-text-fill: black; -fx-background-color: white; -fx-border-color: #e3e6f0;");
                }
            });

            // Update table cells text color while keeping the table structure visible
            table.setStyle(
                    DARK_THEME.equals(currentTheme)
                            ? "-fx-text-fill: white; -fx-control-inner-background: #2d2d2d; -fx-table-cell-border-color: white;"
                            : "-fx-text-fill: black; -fx-control-inner-background: white; -fx-table-cell-border-color: #e3e6f0;");
        }
        // Special handling for PieChart
        else if (node instanceof PieChart) {
            updateChart(); // Still useful if the data needs refreshing
            PieChart pieChart = (PieChart) node;

            Platform.runLater(() -> {
                try {
                    if (DARK_THEME.equals(currentTheme)) {
                        applyDarkThemeToChart(pieChart);
                    } else {
                        applyLightThemeToChart(pieChart);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating chart theme: " + e.getMessage());
                }
            });
        }

        // Recursively update children
        if (node instanceof Parent) {
            for (javafx.scene.Node child : ((Parent) node).getChildrenUnmodifiable()) {
                updateThemeRecursively(child);
            }
        }
    }

    private void showOverlayEffect() {
        // Add blur effect to the main content
        GaussianBlur blur = new GaussianBlur(5);
        root.setEffect(blur);

        // Show semi-transparent overlay
        overlayContainer.setVisible(true);
        overlayContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlayContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void hideOverlayEffect() {
        if (overlayContainer != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(_ -> {
                overlayContainer.setVisible(false);
                overlayContainer.setStyle("");
                if (root != null) {
                    root.setEffect(null);
                }
            });
            fadeOut.play();
        }
    }

    protected void deleteSelectedRow() {
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showModernAlert("No Selection", "Please select a row to delete.", Alert.AlertType.WARNING);
            return;
        }

        // Create confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Deletion");
        dialog.setHeaderText("Are you sure you want to delete this record?");

        // Set buttons
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButton, cancelButton);

        // Show the dialog and process the result
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == deleteButton) {
            int selectedIndex = tableView.getSelectionModel().getSelectedIndex();

            // Create exit animation for the row
            TableRow<?> row = getRowByIndex(tableView, selectedIndex);
            if (row != null) {
                FadeTransition fade = new FadeTransition(Duration.millis(300), row);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);

                TranslateTransition translate = new TranslateTransition(Duration.millis(300), row);
                translate.setByX(100);

                fade.setOnFinished(_ -> {
                    observableData.remove(selectedRow);
                    tableView.refresh();

                    // Remove from database (adjust index since headers are at row 0)
                    database.deleteEntry(selectedIndex + 1);
                    database.saveData(); // Ensure database update
                });

                fade.play();
                translate.play();
            } else {
                // Fallback if animation fails
                observableData.remove(selectedRow);
                tableView.refresh();
                database.deleteEntry(selectedIndex + 1);
                database.saveData();
            }
        }
    }

    protected void toggleEnableDisable() {
        int isActiveIndex = headers.indexOf("isActive");
        if (isActiveIndex == -1)
            return;

        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();
        if (selectedRow != null) {
            boolean isEnabled = Boolean.parseBoolean(selectedRow.get(isActiveIndex));
            selectedRow.set(isActiveIndex, String.valueOf(!isEnabled)); // Toggle value

            // Update the corresponding row in the database
            int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                HashMap<Integer, ArrayList<String>> dbData = database.getEntries();
                if (dbData.containsKey(selectedIndex + 1)) {
                    dbData.get(selectedIndex + 1).set(isActiveIndex, String.valueOf(!isEnabled));
                }
                database.saveData(); // Ensure changes persist
            }

            enableDisableButton.setText(isEnabled ? "Enable" : "Disable");

            // 🔥 **Force an immediate update of the Delete button!**
            updateDeleteButton();

            // Add toggle animation
            TableRow<?> row = getRowByIndex(tableView, selectedIndex);
            if (row != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), row);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.7);
                fadeOut.setCycleCount(2);
                fadeOut.setAutoReverse(true);
                fadeOut.play();
            }

            tableView.refresh();
        }
    }

    protected void updateEnableDisableButton() {
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null || headers.isEmpty()) {
            enableDisableButton.setDisable(true);
            enableDisableButton.setText("Toggle");
            return;
        }

        int isActiveColumnIndex = headers.indexOf("isActive");
        if (isActiveColumnIndex == -1) {
            enableDisableButton.setDisable(true);
            enableDisableButton.setText("Toggle");
            return;
        }

        String currentValue = selectedRow.get(isActiveColumnIndex);

        // Check if value is "-" (disabled state)
        if ("-".equals(currentValue)) {
            enableDisableButton.setDisable(true);
            enableDisableButton.setText("Toggle");
            return;
        }

        enableDisableButton.setDisable(false);
        enableDisableButton.setText("true".equalsIgnoreCase(currentValue) ? "Disable" : "Enable");
    }

    protected void filterTable(String query) {
        if (query == null || query.trim().isEmpty()) {
            tableView.setItems(observableData);
            return;
        }

        ObservableList<ObservableList<String>> filteredData = FXCollections.observableArrayList();
        for (ObservableList<String> row : observableData) {
            for (String cell : row) {
                if (cell.toLowerCase().contains(query.toLowerCase())) {
                    filteredData.add(row);
                    break;
                }
            }
        }

        // Create a fade transition when filtering
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), tableView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.7);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), tableView);
        fadeIn.setFromValue(0.7);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(_ -> {
            tableView.setItems(filteredData);
            fadeIn.play();
        });

        fadeOut.play();
    }

    protected void updateDeleteButton() {
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();

        if (selectedRow == null || headers.isEmpty()) {
            btnDelete.setDisable(true);
            return;
        }

        int isActiveIndex = headers.indexOf("isActive");
        if (isActiveIndex == -1) {
            btnDelete.setDisable(true);
            return;
        }

        String isActiveValue = selectedRow.get(isActiveIndex);

        // 🔥 **Immediately enable or disable delete button based on isActive**
        btnDelete.setDisable(!"false".equalsIgnoreCase(isActiveValue));
    }

    protected void saveData() {
        HashMap<Integer, ArrayList<String>> dataToSave = new HashMap<>();
        dataToSave.put(0, new ArrayList<>(headers));
        int rowIndex = 1; // <- Row 0 is headers
        for (ObservableList<String> row : observableData) {
            dataToSave.put(rowIndex, new ArrayList<>(row));
            rowIndex++;
        }
        // Update the database's boundList and save.
        database.boundList = dataToSave;
        database.saveData();
    }
    // First Letter Capitalizer
    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    protected void showModernAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Apply modern styling
        alert.getDialogPane().getStyleClass().add("modern-alert");

        // Add animation
        alert.setOnShowing(e -> {
            alert.getDialogPane().setScaleX(0.8);
            alert.getDialogPane().setScaleY(0.8);
            alert.getDialogPane().setOpacity(0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(200), alert.getDialogPane());
            scale.setToX(1.0);
            scale.setToY(1.0);

            FadeTransition fade = new FadeTransition(Duration.millis(200), alert.getDialogPane());
            fade.setToValue(1.0);

            scale.play();
            fade.play();
        });

        // Show blur overlay
        showOverlayEffect();

        // Show the alert and hide overlay when closed
        alert.showAndWait();
        hideOverlayEffect();
    }

    protected void showAlert(String title, String content) {
        showModernAlert(title, content, Alert.AlertType.WARNING);
    }

    protected void goBack(Stage primaryStage) {
        try {
            // Add exit animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(_ -> {
                try {
                    MainMenuGUI mainMenu = new MainMenuGUI();
                    mainMenu.start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            fadeOut.play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}