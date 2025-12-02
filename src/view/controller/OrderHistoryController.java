package view.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Order;
import model.Item;
import service.OrderService;
import service.ItemService;
import service.ReviewService;
import service.UserService;
import view.util.DialogUtils;

import java.util.List;

/**
 * Order history controller - displays buyer's order history.
 * Shows purchased items, order status, and review functionality.
 */
public class OrderHistoryController {

    private BorderPane mainLayout;
    private OrderService orderService;
    private ItemService itemService;
    private ReviewService reviewService;
    private VBox orderListContainer;

    public OrderHistoryController(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        this.orderService = new OrderService();
        this.itemService = new ItemService();
        this.reviewService = new ReviewService();
    }

    public void showOrderHistoryView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Top bar
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        Label titleLabel = new Label("My Orders");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadOrders());

        topBar.getChildren().addAll(titleLabel, spacer, refreshButton);

        // Order list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");

        orderListContainer = new VBox(15);
        orderListContainer.setPadding(new Insets(10));
        scrollPane.setContent(orderListContainer);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(topBar, scrollPane);

        loadOrders();

        mainLayout.setCenter(root);
    }

    private void loadOrders() {
        orderListContainer.getChildren().clear();
        Long userId = UserService.getCurrentUser().getId();
        List<Order> orders = orderService.getOrdersByBuyer(userId);

        if (orders.isEmpty()) {
            Label emptyLabel = new Label("You haven't purchased any items yet");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            orderListContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Order order : orders) {
            orderListContainer.getChildren().add(createOrderCard(order));
        }
    }

    private HBox createOrderCard(Order order) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-border-width: 1;");

        Item item = itemService.getItemById(order.getItemId());
        String itemTitle = (item != null) ? item.getTitle() : "Unknown Item (ID: " + order.getItemId() + ")";

        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label orderNoLabel = new Label("Order #: " + order.getOrderNo());
        orderNoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

        Label titleLabel = new Label(itemTitle);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label timeLabel = new Label("Date: " + order.getCreatedTime());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        infoBox.getChildren().addAll(orderNoLabel, titleLabel, timeLabel);

        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        Label priceLabel = new Label("¥" + String.format("%.2f", order.getAmount()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        Label statusLabel = new Label(order.getStatus());
        statusLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10;");

        statusBox.getChildren().addAll(priceLabel, statusLabel);
        
        // Action Buttons
        if ("SHIPPED".equals(order.getStatus())) {
            Button confirmButton = new Button("Confirm Receipt");
            confirmButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 15; -fx-background-radius: 5;");
            confirmButton.setOnAction(e -> handleConfirmReceipt(order));
            statusBox.getChildren().add(confirmButton);
        }
        
        // Review Button
        if ("COMPLETED".equals(order.getStatus()) && !reviewService.hasReviewed(order.getId())) {
            Button reviewButton = new Button("Review");
            reviewButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 15; -fx-background-radius: 5;");
            reviewButton.setOnAction(e -> showReviewDialog(order));
            statusBox.getChildren().add(reviewButton);
        } else if (reviewService.hasReviewed(order.getId())) {
            Label reviewedLabel = new Label("Reviewed");
            reviewedLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-font-style: italic;");
            statusBox.getChildren().add(reviewedLabel);
        }

        card.getChildren().addAll(infoBox, statusBox);

        return card;
    }
    
    private void handleConfirmReceipt(Order order) {
        boolean confirm = DialogUtils.showConfirm("Confirm Receipt", "Have you received the item? This will complete the order.");
        if (confirm) {
            String error = orderService.updateOrderStatus(order.getId(), "COMPLETED", UserService.getCurrentUser().getId());
            if (error != null) {
                DialogUtils.showError("Operation Failed", error);
            } else {
                DialogUtils.showSuccess("Order completed!");
                loadOrders();
            }
        }
    }
    
    private void showReviewDialog(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Write a Review");
        dialog.setHeaderText("Rate your experience");
        
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(5, 4, 3, 2, 1);
        ratingBox.setValue(5);
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your review here...");
        commentArea.setPrefRowCount(3);
        
        grid.add(new Label("Rating:"), 0, 0);
        grid.add(ratingBox, 1, 0);
        grid.add(new Label("Comment:"), 0, 1);
        grid.add(commentArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.showAndWait().ifPresent(result -> {
            if (result == submitButtonType) {
                String error = reviewService.addReview(
                    order.getId(), 
                    order.getBuyerId(), 
                    order.getSellerId(), 
                    order.getItemId(), 
                    ratingBox.getValue(), 
                    commentArea.getText()
                );
                
                if (error != null) {
                    DialogUtils.showError("Review Failed", error);
                } else {
                    DialogUtils.showSuccess("Review submitted successfully!");
                    loadOrders();
                }
            }
        });
    }
}

