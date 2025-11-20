package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.UserRole;
import service.UserService;
import util.DialogUtils;

/**
 * Login controller - handles user login and registration
 */
public class LoginController {

    private Stage primaryStage;
    private UserService userService;
    
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField emailField;
    private ComboBox<UserRole> roleComboBox;
    
    public LoginController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = new UserService();
    }
    
    /**
     * Show login view
     */
    public void showLoginView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Second-hand Trading Platform");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Welcome to Login");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        
        // Login form
        GridPane loginForm = new GridPane();
        loginForm.setHgap(10);
        loginForm.setVgap(15);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setMaxWidth(400);
        loginForm.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10;");
        
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefWidth(250);
        
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefWidth(250);
        
        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameField, 1, 0);
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordField, 1, 1);
        
        // Button area
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        loginButton.setOnAction(e -> handleLogin());
        
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        registerButton.setOnAction(e -> showRegisterView());
        
        buttonBox.getChildren().addAll(loginButton, registerButton);
        
        // Enter key for login
        passwordField.setOnAction(e -> handleLogin());
        
        root.getChildren().addAll(titleLabel, subtitleLabel, loginForm, buttonBox);
        
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login - Second-hand Trading Platform");
        primaryStage.show();
    }
    
    /**
     * Show register view
     */
    public void showRegisterView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("User Registration");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Register form
        GridPane registerForm = new GridPane();
        registerForm.setHgap(10);
        registerForm.setVgap(15);
        registerForm.setAlignment(Pos.CENTER);
        registerForm.setMaxWidth(450);
        registerForm.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10;");
        
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("3-20 chars, letters/numbers/underscore");
        usernameField.setPrefWidth(280);
        
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("At least 6 characters");
        passwordField.setPrefWidth(280);
        
        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        emailField.setPromptText("Optional");
        emailField.setPrefWidth(280);
        
        Label roleLabel = new Label("Role:");
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(UserRole.BUYER, UserRole.SELLER);
        roleComboBox.setValue(UserRole.BUYER);
        roleComboBox.setPrefWidth(280);
        
        registerForm.add(usernameLabel, 0, 0);
        registerForm.add(usernameField, 1, 0);
        registerForm.add(passwordLabel, 0, 1);
        registerForm.add(passwordField, 1, 1);
        registerForm.add(emailLabel, 0, 2);
        registerForm.add(emailField, 1, 2);
        registerForm.add(roleLabel, 0, 3);
        registerForm.add(roleComboBox, 1, 3);
        
        // Button area
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        registerButton.setOnAction(e -> handleRegister());
        
        Button backButton = new Button("Back to Login");
        backButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        backButton.setOnAction(e -> showLoginView());
        
        buttonBox.getChildren().addAll(registerButton, backButton);
        
        root.getChildren().addAll(titleLabel, registerForm, buttonBox);
        
        Scene scene = new Scene(root, 650, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Register - Second-hand Trading Platform");
    }
    
    /**
     * Handle login
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            DialogUtils.showWarning("Input Error", "Please enter username and password");
            return;
        }
        
        String error = userService.login(username, password);
        if (error != null) {
            DialogUtils.showError("Login Failed", error);
            return;
        }
        
        DialogUtils.showSuccess("Login successful!");
        
        // Navigate to main view
        MainController mainController = new MainController(primaryStage);
        mainController.showMainView();
    }
    
    /**
     * Handle registration
     */
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String email = emailField.getText().trim();
        UserRole role = roleComboBox.getValue();
        
        if (username.isEmpty() || password.isEmpty()) {
            DialogUtils.showWarning("Input Error", "Username and password cannot be empty");
            return;
        }
        
        String error = userService.register(username, password, email, role);
        if (error != null) {
            DialogUtils.showError("Registration Failed", error);
            return;
        }
        
        DialogUtils.showSuccess("Registration successful! Please login");
        showLoginView();
    }
}
