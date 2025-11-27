package view.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import model.Item;
import service.ItemService;
import service.UserService;
import util.ValidationUtils;
import view.util.DialogUtils;

import java.io.File;
import java.util.List;

/**
 * My items controller - manages user's published items
 */
public class MyItemsController {

    private BorderPane mainLayout;
    private ItemService itemService;
    private VBox itemListContainer;
    
    public MyItemsController(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        this.itemService = new ItemService();
    }
    
    /**
     * Show my items view
     */
    public void showMyItemsView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Top bar
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
        loadMyItems();
        
        mainLayout.setCenter(root);
    }
    
    /**
     * Create top bar
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label titleLabel = new Label("My Items");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadMyItems());
        
        Button publishButton = new Button("+ Publish New Item");
        publishButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px;");
        publishButton.setOnAction(e -> showPublishDialog());
        
        topBar.getChildren().addAll(titleLabel, spacer, refreshButton, publishButton);
        
        return topBar;
    }
    
    /**
     * Load my items
     */
    private void loadMyItems() {
        itemListContainer.getChildren().clear();
        
        Long userId = UserService.getCurrentUser().getId();
        List<Item> items = itemService.getItemsBySeller(userId);
        
        if (items.isEmpty()) {
            Label emptyLabel = new Label("You haven't published any items yet");
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
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
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
                // Failed to load image
            }
        }
        
        // Item info
        VBox infoBox = new VBox(10);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label descLabel = new Label(item.getDescription() != null ? item.getDescription() : "No description");
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
        descLabel.setWrapText(true);
        
        Label statusLabel = new Label("Status: " + (item.isActive() ? "Active" : "Inactive"));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: " + 
                            (item.isActive() ? "#2ecc71" : "#e74c3c") + "; -fx-padding: 3 8; -fx-background-radius: 10;");
        
        Label timeLabel = new Label("Posted: " + item.getCreatedTime());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #bdc3c7;");
        
        HBox metaBox = new HBox(10);
        metaBox.getChildren().addAll(statusLabel, timeLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        
        infoBox.getChildren().addAll(titleLabel, descLabel, metaBox);
        
        // Price and actions
        VBox actionBox = new VBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label priceLabel = new Label("¥" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        editButton.setOnAction(e -> showEditDialog(item));
        
        Button toggleButton = new Button(item.isActive() ? "Deactivate" : "Activate");
        toggleButton.setStyle("-fx-background-color: " + (item.isActive() ? "#e67e22" : "#2ecc71") + 
                             "; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        toggleButton.setOnAction(e -> handleToggleActive(item));
        
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDelete(item));
        
        buttonBox.getChildren().addAll(editButton, toggleButton, deleteButton);
        
        actionBox.getChildren().addAll(priceLabel, buttonBox);
        
        card.getChildren().addAll(imageView, infoBox, actionBox);
        
        return card;
    }
    
    /**
     * Show publish item dialog
     */
    private void showPublishDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
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
        titleField.setPrefWidth(300);
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Item description");
        descArea.setPrefRowCount(4);
        descArea.setPrefWidth(300);
        
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
            if (result == publishButtonType) {
                handlePublish(titleField.getText(), descArea.getText(), 
                            priceField.getText(), categoryBox.getValue(), imageUrlField.getText());
            }
        });
    }
    
    /**
     * Show edit item dialog
     */
    private void showEditDialog(Item item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Modify item information");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField titleField = new TextField(item.getTitle());
        titleField.setPrefWidth(300);
        
        TextArea descArea = new TextArea(item.getDescription());
        descArea.setPrefRowCount(4);
        descArea.setPrefWidth(300);
        
        TextField priceField = new TextField(String.valueOf(item.getPrice()));
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Electronics", "Books", "Clothing", "Furniture", "Other");
        categoryBox.setValue(item.getCategory() != null ? item.getCategory() : "Other");

        // Image input
        TextField imageUrlField = new TextField(item.getImageUrl() != null ? item.getImageUrl() : "");
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
            if (result == saveButtonType) {
                handleEdit(item, titleField.getText(), descArea.getText(), 
                         priceField.getText(), categoryBox.getValue(), imageUrlField.getText());
            }
        });
    }
    
    /**
     * Handle publish item
     */
    private void handlePublish(String title, String description, String priceText, String category, String imageUrl) {
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
            loadMyItems();
        }
    }
    
    /**
     * Handle edit item
     */
    private void handleEdit(Item item, String title, String description, String priceText, String category, String imageUrl) {
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
        
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setImageUrl(imageUrl);
        
        String error = itemService.updateItem(item);
        if (error != null) {
            DialogUtils.showError("Update Failed", error);
        } else {
            DialogUtils.showSuccess("Item updated successfully!");
            loadMyItems();
        }
    }
    
    /**
     * Handle activate/deactivate
     */
    private void handleToggleActive(Item item) {
        if (item.isActive()) {
            boolean confirm = DialogUtils.showConfirm("Confirm Deactivate", "Are you sure you want to deactivate this item?");
            if (confirm) {
                itemService.deactivateItem(item.getId());
                DialogUtils.showSuccess("Item deactivated");
                loadMyItems();
            }
        } else {
            itemService.activateItem(item.getId());
            DialogUtils.showSuccess("Item activated");
            loadMyItems();
        }
    }
    
    /**
     * Handle delete
     */
    private void handleDelete(Item item) {
        boolean confirm = DialogUtils.showConfirm("Confirm Delete", 
            "Are you sure you want to delete item \"" + item.getTitle() + "\"? This action cannot be undone!");
        
        if (confirm) {
            itemService.deleteItem(item.getId());
            DialogUtils.showSuccess("Item deleted");
            loadMyItems();
        }
    }
}

