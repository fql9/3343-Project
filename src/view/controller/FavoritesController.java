package view.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Item;
import service.FavoriteService;
import service.ItemService;
import service.UserService;
import view.util.DialogUtils;

import java.util.List;

/**
 * Favorites controller - manages user's favorite items
 */
public class FavoritesController {

    private BorderPane mainLayout;
    private FavoriteService favoriteService;
    private ItemService itemService;
    private VBox itemListContainer;
    
    public FavoritesController(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        this.favoriteService = new FavoriteService();
        this.itemService = new ItemService();
    }
    
    /**
     * Show favorites list view
     */
    public void showFavoritesView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Top bar
        HBox topBar = createTopBar();
        
        // Favorites list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");
        
        itemListContainer = new VBox(15);
        itemListContainer.setPadding(new Insets(10));
        scrollPane.setContent(itemListContainer);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().addAll(topBar, scrollPane);
        
        // Load favorites
        loadFavorites();
        
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
        
        Label titleLabel = new Label("My Favorites");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadFavorites());
        
        topBar.getChildren().addAll(titleLabel, spacer, refreshButton);
        
        return topBar;
    }
    
    /**
     * Load favorites list
     */
    private void loadFavorites() {
        itemListContainer.getChildren().clear();
        
        Long userId = UserService.getCurrentUser().getId();
        List<Item> items = favoriteService.getUserFavoriteItems(userId);
        
        if (items.isEmpty()) {
            Label emptyLabel = new Label("You haven't favorited any items yet");
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
        
        Label categoryLabel = new Label("Category: " + (item.getCategory() != null ? item.getCategory() : "Uncategorized"));
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        
        HBox metaBox = new HBox(10);
        metaBox.getChildren().addAll(statusLabel, categoryLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        
        infoBox.getChildren().addAll(titleLabel, descLabel, metaBox);
        
        // Price and actions
        VBox actionBox = new VBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label priceLabel = new Label("¥" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button detailButton = new Button("View Details");
        detailButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        detailButton.setOnAction(e -> showItemDetail(item));
        
        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        removeButton.setOnAction(e -> handleRemoveFavorite(item));
        
        buttonBox.getChildren().addAll(detailButton, removeButton);
        
        actionBox.getChildren().addAll(priceLabel, buttonBox);
        
        card.getChildren().addAll(infoBox, actionBox);
        
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
     * Handle remove favorite
     */
    private void handleRemoveFavorite(Item item) {
        boolean confirm = DialogUtils.showConfirm("Confirm Remove", 
            "Are you sure you want to remove \"" + item.getTitle() + "\" from favorites?");
        
        if (confirm) {
            favoriteService.removeFavorite(UserService.getCurrentUser().getId(), item.getId());
            DialogUtils.showSuccess("Removed from favorites");
            loadFavorites();
        }
    }
}

