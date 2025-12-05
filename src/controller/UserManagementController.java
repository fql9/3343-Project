package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.User;
import service.UserService;
import util.DialogUtils;

import java.util.List;

/**
 * User management controller - admin manages all users
 */
public class UserManagementController {

    private BorderPane mainLayout;
    private UserService userService;
    private TableView<User> userTable;
    
    public UserManagementController(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        this.userService = new UserService();
    }
    
    /**
     * Show user management view
     */
    public void showUserManagementView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Title
        HBox topBar = createTopBar();
        
        // User table
        userTable = createUserTable();
        VBox.setVgrow(userTable, Priority.ALWAYS);
        
        // Action buttons
        HBox buttonBar = createButtonBar();
        
        root.getChildren().addAll(topBar, userTable, buttonBar);
        
        // Load user data
        loadUsers();
        
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
        
        Label titleLabel = new Label("User Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadUsers());
        
        topBar.getChildren().addAll(titleLabel, spacer, refreshButton);
        
        return topBar;
    }
    
    /**
     * Create user table
     */
    private TableView<User> createUserTable() {
        TableView<User> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");
        
        // ID column
        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        // Username column
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);
        
        // Email column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        // Role column
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole().name()));
        roleCol.setPrefWidth(100);
        
        // Status column
        TableColumn<User, Boolean> activeCol = new TableColumn<>("Status");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(100);
        activeCol.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Active" : "Banned");
                    setStyle("-fx-text-fill: " + (item ? "#2ecc71" : "#e74c3c") + "; -fx-font-weight: bold;");
                }
            }
        });
        
        table.getColumns().addAll(idCol, usernameCol, emailCol, roleCol, activeCol);
        
        return table;
    }
    
    /**
     * Create button bar
     */
    private HBox createButtonBar() {
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10));
        
        Button banButton = new Button("Ban User");
        banButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        banButton.setOnAction(e -> handleBanUser());
        
        Button unbanButton = new Button("Unban User");
        unbanButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        unbanButton.setOnAction(e -> handleUnbanUser());
        
        Button deleteButton = new Button("Delete User");
        deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        deleteButton.setOnAction(e -> handleDeleteUser());
        
        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        viewDetailsButton.setOnAction(e -> handleViewDetails());
        
        buttonBar.getChildren().addAll(viewDetailsButton, banButton, unbanButton, deleteButton);
        
        return buttonBar;
    }
    
    /**
     * Load user list
     */
    private void loadUsers() {
        userTable.getSelectionModel().clearSelection();
        userTable.getItems().clear();
        
        List<User> users = userService.getAllUsers();
        userTable.getItems().addAll(users);
    }
    
    /**
     * Handle ban user
     */
    private void handleBanUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            DialogUtils.showWarning("No User Selected", "Please select a user to ban first");
            return;
        }
        
        if (!selectedUser.isActive()) {
            DialogUtils.showWarning("Operation Failed", "This user is already banned");
            return;
        }
        
        if (selectedUser.getId().equals(UserService.getCurrentUser().getId())) {
            DialogUtils.showWarning("Operation Failed", "You cannot ban yourself");
            return;
        }
        
        boolean confirm = DialogUtils.showConfirm("Confirm Ban", 
            "Are you sure you want to ban user " + selectedUser.getUsername() + "?");
        
        if (confirm) {
            userService.setUserActive(selectedUser.getId(), false);
            DialogUtils.showSuccess("User has been banned");
            loadUsers();
        }
    }
    
    /**
     * Handle unban user
     */
    private void handleUnbanUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            DialogUtils.showWarning("No User Selected", "Please select a user to unban first");
            return;
        }
        
        if (selectedUser.isActive()) {
            DialogUtils.showWarning("Operation Failed", "This user is not banned");
            return;
        }
        
        boolean confirm = DialogUtils.showConfirm("Confirm Unban", 
            "Are you sure you want to unban user " + selectedUser.getUsername() + "?");
        
        if (confirm) {
            userService.setUserActive(selectedUser.getId(), true);
            DialogUtils.showSuccess("User has been unbanned");
            loadUsers();
        }
    }
    
    /**
     * Handle delete user
     */
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            DialogUtils.showWarning("No User Selected", "Please select a user to delete first");
            return;
        }
        
        if (selectedUser.getId().equals(UserService.getCurrentUser().getId())) {
            DialogUtils.showWarning("Operation Failed", "You cannot delete yourself");
            return;
        }
        
        boolean confirm = DialogUtils.showConfirm("Confirm Delete", 
            "Are you sure you want to delete user " + selectedUser.getUsername() + "?\nThis action cannot be undone!");
        
        if (confirm) {
            userService.deleteUser(selectedUser.getId());
            DialogUtils.showSuccess("User has been deleted");
            loadUsers();
        }
    }
    
    /**
     * Handle view details
     */
    private void handleViewDetails() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            DialogUtils.showWarning("No User Selected", "Please select a user to view first");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText("User " + selectedUser.getUsername() + " Details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(String.valueOf(selectedUser.getId())), 1, 0);
        
        grid.add(new Label("Username:"), 0, 1);
        grid.add(new Label(selectedUser.getUsername()), 1, 1);
        
        grid.add(new Label("Email:"), 0, 2);
        grid.add(new Label(selectedUser.getEmail() != null ? selectedUser.getEmail() : "Not set"), 1, 2);
        
        grid.add(new Label("Role:"), 0, 3);
        grid.add(new Label(selectedUser.getRole().name()), 1, 3);
        
        grid.add(new Label("Status:"), 0, 4);
        Label statusLabel = new Label(selectedUser.isActive() ? "Active" : "Banned");
        statusLabel.setStyle("-fx-text-fill: " + (selectedUser.isActive() ? "#2ecc71" : "#e74c3c") + "; -fx-font-weight: bold;");
        grid.add(statusLabel, 1, 4);
        
        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }
}
