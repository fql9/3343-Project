package view.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Notification;
import network.P2PService;
import service.MessageService;
import service.NotificationService;
import service.UserService;
import view.util.DialogUtils;

import java.util.List;

/**
 * Main controller - main view navigation.
 * Handles the main application layout and navigation between views.
 * Includes sidebar menu, top bar, and content area management.
 */
public class MainController {

    private Stage primaryStage;
    private UserService userService;
    private MessageService messageService;
    private NotificationService notificationService;
    private BorderPane mainLayout;
    private Label unreadCountLabel;
    private Label notificationCountLabel;
    private Timeline notificationPoller;
    
    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = new UserService();
        this.messageService = new MessageService();
        this.notificationService = new NotificationService();
        startNotificationPoller();
    }
    
    private void startNotificationPoller() {
        notificationPoller = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            if (UserService.getCurrentUser() != null) {
                checkNewNotifications();
                updateUnreadCount();
            }
        }));
        notificationPoller.setCycleCount(Timeline.INDEFINITE);
        notificationPoller.play();
    }

    private void checkNewNotifications() {
        // In a real app, we would track the last checked time or ID.
        // Here we just check count for badge, but for "popup" we might need more logic.
        // For simplicity, we just update the badge count here.
        // If we wanted a popup, we'd check for notifications created since last check.
    }

    /**
     * Show main view.
     * Creates and displays the main application interface.
     */
    public void showMainView() {
        mainLayout = new BorderPane();
        
        // Top navigation bar
        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);
        
        // Left menu
        VBox leftMenu = createLeftMenu();
        mainLayout.setLeft(leftMenu);
        
        // Default: show item board
        showBoard();
        
        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Second-hand Trading Platform - Home");
        primaryStage.show();
        
        // Update unread message count
        updateUnreadCount();
    }
    
    /**
     * Create top navigation bar.
     * @return HBox containing the navigation bar components.
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #2c3e50; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 0, 0, 0, 2);");
        
        Label titleLabel = new Label("Second-hand Trading Platform");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label("User: " + UserService.getCurrentUser().getUsername());
        userLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Role label removed as everyone is a SELLER/USER now
        
        unreadCountLabel = new Label();
        unreadCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 2 8;");
        
        notificationCountLabel = new Label();
        notificationCountLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 2 8;");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
        logoutButton.setOnAction(e -> handleLogout());
        
        topBar.getChildren().addAll(titleLabel, spacer, unreadCountLabel, notificationCountLabel, userLabel, logoutButton);
        
        return topBar;
    }
    
    /**
     * Create left menu.
     * @return VBox containing the sidebar menu buttons.
     */
    private VBox createLeftMenu() {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(20, 10, 20, 10));
        menu.setStyle("-fx-background-color: #34495e;");
        menu.setPrefWidth(220);
        
        Label menuLabel = new Label("MENU");
        menuLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 0 10 10;");
        menu.getChildren().add(menuLabel);
        
        Button boardButton = createMenuButton("Item Market");
        boardButton.setOnAction(e -> showBoard());
        
        Button myItemsButton = createMenuButton("My Items");
        myItemsButton.setOnAction(e -> showMyItems());
        
        Button myOrdersButton = createMenuButton("My Orders");
        myOrdersButton.setOnAction(e -> showMyOrders());
        
        Button mySalesButton = createMenuButton("My Sales");
        mySalesButton.setOnAction(e -> showMySales());
        
        Button favoritesButton = createMenuButton("My Favorites");
        favoritesButton.setOnAction(e -> showFavorites());
        
        Button messagesButton = createMenuButton("My Messages");
        messagesButton.setOnAction(e -> showMessages());
        
        Button p2pChatButton = createMenuButton("P2P Live Chat");
        p2pChatButton.setOnAction(e -> showP2PChat());
        
        Button notificationsButton = createMenuButton("Notifications");
        notificationsButton.setOnAction(e -> showNotifications());

        Button profileButton = createMenuButton("My Profile");
        profileButton.setOnAction(e -> showProfile());

        menu.getChildren().addAll(boardButton, myItemsButton, myOrdersButton, mySalesButton, favoritesButton, messagesButton, p2pChatButton, notificationsButton, profileButton);
        
        // If admin, add user management button
        if (UserService.isAdmin()) {
            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 0, 10, 0));
            
            Label adminLabel = new Label("ADMIN");
            adminLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 0 10 10;");
            
            Button userManagementButton = createMenuButton("User Management");
            userManagementButton.setOnAction(e -> showUserManagement());
            menu.getChildren().addAll(separator, adminLabel, userManagementButton);
        }
        
        return menu;
    }
    
    /**
     * Create menu button.
     * @param text The button label text.
     * @return Button with consistent styling.
     */
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                       "-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5; -fx-cursor: hand;";
        
        String hoverStyle = "-fx-background-color: #2c3e50; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5; -fx-cursor: hand;";
                          
        button.setStyle(defaultStyle);
        
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));
        
        return button;
    }
    
    /**
     * Show item market.
     * Navigates to the marketplace view.
     */
    private void showBoard() {
        BoardController boardController = new BoardController(mainLayout);
        boardController.showBoardView();
    }
    
    /**
     * Show my items.
     * Navigates to the user's published items view.
     */
    private void showMyItems() {
        MyItemsController myItemsController = new MyItemsController(mainLayout);
        myItemsController.showMyItemsView();
    }

    /**
     * Show my orders.
     * Navigates to the user's purchase history view.
     */
    private void showMyOrders() {
        OrderHistoryController orderHistoryController = new OrderHistoryController(mainLayout);
        orderHistoryController.showOrderHistoryView();
    }

    /**
     * Show my sales.
     * Navigates to the user's sales history view.
     */
    private void showMySales() {
        SalesHistoryController salesHistoryController = new SalesHistoryController(mainLayout);
        salesHistoryController.showSalesHistoryView();
    }
    
    /**
     * Show my favorites.
     * Navigates to the user's favorite items view.
     */
    private void showFavorites() {
        FavoritesController favoritesController = new FavoritesController(mainLayout);
        favoritesController.showFavoritesView();
    }
    
    /**
     * Show my messages.
     * Navigates to the messaging interface.
     */
    private void showMessages() {
        MessageController messageController = new MessageController(mainLayout, this);
        messageController.showMessagesView();
    }
    
    /**
     * Show P2P live chat.
     * Navigates to the real-time P2P chat interface.
     */
    private void showP2PChat() {
        P2PChatController p2pChatController = new P2PChatController(mainLayout, this);
        p2pChatController.showP2PChatView();
    }
    
    /**
     * Show notifications.
     * Navigates to the notifications list view.
     */
    private void showNotifications() {
        NotificationController notificationController = new NotificationController(mainLayout, this);
        notificationController.showNotificationView();
    }

    /**
     * Show user profile.
     * Navigates to the profile editing view.
     */
    private void showProfile() {
        UserProfileController userProfileController = new UserProfileController(mainLayout);
        userProfileController.showUserProfileView();
    }
    
    /**
     * Show user management.
     * Navigates to the admin user management view.
     */
    private void showUserManagement() {
        UserManagementController userManagementController = new UserManagementController(mainLayout);
        userManagementController.showUserManagementView();
    }
    
    /**
     * Update unread message count.
     * Updates the badge counters in the top bar.
     */
    public void updateUnreadCount() {
        long unreadMsg = messageService.getUnreadCount(UserService.getCurrentUser().getId());
        if (unreadMsg > 0) {
            unreadCountLabel.setText("Msg: " + unreadMsg);
            unreadCountLabel.setVisible(true);
            unreadCountLabel.setManaged(true);
            unreadCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 2 8;");
        } else {
            unreadCountLabel.setText("");
            unreadCountLabel.setVisible(false);
            unreadCountLabel.setManaged(false);
        }
        
        List<Notification> unreadNotifs = notificationService.getUnreadNotifications(UserService.getCurrentUser().getId());
        if (!unreadNotifs.isEmpty()) {
            notificationCountLabel.setText("Notif: " + unreadNotifs.size());
            notificationCountLabel.setVisible(true);
            notificationCountLabel.setManaged(true);
            notificationCountLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 2 8;");
        } else {
            notificationCountLabel.setText("");
            notificationCountLabel.setVisible(false);
            notificationCountLabel.setManaged(false);
        }
    }
    
    /**
     * Handle logout action.
     * Confirms and performs user logout.
     */
    private void handleLogout() {
        boolean confirm = DialogUtils.showConfirm("Confirm Logout", "Are you sure you want to logout?");
        if (confirm) {
            if (notificationPoller != null) {
                notificationPoller.stop();
            }
            
            // Shutdown P2P service before logout
            try {
                P2PService.getInstance().shutdown();
            } catch (Exception e) {
                System.err.println("Error shutting down P2P service: " + e.getMessage());
            }
            
            userService.logout();
            LoginController loginController = new LoginController(primaryStage);
            loginController.showLoginView();
        }
    }
}

