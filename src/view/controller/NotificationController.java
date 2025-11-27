package view.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Notification;
import service.NotificationService;
import service.UserService;

import java.util.List;

public class NotificationController {

    private BorderPane mainLayout;
    private NotificationService notificationService;
    private MainController mainController;

    public NotificationController(BorderPane mainLayout, MainController mainController) {
        this.mainLayout = mainLayout;
        this.mainController = mainController;
        this.notificationService = new NotificationService();
    }

    public void showNotificationView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #ecf0f1;");

        // Header
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("My Notifications");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button markAllReadBtn = new Button("Mark All as Read");
        markAllReadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        markAllReadBtn.setOnAction(e -> {
            notificationService.markAllAsRead(UserService.getCurrentUser().getId());
            showNotificationView(); // Refresh
            mainController.updateUnreadCount(); // Update badge
        });
        
        headerBox.getChildren().addAll(titleLabel, spacer, markAllReadBtn);

        // Notification List
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox notificationList = new VBox(10);
        notificationList.setPadding(new Insets(10));
        
        List<Notification> notifications = notificationService.getUserNotifications(UserService.getCurrentUser().getId());
        
        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            notificationList.getChildren().add(emptyLabel);
        } else {
            for (Notification n : notifications) {
                notificationList.getChildren().add(createNotificationCard(n));
            }
        }
        
        scrollPane.setContent(notificationList);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().addAll(headerBox, scrollPane);
        mainLayout.setCenter(root);
    }

    private VBox createNotificationCard(Notification n) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        
        String bgStyle = n.isRead() ? "-fx-background-color: white;" : "-fx-background-color: #e8f6f3;";
        card.setStyle(bgStyle + " -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label(n.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label time = new Label(n.getCreatedTime());
        time.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
        
        header.getChildren().addAll(title, spacer, time);
        
        Label content = new Label(n.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #34495e; -fx-font-size: 14px;");
        
        card.getChildren().addAll(header, content);
        
        // Mark as read on click if unread
        if (!n.isRead()) {
            card.setOnMouseClicked(e -> {
                notificationService.markAsRead(n.getId());
                showNotificationView(); // Refresh
                mainController.updateUnreadCount();
            });
            card.setStyle(card.getStyle() + " -fx-cursor: hand;");
        }
        
        return card;
    }
}

