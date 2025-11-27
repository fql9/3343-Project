package view.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import model.User;
import service.UserService;
import view.util.DialogUtils;

import java.io.File;

/**
 * User profile controller - displays and edits user profile.
 * Provides profile editing, avatar upload, and password change functions.
 */
public class UserProfileController {

    private BorderPane mainLayout;
    private UserService userService;
    private ImageView avatarView;
    private String currentAvatarUrl;
    
    public UserProfileController(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        this.userService = new UserService();
    }
    
    /**
     * Show user profile view.
     * Creates and displays the profile editing interface.
     */
    public void showUserProfileView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #ecf0f1;");
        
        // Header
        Label titleLabel = new Label("My Profile");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Profile Card
        VBox profileCard = new VBox(20);
        profileCard.setPadding(new Insets(30));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        profileCard.setMaxWidth(600);
        
        User user = UserService.getCurrentUser();
        currentAvatarUrl = user.getAvatarUrl();
        
        // Avatar Section
        HBox avatarBox = new HBox(20);
        avatarBox.setAlignment(Pos.CENTER_LEFT);
        
        avatarView = new ImageView();
        avatarView.setFitWidth(100);
        avatarView.setFitHeight(100);
        avatarView.setPreserveRatio(true);
        updateAvatarDisplay();
        
        VBox avatarControls = new VBox(10);
        avatarControls.setAlignment(Pos.CENTER_LEFT);
        
        Button uploadBtn = new Button("Upload Avatar");
        uploadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        uploadBtn.setOnAction(e -> handleUploadAvatar());
        
        Label joinDateLabel = new Label("Joined: " + (user.getCreatedTime() != null ? user.getCreatedTime() : "Unknown"));
        joinDateLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
        
        avatarControls.getChildren().addAll(uploadBtn, joinDateLabel);
        avatarBox.getChildren().addAll(avatarView, avatarControls);
        
        // Stats Section
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        VBox salesBox = createStatBox("Total Sales", String.format("¥%.2f", userService.getTotalSalesAmount()));
        VBox purchaseBox = createStatBox("Total Purchases", String.format("¥%.2f", userService.getTotalPurchaseAmount()));
        
        statsBox.getChildren().addAll(salesBox, new Separator(javafx.geometry.Orientation.VERTICAL), purchaseBox);

        // Username (Read-only)
        VBox usernameBox = createField("Username", user.getUsername(), false);
        
        // Role (Read-only)
        VBox roleBox = createField("Role", user.getRole().name(), false);
        
        // Email (Editable)
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        TextField emailField = new TextField(user.getEmail());
        emailField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        emailBox.getChildren().addAll(emailLabel, emailField);
        
        // Bio (Editable)
        VBox bioBox = new VBox(5);
        Label bioLabel = new Label("Bio");
        bioLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        TextArea bioArea = new TextArea(user.getBio());
        bioArea.setPromptText("Tell us about yourself...");
        bioArea.setPrefRowCount(3);
        bioArea.setWrapText(true);
        bioArea.setStyle("-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        bioBox.getChildren().addAll(bioLabel, bioArea);
        
        // Save Profile Button
        Button saveButton = new Button("Update Profile");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        saveButton.setOnAction(e -> handleUpdateProfile(emailField.getText(), bioArea.getText()));
        
        HBox saveBox = new HBox(saveButton);
        saveBox.setAlignment(Pos.CENTER_RIGHT);
        
        profileCard.getChildren().addAll(avatarBox, statsBox, usernameBox, roleBox, emailBox, bioBox, saveBox);
        
        // Password Change Section
        VBox passwordCard = new VBox(20);
        passwordCard.setPadding(new Insets(30));
        passwordCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        passwordCard.setMaxWidth(600);
        
        Label passwordTitle = new Label("Change Password");
        passwordTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Current Password");
        oldPassField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New Password");
        newPassField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm New Password");
        confirmPassField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        Button changePassButton = new Button("Change Password");
        changePassButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        changePassButton.setOnAction(e -> handleChangePassword(oldPassField.getText(), newPassField.getText(), confirmPassField.getText()));
        
        HBox passBtnBox = new HBox(changePassButton);
        passBtnBox.setAlignment(Pos.CENTER_RIGHT);
        
        passwordCard.getChildren().addAll(passwordTitle, oldPassField, newPassField, confirmPassField, passBtnBox);
        
        // Center alignment wrapper
        VBox contentWrapper = new VBox(20);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.getChildren().addAll(profileCard, passwordCard);
        
        ScrollPane scrollPane = new ScrollPane(contentWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0, 0, 20, 0));
        
        root.getChildren().addAll(titleLabel, scrollPane);
        mainLayout.setCenter(root);
    }
    
    private VBox createStatBox(String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        box.getChildren().addAll(valLabel, titleLabel);
        return box;
    }
    
    private void updateAvatarDisplay() {
        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
            try {
                File file = new File(currentAvatarUrl);
                if (file.exists()) {
                    avatarView.setImage(new Image(file.toURI().toString()));
                } else {
                    // Fallback or default
                    avatarView.setImage(null); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Default placeholder could go here
            avatarView.setImage(null);
        }
    }
    
    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Avatar Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(mainLayout.getScene().getWindow());
        if (selectedFile != null) {
            currentAvatarUrl = selectedFile.getAbsolutePath();
            updateAvatarDisplay();
        }
    }
    
    private VBox createField(String label, String value, boolean editable) {
        VBox box = new VBox(5);
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        
        Control field;
        if (editable) {
            field = new TextField(value);
            field.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        } else {
            field = new Label(value);
            field.setStyle("-fx-padding: 10; -fx-background-color: #f5f6fa; -fx-background-radius: 5; -fx-min-width: 200;");
            ((Label)field).setWrapText(true);
        }
        
        box.getChildren().addAll(l, field);
        return box;
    }
    
    private void handleUpdateProfile(String email, String bio) {
        String error = userService.updateProfile(email, bio, currentAvatarUrl);
        if (error != null) {
            DialogUtils.showError("Update Failed", error);
        } else {
            DialogUtils.showSuccess("Profile updated successfully!");
        }
    }
    
    private void handleChangePassword(String oldPass, String newPass, String confirmPass) {
        if (!newPass.equals(confirmPass)) {
            DialogUtils.showError("Password Mismatch", "New password and confirmation do not match");
            return;
        }
        
        String error = userService.changePassword(oldPass, newPass);
        if (error != null) {
            DialogUtils.showError("Change Failed", error);
        } else {
            DialogUtils.showSuccess("Password changed successfully!");
        }
    }
}

