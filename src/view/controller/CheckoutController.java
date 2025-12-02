package view.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Item;
import service.OrderService;
import service.UserService;
import view.util.DialogUtils;

/**
 * Checkout controller - handles the purchase checkout process.
 * Displays order summary, shipping info, and payment options.
 */
public class CheckoutController {

    private BorderPane mainLayout;
    private Item item;
    private OrderService orderService;

    public CheckoutController(BorderPane mainLayout, Item item) {
        this.mainLayout = mainLayout;
        this.item = item;
        this.orderService = new OrderService();
    }

    public void showCheckoutView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setAlignment(Pos.TOP_CENTER);

        // Title
        Label titleLabel = new Label("Checkout");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Order Summary Card
        VBox summaryCard = new VBox(15);
        summaryCard.setPadding(new Insets(20));
        summaryCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        summaryCard.setMaxWidth(600);

        Label itemTitle = new Label("Item: " + item.getTitle());
        itemTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label itemPrice = new Label("Price: ¥" + String.format("%.2f", item.getPrice()));
        itemPrice.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");

        summaryCard.getChildren().addAll(itemTitle, itemPrice);

        // Shipping Info
        VBox shippingBox = new VBox(10);
        shippingBox.setMaxWidth(600);
        
        Label addressLabel = new Label("Shipping Address:");
        addressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Enter your full shipping address...");
        addressArea.setPrefRowCount(3);
        addressArea.setWrapText(true);

        shippingBox.getChildren().addAll(addressLabel, addressArea);

        // Payment Method (Mock)
        VBox paymentBox = new VBox(10);
        paymentBox.setMaxWidth(600);
        
        Label paymentLabel = new Label("Payment Method:");
        paymentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        RadioButton creditCard = new RadioButton("Credit Card");
        RadioButton alipay = new RadioButton("Alipay");
        RadioButton wechat = new RadioButton("WeChat Pay");
        ToggleGroup group = new ToggleGroup();
        creditCard.setToggleGroup(group);
        alipay.setToggleGroup(group);
        wechat.setToggleGroup(group);
        alipay.setSelected(true);

        HBox paymentOptions = new HBox(20);
        paymentOptions.getChildren().addAll(alipay, wechat, creditCard);
        
        paymentBox.getChildren().addAll(paymentLabel, paymentOptions);

        // Actions
        HBox actionBox = new HBox(20);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        cancelButton.setOnAction(e -> goBack());

        Button payButton = new Button("Pay Now");
        payButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        payButton.setOnAction(e -> handlePayment(addressArea.getText()));

        actionBox.getChildren().addAll(cancelButton, payButton);

        root.getChildren().addAll(titleLabel, summaryCard, shippingBox, paymentBox, actionBox);

        mainLayout.setCenter(root);
    }

    private void handlePayment(String address) {
        if (address == null || address.trim().isEmpty()) {
            DialogUtils.showWarning("Input Error", "Please enter shipping address");
            return;
        }

        // Simulate payment processing
        boolean confirm = DialogUtils.showConfirm("Confirm Payment", "Pay ¥" + item.getPrice() + " for this item?");
        if (confirm) {
            String error = orderService.createOrder(UserService.getCurrentUser().getId(), item.getId(), address);
            if (error != null) {
                DialogUtils.showError("Order Failed", error);
            } else {
                DialogUtils.showSuccess("Payment Successful! Order created.");
                // Redirect to Board or Order History
                BoardController boardController = new BoardController(mainLayout);
                boardController.showBoardView();
            }
        }
    }

    private void goBack() {
        ItemDetailController detailController = new ItemDetailController(mainLayout, item);
        detailController.showItemDetailView();
    }
}

