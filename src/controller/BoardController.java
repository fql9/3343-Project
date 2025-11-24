package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Item;
import service.ItemService;
import service.UserService;
import util.DialogUtils;
import util.ValidationUtils;

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
    private ComboBox<String> categoryComboBox;
    
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
        
        // Title and search bar
        HBox topBar = createTopBar();
        
        // Item list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");
        
        itemListContainer = new VBox(15);
        itemListContainer.setPadding(new Insets(10));
        scrollPane.setContent(itemListContainer);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().addAll(topBar, scrollPane);
        
        // Load items
        loadItems();
        
        mainLayout.setCenter(root);
    }
    
    /**
     * Create top bar (title, search, filter)
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label titleLabel = new Label("Item Market");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.setPrefWidth(300);
        
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        searchButton.setOnAction(e -> handleSearch());
        
        Label categoryLabel = new Label("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("All", "Electronics", "Books", "Clothing", "Furniture", "Other");
        categoryComboBox.setValue("All");
        categoryComboBox.setOnAction(e -> handleCategoryFilter());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadItems());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Show publish item button for all users (since everyone is a SELLER now)
        HBox rightButtons = new HBox(10);
        Button publishButton = new Button("+ Publish Item");
        publishButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px;");
        publishButton.setOnAction(e -> showPublishDialog());
        rightButtons.getChildren().add(publishButton);
        
        topBar.getChildren().addAll(titleLabel, searchField, searchButton, 
                                     categoryLabel, categoryComboBox, refreshButton, spacer, rightButtons);
        
        // Enter to search
        searchField.setOnAction(e -> handleSearch());
        
        return topBar;
    }
    
    /**
     * Load item list
     */
    private void loadItems() {
        itemListContainer.getChildren().clear();
        
        List<Item> items = itemService.getAllActiveItems();
        
        if (items.isEmpty()) {
            Label emptyLabel = new Label("No items available");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            itemListContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Item item : items) {
            itemListContainer.getChildren().add(createItemCard(item));
        }
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
     * Handle search
     */
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        itemListContainer.getChildren().clear();
        
        List<Item> items = itemService.searchItems(keyword);
        
        if (items.isEmpty()) {
            Label emptyLabel = new Label("No items found");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            itemListContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Item item : items) {
            itemListContainer.getChildren().add(createItemCard(item));
        }
    }
    
    /**
     * Handle category filter
     */
    private void handleCategoryFilter() {
        String category = categoryComboBox.getValue();
        itemListContainer.getChildren().clear();
        
        List<Item> items;
        if ("All".equals(category)) {
            items = itemService.getAllActiveItems();
        } else {
            items = itemService.getItemsByCategory(category);
        }
        
        if (items.isEmpty()) {
            Label emptyLabel = new Label("No items in this category");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            itemListContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Item item : items) {
            itemListContainer.getChildren().add(createItemCard(item));
        }
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
