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
import service.TransactionService;
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
    private TransactionService transactionService;
    private UserService userService;
    private Button favoriteButton;
    
    public ItemDetailController(BorderPane mainLayout, Item item) {
        this.mainLayout = mainLayout;
        this.item = item;
        this.itemService = new ItemService();
        this.favoriteService = new FavoriteService();
        this.messageService = new MessageService();
        this.transactionService = new TransactionService();
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
            
            // Buy button - check if transaction exists
            model.Transaction activeTransaction = transactionService.getActiveTransaction(currentUserId, item.getId());
            Button buyButton;
            if (activeTransaction != null) {
                buyButton = new Button("View Transaction");
                buyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                  "-fx-font-size: 14px; -fx-padding: 10 30;");
                buyButton.setOnAction(e -> handleViewTransaction(activeTransaction));
            } else {
                buyButton = new Button("Buy Now");
                buyButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                                  "-fx-font-size: 14px; -fx-padding: 10 30;");
                buyButton.setOnAction(e -> handleBuy());
            }
            
            // Contact seller button
            Button contactButton = new Button("Contact Seller");
            contactButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                                  "-fx-font-size: 14px; -fx-padding: 10 30;");
            contactButton.setOnAction(e -> handleContactSeller());
            
            actionBox.getChildren().addAll(favoriteButton, buyButton, contactButton);
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
     * Handle buy item
     */
    private void handleBuy() {
        // Show dialog to confirm purchase or propose price
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Buy Item");
        dialog.setHeaderText("Confirm purchase or propose a price");
        
        ButtonType buyButtonType = new ButtonType("Buy", ButtonBar.ButtonData.OK_DONE);
        ButtonType proposeButtonType = new ButtonType("Propose Price", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(buyButtonType, proposeButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label priceLabel = new Label("Item Price: ¥" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TextField priceField = new TextField();
        priceField.setPromptText("Enter your proposed price (leave empty to use item price)");
        priceField.setText(String.format("%.2f", item.getPrice()));
        
        content.getChildren().addAll(priceLabel, new Label("Proposed Price:"), priceField);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == buyButtonType || buttonType == proposeButtonType) {
                return priceField.getText();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(priceText -> {
            try {
                Double proposedPrice = null;
                if (priceText != null && !priceText.trim().isEmpty()) {
                    proposedPrice = Double.parseDouble(priceText.trim());
                }
                
                String error = transactionService.initiateTransaction(
                    UserService.getCurrentUser().getId(),
                    item.getId(),
                    proposedPrice
                );
                
                if (error != null) {
                    DialogUtils.showError("Transaction Failed", error);
                } else {
                    DialogUtils.showSuccess("Transaction initiated successfully! The seller will be notified.");
                    // Refresh the view to show "View Transaction" button
                    showItemDetailView();
                }
            } catch (NumberFormatException e) {
                DialogUtils.showError("Invalid Price", "Please enter a valid price");
            }
        });
    }
    
    /**
     * Handle view transaction
     */
    private void handleViewTransaction(model.Transaction transaction) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Transaction Details");
        dialog.setHeaderText("Transaction Information");
        
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        content.getChildren().addAll(
            new Label("Status: " + transaction.getStatus()),
            new Label("Agreed Price: ¥" + String.format("%.2f", transaction.getAgreedPrice())),
            new Label("Delivery Method: " + (transaction.getDeliveryMethod() != null ? transaction.getDeliveryMethod() : "Not set")),
            new Label("Item Received: " + (transaction.isItemReceived() ? "Yes" : "No")),
            new Label("Item Verified: " + (transaction.isItemVerified() ? "Yes" : "No")),
            new Label("Funds Released: " + (transaction.isFundsReleased() ? "Yes" : "No"))
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    /**
     * Go back to item list
     */
    private void goBack() {
        BoardController boardController = new BoardController(mainLayout);
        boardController.showBoardView();
    }
}
