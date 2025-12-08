package view.controller;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Item;
import model.User;
import service.FavoriteService;
import service.ReviewService;
import service.UserService;
import view.util.DialogUtils;

/**
 * Item detail controller - displays item detailed information.
 * Shows item images, description, seller info, and action buttons.
 * Handles favorite, contact seller, and purchase actions.
 */
public class ItemDetailController {

    private BorderPane mainLayout;
    private Item item;
    private FavoriteService favoriteService;
    private UserService userService;
    private ReviewService reviewService;
    private Button favoriteButton;
    
    public ItemDetailController(BorderPane mainLayout, Item item) {
        this.mainLayout = mainLayout;
        this.item = item;
        this.favoriteService = new FavoriteService();
        this.userService = new UserService();
        this.reviewService = new ReviewService();
    }
    
    /**
     * Show item detail view.
     * Creates and displays the detailed item information page.
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
        detailCard.setPadding(new Insets(40));
        detailCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        // detailCard.setMaxWidth(800); // Removed fixed max width
        
        // Make detailCard grow to fill width
        HBox.setHgrow(detailCard, Priority.ALWAYS);
        
        // Title and price
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label categoryLabel = new Label("Category: " + (item.getCategory() != null ? item.getCategory() : "Uncategorized"));
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-background-color: #95a5a6; -fx-padding: 3 10; -fx-background-radius: 15;");
        
        titleBox.getChildren().addAll(titleLabel, categoryLabel);
        
        Label priceLabel = new Label("¥" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        headerBox.getChildren().addAll(titleBox, priceLabel);
        
        // Image display
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 20; -fx-background-radius: 5;");
        // Make image box grow
        VBox.setVgrow(imageBox, Priority.ALWAYS);
        
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                String imageUri = util.ImageUtils.getImageUri(item.getImageUrl());
                if (imageUri != null) {
                    Image image = new Image(imageUri, true); // Load in background
                    ImageView imageView = new ImageView(image);
                    
                    // Bind image size to window size
                    imageView.fitWidthProperty().bind(mainLayout.widthProperty().multiply(0.6)); // 60% of window width
                    imageView.fitHeightProperty().bind(mainLayout.heightProperty().multiply(0.5)); // 50% of window height
                    imageView.setPreserveRatio(true);
                    
                    imageBox.getChildren().add(imageView);
                } else {
                    Label errorLabel = new Label("Image not found");
                    errorLabel.setStyle("-fx-text-fill: #95a5a6;");
                    imageBox.getChildren().add(errorLabel);
                }
            } catch (Exception e) {
                Label errorLabel = new Label("Image failed to load");
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                imageBox.getChildren().add(errorLabel);
            }
        }

        // Description
        VBox descBox = new VBox(10);
        Label descTitle = new Label("Item Description");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        Label descLabel = new Label(item.getDescription() != null ? item.getDescription() : "No description available");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-line-spacing: 5;");
        
        descBox.getChildren().addAll(descTitle, descLabel);
        
        // Seller info
        User seller = userService.getUserById(item.getSellerId());
        VBox sellerBox = new VBox(10);
        Label sellerTitle = new Label("Seller Information");
        sellerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        Label sellerLabel = new Label("Seller: " + (seller != null ? seller.getUsername() : "Unknown"));
        sellerLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        
        // Seller Rating
        double avgRating = reviewService.getSellerAverageRating(item.getSellerId());
        Label ratingLabel = new Label(String.format("Rating: %.1f / 5.0", avgRating));
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
        
        sellerBox.getChildren().addAll(sellerTitle, sellerLabel, ratingLabel);
        
        // Other info
        Label timeLabel = new Label("Posted: " + item.getCreatedTime());
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");
        
        // Action buttons
        HBox actionBox = new HBox(20);
        actionBox.setAlignment(Pos.CENTER);
        
        Long currentUserId = UserService.getCurrentUser().getId();
        
        // If not own item
        if (!item.getSellerId().equals(currentUserId)) {
            // Favorite button
            boolean isFavorite = favoriteService.isFavorite(currentUserId, item.getId());
            favoriteButton = new Button(isFavorite ? "Favorited" : "Add to Favorites");
            String favStyle = isFavorite ? "-fx-background-color: #95a5a6;" : "-fx-background-color: #e67e22;";
            favoriteButton.setStyle(favStyle + " -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 5; -fx-cursor: hand;");
            favoriteButton.setOnAction(e -> handleFavorite());
            
            // Contact seller button
            Button contactButton = new Button("Contact Seller");
            contactButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                                  "-fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 5; -fx-cursor: hand;");
            contactButton.setOnAction(e -> handleContactSeller());

            // Buy Now button
            Button buyButton = new Button("Buy Now");
            buyButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                               "-fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 5; -fx-cursor: hand;");
            buyButton.setOnAction(e -> handleBuyNow());
            
            actionBox.getChildren().addAll(favoriteButton, contactButton, buyButton);
        } else {
            Label ownLabel = new Label("This is your published item");
            ownLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            actionBox.getChildren().add(ownLabel);
        }
        
        detailCard.getChildren().addAll(headerBox, new Separator(), imageBox, new Separator(), descBox, 
                                        new Separator(), sellerBox, timeLabel, 
                                        new Separator(), actionBox);
        
        // Make detailCard fill the width
        VBox.setVgrow(detailCard, Priority.ALWAYS);
        
        root.getChildren().addAll(backButton, detailCard);
        
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true); // Allow height to grow
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");
        
        mainLayout.setCenter(scrollPane);
    }
    
    /**
     * Handle favorite/unfavorite action.
     * Toggles the favorite status of the current item.
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
     * Handle contact seller action.
     * Opens the messaging interface with the seller.
     */
    private void handleContactSeller() {
        // Open the full messages view and show the conversation with the seller
        MessageController messageController = new MessageController(mainLayout, null);
        messageController.openConversation(item.getSellerId());
    }

    /**
     * Handle buy now action.
     * Navigates to the checkout page.
     */
    private void handleBuyNow() {
        CheckoutController checkoutController = new CheckoutController(mainLayout, item);
        checkoutController.showCheckoutView();
    }
    
    /**
     * Go back to item list.
     * Returns to the marketplace view.
     */
    private void goBack() {
        BoardController boardController = new BoardController(mainLayout);
        boardController.showBoardView();
    }
}

