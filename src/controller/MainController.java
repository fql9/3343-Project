package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.MessageService;
import service.UserService;
import util.DialogUtils;

/**
 * Main controller - main view navigation
 */
public class MainController {

    private Stage primaryStage;
    private UserService userService;
    private MessageService messageService;
    private BorderPane mainLayout;
    private Label unreadCountLabel;
    
    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = new UserService();
        this.messageService = new MessageService();
    }
    
    /**
     * Show main view
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
     * Create top navigation bar
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #2c3e50;");
        
        Label titleLabel = new Label("Second-hand Trading Platform");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label("User: " + UserService.getCurrentUser().getUsername());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label roleLabel = new Label("Role: " + UserService.getCurrentUser().getRole().name());
        roleLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 12px;");
        
        unreadCountLabel = new Label();
        unreadCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> handleLogout());
        
        topBar.getChildren().addAll(titleLabel, spacer, unreadCountLabel, userLabel, roleLabel, logoutButton);
        
        return topBar;
    }
    
    /**
     * Create left menu
     */
    private VBox createLeftMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        menu.setStyle("-fx-background-color: #34495e;");
        menu.setPrefWidth(200);
        
        Button boardButton = createMenuButton("Item Market", "#3498db");
        boardButton.setOnAction(e -> showBoard());
        
        Button myItemsButton = createMenuButton("My Items", "#9b59b6");
        myItemsButton.setOnAction(e -> showMyItems());
        
        Button favoritesButton = createMenuButton("My Favorites", "#e67e22");
        favoritesButton.setOnAction(e -> showFavorites());
        
        Button messagesButton = createMenuButton("My Messages", "#1abc9c");
        messagesButton.setOnAction(e -> showMessages());
        
        Button transactionsButton = createMenuButton("My Transactions", "#f39c12");
        transactionsButton.setOnAction(e -> showTransactions());
        
        menu.getChildren().addAll(boardButton, myItemsButton, favoritesButton, messagesButton, transactionsButton);
        
        // If admin, add user management button
        if (UserService.isAdmin()) {
            Button userManagementButton = createMenuButton("User Management", "#e74c3c");
            userManagementButton.setOnAction(e -> showUserManagement());
            menu.getChildren().add(userManagementButton);
        }
        
        return menu;
    }
    
    /**
     * Create menu button
     */
    private Button createMenuButton(String text, String color) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                       "-fx-font-size: 14px; -fx-padding: 12 20;");
        
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-padding: 12 20;"));
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-padding: 12 20;"));
        
        return button;
    }
    
    /**
     * Show item market
     */
    private void showBoard() {
        BoardController boardController = new BoardController(mainLayout);
        boardController.showBoardView();
    }
    
    /**
     * Show my items
     */
    private void showMyItems() {
        MyItemsController myItemsController = new MyItemsController(mainLayout);
        myItemsController.showMyItemsView();
    }
    
    /**
     * Show my favorites
     */
    private void showFavorites() {
        FavoritesController favoritesController = new FavoritesController(mainLayout);
        favoritesController.showFavoritesView();
    }
    
    /**
     * Show my messages
     */
    private void showMessages() {
        MessageController messageController = new MessageController(mainLayout, this);
        messageController.showMessagesView();
    }
    
    /**
     * Show transactions
     */
    private void showTransactions() {
        TransactionController transactionController = new TransactionController(mainLayout);
        transactionController.showTransactionsView();
    }
    
    /**
     * Show user management
     */
    private void showUserManagement() {
        UserManagementController userManagementController = new UserManagementController(mainLayout);
        userManagementController.showUserManagementView();
    }
    
    /**
     * Update unread message count
     */
    public void updateUnreadCount() {
        long unreadCount = messageService.getUnreadCount(UserService.getCurrentUser().getId());
        if (unreadCount > 0) {
            unreadCountLabel.setText("Unread: " + unreadCount);
        } else {
            unreadCountLabel.setText("");
        }
    }
    
    /**
     * Handle logout
     */
    private void handleLogout() {
        boolean confirm = DialogUtils.showConfirm("Confirm Logout", "Are you sure you want to logout?");
        if (confirm) {
            userService.logout();
            LoginController loginController = new LoginController(primaryStage);
            loginController.showLoginView();
        }
    }
}
