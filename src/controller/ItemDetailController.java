package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Item;
import model.User;
import service.FavoriteService;
import service.ItemService;
import service.MessageService;
import service.UserService;
import util.DialogUtils;

/**
 * Item detail controller - displays item detailed information
 */
public class ItemDetailController {

    private BorderPane mainLayout;
    private Item item;
    private ItemService itemService;
    private FavoriteService favoriteService;
    private MessageService messageService;
    private UserService userService;
    private Button favoriteButton;
    
    public ItemDetailController(BorderPane mainLayout, Item item) {
        this.mainLayout = mainLayout;
        this.item = item;
        this.itemService = new ItemService();
        this.favoriteService = new FavoriteService();
        this.messageService = new MessageService();
        this.userService = new UserService();
    }
    
    /**
     * Show item detail view
     */
    public void showItemDetailView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #ecf0f1;");
        
        // Back button
        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        backButton.setOnAction(e -> goBack());
        
        // Item detail card
        VBox detailCard = new VBox(20);
        detailCard.setPadding(new Insets(30));
        detailCard.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        detailCard.setMaxWidth(800);
        
        // Title and price
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        Label categoryLabel = new Label("Category: " + (item.getCategory() != null ? item.getCategory() : "Uncategorized"));
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        titleBox.getChildren().addAll(titleLabel, categoryLabel);
        
        Label priceLabel = new Label("¥" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        headerBox.getChildren().addAll(titleBox, priceLabel);
        
        // Description
        VBox descBox = new VBox(10);
        Label descTitle = new Label("Item Description");
        descTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label(item.getDescription() != null ? item.getDescription() : "No description available");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        descBox.getChildren().addAll(descTitle, descLabel);
        
        // Seller info
        User seller = userService.getUserById(item.getSellerId());
        VBox sellerBox = new VBox(10);
        Label sellerTitle = new Label("Seller Information");
        sellerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label sellerLabel = new Label("Seller: " + (seller != null ? seller.getUsername() : "Unknown"));
        sellerLabel.setStyle("-fx-font-size: 14px;");
        
        sellerBox.getChildren().addAll(sellerTitle, sellerLabel);
        
        // Other info
        Label timeLabel = new Label("Posted: " + item.getCreatedTime());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        
        // Action buttons
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER);
        
        Long currentUserId = UserService.getCurrentUser().getId();
        
        // If not own item
        if (!item.getSellerId().equals(currentUserId)) {
            // Favorite button
            boolean isFavorite = favoriteService.isFavorite(currentUserId, item.getId());
            favoriteButton = new Button(isFavorite ? "Favorited" : "Add to Favorites");
            favoriteButton.setStyle("-fx-background-color: " + (isFavorite ? "#95a5a6" : "#e67e22") + 
                                   "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
            favoriteButton.setOnAction(e -> handleFavorite());
            
            // Contact seller button
            Button contactButton = new Button("Contact Seller");
            contactButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                                  "-fx-font-size: 14px; -fx-padding: 10 30;");
            contactButton.setOnAction(e -> handleContactSeller());
            
            actionBox.getChildren().addAll(favoriteButton, contactButton);
        } else {
            Label ownLabel = new Label("This is your published item");
            ownLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            actionBox.getChildren().add(ownLabel);
        }
        
        detailCard.getChildren().addAll(headerBox, new Separator(), descBox, 
                                        new Separator(), sellerBox, timeLabel, 
                                        new Separator(), actionBox);
        
        root.getChildren().addAll(backButton, detailCard);
        
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    /**
     * Handle favorite/unfavorite
     */
    private void handleFavorite() {
        Long userId = UserService.getCurrentUser().getId();
        
        if (favoriteService.isFavorite(userId, item.getId())) {
            favoriteService.removeFavorite(userId, item.getId());
            favoriteButton.setText("Add to Favorites");
            favoriteButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; " +
                                   "-fx-font-size: 14px; -fx-padding: 10 30;");
            DialogUtils.showSuccess("Removed from favorites");
        } else {
            String error = favoriteService.addFavorite(userId, item.getId());
            if (error != null) {
                DialogUtils.showError("Favorite Failed", error);
            } else {
                favoriteButton.setText("Favorited");
                favoriteButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                                       "-fx-font-size: 14px; -fx-padding: 10 30;");
                DialogUtils.showSuccess("Added to favorites");
            }
        }
    }
    
    /**
     * Handle contact seller
     */
    private void handleContactSeller() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Contact Seller");
        dialog.setHeaderText("Send a message to the seller");
        dialog.setContentText("Message:");
        
        dialog.showAndWait().ifPresent(message -> {
            if (message.trim().isEmpty()) {
                DialogUtils.showWarning("Input Error", "Message cannot be empty");
                return;
            }
            
            String error = messageService.sendMessage(
                UserService.getCurrentUser().getId(),
                item.getSellerId(),
                "About item \"" + item.getTitle() + "\": " + message
            );
            
            if (error != null) {
                DialogUtils.showError("Send Failed", error);
            } else {
                DialogUtils.showSuccess("Message sent successfully");
            }
        });
    }
    
    /**
     * Go back to item list
     */
    private void goBack() {
        BoardController boardController = new BoardController(mainLayout);
        boardController.showBoardView();
    }
}
