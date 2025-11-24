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
    
    public LoginController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = new UserService();
    }
    
    /**
     * Show login view
     */
    public void showLoginView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498db, #2c3e50);");
        
        // Title
        Label titleLabel = new Label("Second-hand Trading Platform");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        
        Label subtitleLabel = new Label("Welcome Back");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ecf0f1;");
        
        // Login form
        GridPane loginForm = new GridPane();
        loginForm.setHgap(15);
        loginForm.setVgap(20);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setMaxWidth(400);
        loginForm.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 10);");
        
        String fieldStyle = "-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 8; -fx-font-size: 14px;";
        
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(250);
        usernameField.setStyle(fieldStyle);
        
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(250);
        passwordField.setStyle(fieldStyle);
        
        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameField, 1, 0);
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordField, 1, 1);
        
        // Button area
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        String btnBaseStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-cursor: hand;";
        
        Button loginButton = new Button("Login");
        loginButton.setStyle(btnBaseStyle + "-fx-background-color: #3498db; -fx-text-fill: white;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(btnBaseStyle + "-fx-background-color: #2980b9; -fx-text-fill: white;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(btnBaseStyle + "-fx-background-color: #3498db; -fx-text-fill: white;"));
        loginButton.setOnAction(e -> handleLogin());
        
        Button registerButton = new Button("Register");
        registerButton.setStyle(btnBaseStyle + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
        registerButton.setOnMouseEntered(e -> registerButton.setStyle(btnBaseStyle + "-fx-background-color: #27ae60; -fx-text-fill: white;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle(btnBaseStyle + "-fx-background-color: #2ecc71; -fx-text-fill: white;"));
        registerButton.setOnAction(e -> showRegisterView());
        
        buttonBox.getChildren().addAll(loginButton, registerButton);
        
        // Enter key for login
        passwordField.setOnAction(e -> handleLogin());
        
        root.getChildren().addAll(titleLabel, subtitleLabel, loginForm, buttonBox);
        
        Scene scene = new Scene(root, 800, 600);
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
        
        registerForm.add(usernameLabel, 0, 0);
        registerForm.add(usernameField, 1, 0);
        registerForm.add(passwordLabel, 0, 1);
        registerForm.add(passwordField, 1, 1);
        registerForm.add(emailLabel, 0, 2);
        registerForm.add(emailField, 1, 2);
        
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
        // Default role is SELLER (which includes BUYER capabilities)
        UserRole role = UserRole.SELLER;
        
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
