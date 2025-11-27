package view.controller;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Item;
import service.ItemService;
import service.UserService;
import util.ValidationUtils;
import view.util.DialogUtils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

/**
 * Board controller - displays and searches items
 */
public class BoardController {

    private BorderPane mainLayout;
    private ItemService itemService;
    private VBox itemListContainer;
    private TextField searchField;
    private TextField minPriceField;
    private TextField maxPriceField;
    private ComboBox<String> categoryComboBox;
    private ComboBox<String> sortBox;
    
    public BoardController(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        this.itemService = new ItemService();
    }
    
    /**
     * Show board view
     */
    public void showBoardView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f6fa;");
        
        // Top bar (Title + Publish)
        HBox topBar = createTopBar();
        
        // Search Section
        VBox searchSection = createSearchSection();
        
        // Item list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");
        
        itemListContainer = new VBox(15);
        itemListContainer.setPadding(new Insets(10));
        scrollPane.setContent(itemListContainer);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().addAll(topBar, searchSection, scrollPane);
        
        // Load items
        loadItems();
        
        mainLayout.setCenter(root);
    }
    
    /**
     * Create top bar (title, publish button)
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        
        Label titleLabel = new Label("Item Market");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshButton.setOnAction(e -> loadItems());
        
        Button publishButton = new Button("+ Publish Item");
        publishButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        publishButton.setOnAction(e -> showPublishDialog());
        
        topBar.getChildren().addAll(titleLabel, spacer, refreshButton, publishButton);
        
        return topBar;
    }

    /**
     * Create search section
     */
    private VBox createSearchSection() {
        VBox searchContainer = new VBox(15);
        searchContainer.setPadding(new Insets(20));
        searchContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Row 1: Keyword search
        HBox row1 = new HBox(15);
        row1.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        searchField = new TextField();
        searchField.setPromptText("Search by title or description...");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
        searchButton.setOnAction(e -> handleSearch());
        
        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
        clearButton.setOnAction(e -> clearSearch());

        row1.getChildren().addAll(searchLabel, searchField, searchButton, clearButton);

        // Row 2: Filters
        HBox row2 = new HBox(20);
        row2.setAlignment(Pos.CENTER_LEFT);

        // Price Range
        Label priceLabel = new Label("Price:");
        priceLabel.setStyle("-fx-font-weight: bold;");
        
        minPriceField = new TextField();
        minPriceField.setPromptText("Min");
        minPriceField.setPrefWidth(80);
        minPriceField.setStyle("-fx-padding: 5;");
        
        Label toLabel = new Label("-");
        
        maxPriceField = new TextField();
        maxPriceField.setPromptText("Max");
        maxPriceField.setPrefWidth(80);
        maxPriceField.setStyle("-fx-padding: 5;");

        // Category
        Label catLabel = new Label("Category:");
        catLabel.setStyle("-fx-font-weight: bold;");
        
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "All Categories", "Electronics", "Books", "Clothing", "Furniture", "Other"
        ));
        categoryComboBox.setValue("All Categories");
        categoryComboBox.setStyle("-fx-padding: 2;");

        // Sort
        Label sortLabel = new Label("Sort By:");
        sortLabel.setStyle("-fx-font-weight: bold;");
        
        sortBox = new ComboBox<>();
        sortBox.setItems(FXCollections.observableArrayList(
            "Newest First", "Price: Low to High", "Price: High to Low"
        ));
        sortBox.setValue("Newest First");
        sortBox.setStyle("-fx-padding: 2;");
        
        // Add listener to auto-search when sort/category changes
        sortBox.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        row2.getChildren().addAll(priceLabel, minPriceField, toLabel, maxPriceField, 
                                  new Separator(Orientation.VERTICAL),
                                  catLabel, categoryComboBox,
                                  new Separator(Orientation.VERTICAL),
                                  sortLabel, sortBox);

        searchContainer.getChildren().addAll(row1, new Separator(), row2);
        
        // Enter to search
        searchField.setOnAction(e -> handleSearch());
        
        return searchContainer;
    }
    
    /**
     * Load item list
     */
    private void loadItems() {
        handleSearch();
    }
    
    /**
     * Handle search
     */
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        String category = categoryComboBox.getValue();
        String sortBy = sortBox.getValue();
        
        Double minPrice = null;
        try {
            if (minPriceField.getText() != null && !minPriceField.getText().trim().isEmpty()) {
                minPrice = Double.parseDouble(minPriceField.getText().trim());
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
        
        Double maxPrice = null;
        try {
            if (maxPriceField.getText() != null && !maxPriceField.getText().trim().isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceField.getText().trim());
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
        
        itemListContainer.getChildren().clear();
        
        List<Item> items = itemService.searchItems(keyword, minPrice, maxPrice, category, sortBy);
        
        if (items.isEmpty()) {
            Label emptyLabel = new Label("No items found matching your criteria");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            itemListContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Item item : items) {
            itemListContainer.getChildren().add(createItemCard(item));
        }
    }
    
    /**
     * Clear search filters
     */
    private void clearSearch() {
        searchField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        categoryComboBox.setValue("All Categories");
        sortBox.setValue("Newest First");
        handleSearch();
    }
    /**
     * Create item card
     */
    private HBox createItemCard(Item item) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Image thumbnail
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                String imageUrl = item.getImageUrl();
                Image image;
                if (imageUrl.startsWith("http")) {
                    image = new Image(imageUrl, true);
                } else {
                    image = new Image(new File(imageUrl).toURI().toString());
                }
                imageView.setImage(image);
            } catch (Exception e) {
                // Failed to load image, keep empty or set placeholder
            }
        }
        
        // If no image loaded, set a placeholder color/text
        if (imageView.getImage() == null) {
            // Optional: set a placeholder image
        }

        // Item info
        VBox infoBox = new VBox(10);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label descLabel = new Label(item.getDescription() != null ? item.getDescription() : "No description");
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
        descLabel.setWrapText(true);
        
        Label categoryLabel = new Label(item.getCategory() != null ? item.getCategory() : "Uncategorized");
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: #95a5a6; -fx-padding: 3 8; -fx-background-radius: 10;");
        
        Label timeLabel = new Label("Posted: " + item.getCreatedTime());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #bdc3c7;");
        
        HBox metaBox = new HBox(10);
        metaBox.getChildren().addAll(categoryLabel, timeLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        
        infoBox.getChildren().addAll(titleLabel, descLabel, metaBox);
        
        // Price and actions
        VBox actionBox = new VBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label priceLabel = new Label("¥" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Button detailButton = new Button("View Details");
        detailButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        detailButton.setOnMouseEntered(e -> detailButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;"));
        detailButton.setOnMouseExited(e -> detailButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;"));
        detailButton.setOnAction(e -> showItemDetail(item));
        
        actionBox.getChildren().addAll(priceLabel, detailButton);
        
        card.getChildren().addAll(imageView, infoBox, actionBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                                                  "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        
        return card;
    }
    

    
    /**
     * Show item detail
     */
    private void showItemDetail(Item item) {
        ItemDetailController detailController = new ItemDetailController(mainLayout, item);
        detailController.showItemDetailView();
    }
    
    /**
     * Show publish item dialog
     */
    private void showPublishDialog() {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Publish Item");
        dialog.setHeaderText("Fill in item information");
        
        ButtonType publishButtonType = new ButtonType("Publish", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(publishButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Item title");
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Item description");
        descArea.setPrefRowCount(3);
        
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Electronics", "Books", "Clothing", "Furniture", "Other");
        categoryBox.setValue("Other");

        // Image input
        TextField imageUrlField = new TextField();
        imageUrlField.setPromptText("Image URL or File Path");
        
        Button fileButton = new Button("Choose File");
        fileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                imageUrlField.setText(selectedFile.getAbsolutePath());
            }
        });
        
        HBox imageBox = new HBox(10);
        imageBox.getChildren().addAll(imageUrlField, fileButton);
        HBox.setHgrow(imageUrlField, Priority.ALWAYS);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);
        grid.add(new Label("Image:"), 0, 4);
        grid.add(imageBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.showAndWait().ifPresent(result -> {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();
            String priceText = priceField.getText().trim();
            String category = categoryBox.getValue();
            String imageUrl = imageUrlField.getText().trim();
            
            if (!ValidationUtils.isNotEmpty(title)) {
                DialogUtils.showWarning("Input Error", "Item title cannot be empty");
                return;
            }
            
            double price;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                DialogUtils.showError("Input Error", "Price must be a number");
                return;
            }
            
            String error = itemService.publishItem(
                UserService.getCurrentUser().getId(),
                title, description, price, category, imageUrl
            );
            
            if (error != null) {
                DialogUtils.showError("Publish Failed", error);
            } else {
                DialogUtils.showSuccess("Item published successfully!");
                loadItems();
            }
        });
    }
}

