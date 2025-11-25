package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Item;
import model.Transaction;
import model.TransactionStatus;
import model.DeliveryMethod;
import model.PaymentMethod;
import model.User;
import service.ItemService;
import service.TransactionService;
import service.UserService;
import util.DialogUtils;

import java.util.List;

/**
 * Transaction controller - manages user transactions (as buyer or seller)
 */
public class TransactionController {

    private BorderPane mainLayout;
    private TransactionService transactionService;
    private ItemService itemService;
    private UserService userService;
    private VBox transactionListContainer;
    private boolean isBuyerView; // true for buyer view, false for seller view
    
    public TransactionController(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        this.transactionService = new TransactionService();
        this.itemService = new ItemService();
        this.userService = new UserService();
    }
    
    /**
     * Show transactions view
     */
    public void showTransactionsView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Top bar with tabs
        HBox topBar = createTopBar();
        
        // Transaction list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");
        
        transactionListContainer = new VBox(15);
        transactionListContainer.setPadding(new Insets(10));
        scrollPane.setContent(transactionListContainer);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().addAll(topBar, scrollPane);
        
        // Load transactions (default: buyer view)
        isBuyerView = true;
        loadBuyerTransactions();
        
        mainLayout.setCenter(root);
    }
    
    /**
     * Create top bar with tabs
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label titleLabel = new Label("My Transactions");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Tabs for buyer/seller view
        Button buyerButton = new Button("As Buyer");
        Button sellerButton = new Button("As Seller");
        
        buyerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        buyerButton.setOnAction(e -> {
            isBuyerView = true;
            loadBuyerTransactions();
            updateTabButtons(buyerButton, sellerButton);
        });
        
        sellerButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        sellerButton.setOnAction(e -> {
            isBuyerView = false;
            loadSellerTransactions();
            updateTabButtons(buyerButton, sellerButton);
        });
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> {
            if (isBuyerView) {
                loadBuyerTransactions();
            } else {
                loadSellerTransactions();
            }
        });
        
        topBar.getChildren().addAll(titleLabel, spacer, buyerButton, sellerButton, refreshButton);
        
        return topBar;
    }
    
    /**
     * Update tab button styles
     */
    private void updateTabButtons(Button buyerButton, Button sellerButton) {
        if (isBuyerView) {
            buyerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
            sellerButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        } else {
            buyerButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
            sellerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        }
    }
    
    /**
     * Load buyer transactions
     */
    private void loadBuyerTransactions() {
        transactionListContainer.getChildren().clear();
        
        Long buyerId = UserService.getCurrentUser().getId();
        List<Transaction> transactions = transactionService.getTransactionsByBuyer(buyerId);
        
        if (transactions.isEmpty()) {
            Label emptyLabel = new Label("You have no purchase transactions");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            transactionListContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Transaction transaction : transactions) {
            transactionListContainer.getChildren().add(createBuyerTransactionCard(transaction));
        }
    }
    
    /**
     * Load seller transactions
     */
    private void loadSellerTransactions() {
        transactionListContainer.getChildren().clear();
        
        Long sellerId = UserService.getCurrentUser().getId();
        List<Transaction> transactions = transactionService.getTransactionsBySeller(sellerId);
        
        if (transactions.isEmpty()) {
            Label emptyLabel = new Label("You have no sales transactions");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            transactionListContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Transaction transaction : transactions) {
            transactionListContainer.getChildren().add(createSellerTransactionCard(transaction));
        }
    }
    
    /**
     * Create buyer transaction card
     */
    private VBox createBuyerTransactionCard(Transaction transaction) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-border-width: 1;");
        
        Item item = itemService.getItemById(transaction.getItemId());
        User seller = userService.getUserById(transaction.getSellerId());
        
        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox itemInfo = new VBox(5);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);
        
        Label itemTitle = new Label(item != null ? item.getTitle() : "Unknown Item");
        itemTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label sellerLabel = new Label("Seller: " + (seller != null ? seller.getUsername() : "Unknown"));
        sellerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        itemInfo.getChildren().addAll(itemTitle, sellerLabel);
        
        Label priceLabel = new Label("¥" + String.format("%.2f", transaction.getAgreedPrice()));
        priceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Label statusLabel = new Label("Status: " + transaction.getStatus());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(itemInfo, priceLabel, statusLabel);
        
        // Transaction details
        VBox detailsBox = new VBox(8);
        detailsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-padding: 10;");
        
        detailsBox.getChildren().add(new Label("Delivery Method: " + 
            (transaction.getDeliveryMethod() != null ? transaction.getDeliveryMethod() : "Not set")));
        
        if (transaction.getTrackingNumber() != null && !transaction.getTrackingNumber().isEmpty()) {
            detailsBox.getChildren().add(new Label("Tracking Number: " + transaction.getTrackingNumber()));
        }
        
        detailsBox.getChildren().add(new Label("Created: " + transaction.getCreatedTime()));
        detailsBox.getChildren().add(new Label("Updated: " + transaction.getUpdatedTime()));
        
        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        
        // Add action buttons based on transaction status
        addBuyerActionButtons(actionBox, transaction);
        
        card.getChildren().addAll(headerBox, new Separator(), detailsBox, actionBox);
        
        return card;
    }
    
    /**
     * Create seller transaction card
     */
    private VBox createSellerTransactionCard(Transaction transaction) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-border-width: 1;");
        
        Item item = itemService.getItemById(transaction.getItemId());
        User buyer = userService.getUserById(transaction.getBuyerId());
        
        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox itemInfo = new VBox(5);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);
        
        Label itemTitle = new Label(item != null ? item.getTitle() : "Unknown Item");
        itemTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label buyerLabel = new Label("Buyer: " + (buyer != null ? buyer.getUsername() : "Unknown"));
        buyerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        itemInfo.getChildren().addAll(itemTitle, buyerLabel);
        
        Label priceLabel = new Label("¥" + String.format("%.2f", transaction.getAgreedPrice()));
        priceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Label statusLabel = new Label("Status: " + transaction.getStatus());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(itemInfo, priceLabel, statusLabel);
        
        // Transaction details
        VBox detailsBox = new VBox(8);
        detailsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-padding: 10;");
        
        detailsBox.getChildren().add(new Label("Delivery Method: " + 
            (transaction.getDeliveryMethod() != null ? transaction.getDeliveryMethod() : "Not set")));
        
        if (transaction.getShippingAddress() != null && !transaction.getShippingAddress().isEmpty()) {
            Label addressLabel = new Label("Shipping Address: " + transaction.getShippingAddress());
            addressLabel.setWrapText(true);
            detailsBox.getChildren().add(addressLabel);
        }
        
        if (transaction.getTrackingNumber() != null && !transaction.getTrackingNumber().isEmpty()) {
            Label trackingLabel = new Label("Tracking Number: " + transaction.getTrackingNumber());
            trackingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
            detailsBox.getChildren().add(trackingLabel);
        } else if (transaction.getStatus().equals(TransactionStatus.SHIPPED.name()) || 
                   transaction.getStatus().equals(TransactionStatus.DELIVERED.name()) ||
                   transaction.getStatus().equals(TransactionStatus.COMPLETED.name())) {
            Label trackingLabel = new Label("Tracking Number: Not provided");
            trackingLabel.setStyle("-fx-text-fill: #95a5a6;");
            detailsBox.getChildren().add(trackingLabel);
        }
        
        // Show item received and verified status for shipped/delivered transactions
        if (transaction.getStatus().equals(TransactionStatus.DELIVERED.name()) ||
            transaction.getStatus().equals(TransactionStatus.COMPLETED.name())) {
            detailsBox.getChildren().add(new Label("Item Received: " + (transaction.isItemReceived() ? "Yes" : "No")));
            detailsBox.getChildren().add(new Label("Item Verified: " + (transaction.isItemVerified() ? "Yes" : "No")));
            if (transaction.isFundsReleased()) {
                Label fundsLabel = new Label("Funds Released: Yes");
                fundsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");
                detailsBox.getChildren().add(fundsLabel);
            }
        }
        
        detailsBox.getChildren().add(new Label("Created: " + transaction.getCreatedTime()));
        detailsBox.getChildren().add(new Label("Updated: " + transaction.getUpdatedTime()));
        
        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        
        // Add action buttons based on transaction status
        addSellerActionButtons(actionBox, transaction);
        
        card.getChildren().addAll(headerBox, new Separator(), detailsBox, actionBox);
        
        return card;
    }
    
    /**
     * Add buyer action buttons based on transaction status
     */
    private void addBuyerActionButtons(HBox actionBox, Transaction transaction) {
        String status = transaction.getStatus();
        
        if (status.equals(TransactionStatus.PENDING.name()) || 
            status.equals(TransactionStatus.NEGOTIATING.name())) {
            // Can confirm transaction and choose payment method
            Button confirmButton = new Button("Confirm & Pay");
            confirmButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
            confirmButton.setOnAction(e -> handleBuyerConfirmAndPay(transaction));
            actionBox.getChildren().add(confirmButton);
            
            // Can propose price if negotiating
            if (status.equals(TransactionStatus.NEGOTIATING.name())) {
                Button proposeButton = new Button("Propose Price");
                proposeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
                proposeButton.setOnAction(e -> handleProposePrice(transaction));
                actionBox.getChildren().add(proposeButton);
            }
            
            Button cancelButton = new Button("Cancel");
            cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
            cancelButton.setOnAction(e -> handleCancelTransaction(transaction));
            actionBox.getChildren().add(cancelButton);
        } else if (status.equals(TransactionStatus.CONFIRMED.name()) || 
                   status.equals(TransactionStatus.SHIPPED.name())) {
            // Can mark as delivered
            if (!transaction.isItemReceived()) {
                Button receiveButton = new Button("Mark as Received");
                receiveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
                receiveButton.setOnAction(e -> handleMarkAsDelivered(transaction));
                actionBox.getChildren().add(receiveButton);
            }
        } else if (status.equals(TransactionStatus.DELIVERED.name())) {
            // Can verify item and complete transaction
            if (!transaction.isItemVerified()) {
                Button verifyButton = new Button("Verify Item");
                verifyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
                verifyButton.setOnAction(e -> handleVerifyItem(transaction));
                actionBox.getChildren().add(verifyButton);
            }
            
            if (transaction.isItemVerified() && !transaction.isFundsReleased()) {
                Button completeButton = new Button("Complete Transaction");
                completeButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
                completeButton.setOnAction(e -> handleCompleteTransaction(transaction));
                actionBox.getChildren().add(completeButton);
            }
        }
    }
    
    /**
     * Add seller action buttons based on transaction status
     */
    private void addSellerActionButtons(HBox actionBox, Transaction transaction) {
        String status = transaction.getStatus();
        
        if (status.equals(TransactionStatus.PENDING.name())) {
            // Can start negotiation or confirm
            Button negotiateButton = new Button("Start Negotiation");
            negotiateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
            negotiateButton.setOnAction(e -> handleStartNegotiation(transaction));
            actionBox.getChildren().add(negotiateButton);
            
            Button confirmButton = new Button("Confirm Transaction");
            confirmButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
            confirmButton.setOnAction(e -> handleSellerConfirm(transaction));
            actionBox.getChildren().add(confirmButton);
        } else if (status.equals(TransactionStatus.NEGOTIATING.name())) {
            // Can propose price or confirm
            Button proposeButton = new Button("Propose Price");
            proposeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
            proposeButton.setOnAction(e -> handleProposePrice(transaction));
            actionBox.getChildren().add(proposeButton);
            
            Button confirmButton = new Button("Confirm Transaction");
            confirmButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
            confirmButton.setOnAction(e -> handleSellerConfirm(transaction));
            actionBox.getChildren().add(confirmButton);
        } else if (status.equals(TransactionStatus.CONFIRMED.name())) {
            // Can mark as shipped
            Button shipButton = new Button("Mark as Shipped");
            shipButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15;");
            shipButton.setOnAction(e -> handleMarkAsShipped(transaction));
            actionBox.getChildren().add(shipButton);
        } else if (status.equals(TransactionStatus.SHIPPED.name()) || 
                   status.equals(TransactionStatus.DELIVERED.name()) ||
                   status.equals(TransactionStatus.COMPLETED.name())) {
            // Show view details button for shipped/delivered/completed transactions
            Label infoLabel = new Label("Transaction in progress - Track shipping status above");
            infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            actionBox.getChildren().add(infoLabel);
        }
    }
    
    /**
     * Handle buyer confirm and pay
     */
    private void handleBuyerConfirmAndPay(Transaction transaction) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Confirm Transaction & Payment");
        dialog.setHeaderText("Choose payment method and delivery method");
        
        ButtonType confirmButtonType = new ButtonType("Confirm & Pay", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label priceLabel = new Label("Total Price: ¥" + String.format("%.2f", transaction.getAgreedPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label paymentLabel = new Label("Payment Method:");
        ComboBox<PaymentMethod> paymentComboBox = new ComboBox<>();
        paymentComboBox.getItems().addAll(PaymentMethod.CARD, PaymentMethod.ALIPAY, PaymentMethod.OCTOPUS);
        paymentComboBox.setValue(PaymentMethod.ALIPAY);
        
        Label deliveryLabel = new Label("Delivery Method:");
        ComboBox<DeliveryMethod> deliveryComboBox = new ComboBox<>();
        deliveryComboBox.getItems().addAll(DeliveryMethod.LOCAL_PICKUP, DeliveryMethod.SHIP);
        deliveryComboBox.setValue(DeliveryMethod.LOCAL_PICKUP);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Shipping address (required if shipping)");
        addressField.setVisible(false);
        
        deliveryComboBox.setOnAction(e -> {
            addressField.setVisible(deliveryComboBox.getValue() == DeliveryMethod.SHIP);
        });
        
        content.getChildren().addAll(priceLabel, paymentLabel, paymentComboBox, 
                                     deliveryLabel, deliveryComboBox, addressField);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                PaymentMethod paymentMethod = paymentComboBox.getValue();
                DeliveryMethod deliveryMethod = deliveryComboBox.getValue();
                String shippingAddress = addressField.getText().trim();
                
                if (deliveryMethod == DeliveryMethod.SHIP && 
                    (shippingAddress == null || shippingAddress.isEmpty())) {
                    DialogUtils.showWarning("Input Error", "Shipping address is required for shipping delivery");
                    return null;
                }
                
                String error = transactionService.confirmTransaction(
                    transaction.getId(),
                    UserService.getCurrentUser().getId(),
                    deliveryMethod,
                    shippingAddress
                );
                
                if (error != null) {
                    DialogUtils.showError("Confirmation Failed", error);
                } else {
                    DialogUtils.showSuccess("Transaction confirmed and payment processed via " + paymentMethod.name() + "!");
                    if (isBuyerView) {
                        loadBuyerTransactions();
                    } else {
                        loadSellerTransactions();
                    }
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Handle seller confirm transaction
     */
    private void handleSellerConfirm(Transaction transaction) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Confirm Transaction");
        dialog.setHeaderText("Choose delivery method");
        
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label deliveryLabel = new Label("Delivery Method:");
        ComboBox<DeliveryMethod> deliveryComboBox = new ComboBox<>();
        deliveryComboBox.getItems().addAll(DeliveryMethod.LOCAL_PICKUP, DeliveryMethod.SHIP);
        deliveryComboBox.setValue(DeliveryMethod.LOCAL_PICKUP);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Shipping address (required if shipping)");
        addressField.setVisible(false);
        
        deliveryComboBox.setOnAction(e -> {
            addressField.setVisible(deliveryComboBox.getValue() == DeliveryMethod.SHIP);
        });
        
        content.getChildren().addAll(deliveryLabel, deliveryComboBox, addressField);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                DeliveryMethod deliveryMethod = deliveryComboBox.getValue();
                String shippingAddress = addressField.getText().trim();
                
                if (deliveryMethod == DeliveryMethod.SHIP && 
                    (shippingAddress == null || shippingAddress.isEmpty())) {
                    DialogUtils.showWarning("Input Error", "Shipping address is required for shipping delivery");
                    return null;
                }
                
                String error = transactionService.confirmTransaction(
                    transaction.getId(),
                    UserService.getCurrentUser().getId(),
                    deliveryMethod,
                    shippingAddress
                );
                
                if (error != null) {
                    DialogUtils.showError("Confirmation Failed", error);
                } else {
                    DialogUtils.showSuccess("Transaction confirmed!");
                    loadSellerTransactions();
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Handle start negotiation
     */
    private void handleStartNegotiation(Transaction transaction) {
        String error = transactionService.startNegotiation(
            transaction.getId(),
            UserService.getCurrentUser().getId()
        );
        
        if (error != null) {
            DialogUtils.showError("Negotiation Failed", error);
        } else {
            DialogUtils.showSuccess("Negotiation started");
            loadSellerTransactions();
        }
    }
    
    /**
     * Handle propose price
     */
    private void handleProposePrice(Transaction transaction) {
        TextInputDialog dialog = new TextInputDialog(String.format("%.2f", transaction.getAgreedPrice()));
        dialog.setTitle("Propose Price");
        dialog.setHeaderText("Enter your proposed price");
        dialog.setContentText("Price:");
        
        dialog.showAndWait().ifPresent(priceText -> {
            try {
                double newPrice = Double.parseDouble(priceText.trim());
                String error = transactionService.proposePrice(
                    transaction.getId(),
                    UserService.getCurrentUser().getId(),
                    newPrice
                );
                
                if (error != null) {
                    DialogUtils.showError("Proposal Failed", error);
                } else {
                    DialogUtils.showSuccess("Price proposal sent");
                    if (isBuyerView) {
                        loadBuyerTransactions();
                    } else {
                        loadSellerTransactions();
                    }
                }
            } catch (NumberFormatException e) {
                DialogUtils.showError("Invalid Price", "Please enter a valid price");
            }
        });
    }
    
    /**
     * Handle mark as shipped
     */
    private void handleMarkAsShipped(Transaction transaction) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mark as Shipped");
        dialog.setHeaderText("Enter tracking number (optional)");
        
        ButtonType shipButtonType = new ButtonType("Mark as Shipped", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(shipButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        TextField trackingField = new TextField();
        trackingField.setPromptText("Tracking number (leave empty if local pickup)");
        
        content.getChildren().addAll(new Label("Tracking Number:"), trackingField);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == shipButtonType) {
                return trackingField.getText().trim();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(trackingNumber -> {
            String error = transactionService.markAsShipped(
                transaction.getId(),
                UserService.getCurrentUser().getId(),
                trackingNumber.isEmpty() ? null : trackingNumber
            );
            
            if (error != null) {
                DialogUtils.showError("Shipment Failed", error);
            } else {
                DialogUtils.showSuccess("Item marked as shipped!");
                loadSellerTransactions();
            }
        });
    }
    
    /**
     * Handle mark as delivered
     */
    private void handleMarkAsDelivered(Transaction transaction) {
        boolean confirm = DialogUtils.showConfirm("Confirm Receipt", 
            "Have you received the item?");
        if (confirm) {
            String error = transactionService.markAsDelivered(
                transaction.getId(),
                UserService.getCurrentUser().getId()
            );
            
            if (error != null) {
                DialogUtils.showError("Delivery Failed", error);
            } else {
                DialogUtils.showSuccess("Item marked as delivered!");
                loadBuyerTransactions();
            }
        }
    }
    
    /**
     * Handle verify item
     */
    private void handleVerifyItem(Transaction transaction) {
        boolean confirm = DialogUtils.showConfirm("Verify Item", 
            "Is the item condition acceptable?");
        if (confirm) {
            String error = transactionService.verifyItemCondition(
                transaction.getId(),
                UserService.getCurrentUser().getId()
            );
            
            if (error != null) {
                DialogUtils.showError("Verification Failed", error);
            } else {
                DialogUtils.showSuccess("Item verified!");
                loadBuyerTransactions();
            }
        }
    }
    
    /**
     * Handle complete transaction
     */
    private void handleCompleteTransaction(Transaction transaction) {
        boolean confirm = DialogUtils.showConfirm("Complete Transaction", 
            "Are you sure you want to complete this transaction and release funds to the seller?");
        if (confirm) {
            String error = transactionService.completeTransaction(
                transaction.getId(),
                UserService.getCurrentUser().getId()
            );
            
            if (error != null) {
                DialogUtils.showError("Completion Failed", error);
            } else {
                DialogUtils.showSuccess("Transaction completed! Funds have been released.");
                loadBuyerTransactions();
            }
        }
    }
    
    /**
     * Handle cancel transaction
     */
    private void handleCancelTransaction(Transaction transaction) {
        boolean confirm = DialogUtils.showConfirm("Cancel Transaction", 
            "Are you sure you want to cancel this transaction?");
        if (confirm) {
            String error = transactionService.cancelTransaction(
                transaction.getId(),
                UserService.getCurrentUser().getId()
            );
            
            if (error != null) {
                DialogUtils.showError("Cancellation Failed", error);
            } else {
                DialogUtils.showSuccess("Transaction cancelled");
                if (isBuyerView) {
                    loadBuyerTransactions();
                } else {
                    loadSellerTransactions();
                }
            }
        }
    }
}

