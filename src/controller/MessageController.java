package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Message;
import model.User;
import service.MessageService;
import service.UserService;
import util.DialogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message controller - manages user messages
 */
public class MessageController {

    private BorderPane mainLayout;
    private MessageService messageService;
    private UserService userService;
    private MainController mainController;
    private ListView<MessageItem> messageListView;
    private VBox conversationBox;
    private Long selectedUserId;
    
    public MessageController(BorderPane mainLayout, MainController mainController) {
        this.mainLayout = mainLayout;
        this.mainController = mainController;
        this.messageService = new MessageService();
        this.userService = new UserService();
    }
    
    /**
     * Show messages view
     */
    public void showMessagesView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Left: message list
        VBox leftPanel = createMessageListPanel();
        leftPanel.setPrefWidth(300);
        
        // Right: conversation details
        conversationBox = new VBox(15);
        conversationBox.setPadding(new Insets(20));
        conversationBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        Label emptyLabel = new Label("Please select a conversation");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        conversationBox.getChildren().add(emptyLabel);
        conversationBox.setAlignment(Pos.CENTER);
        
        root.setLeft(leftPanel);
        root.setCenter(conversationBox);
        
        mainLayout.setCenter(root);
        
        // Load message list
        loadMessageList();
    }
    
    /**
     * Create message list panel
     */
    private VBox createMessageListPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10;");
        
        Label titleLabel = new Label("Message List");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        messageListView = new ListView<>();
        messageListView.setCellFactory(lv -> new MessageListCell());
        messageListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    showConversation(newVal.userId);
                }
            }
        );
        
        VBox.setVgrow(messageListView, Priority.ALWAYS);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadMessageList());
        
        panel.getChildren().addAll(titleLabel, messageListView, refreshButton);
        
        return panel;
    }
    
    /**
     * Load message list
     */
    private void loadMessageList() {
        messageListView.getItems().clear();
        
        Long currentUserId = UserService.getCurrentUser().getId();
        List<Message> messages = messageService.getInbox(currentUserId);
        
        // Group by user, show only latest message for each user
        Map<Long, Message> latestMessages = new HashMap<>();
        for (Message msg : messages) {
            Long otherUserId = msg.getFromUserId();
            if (!latestMessages.containsKey(otherUserId)) {
                latestMessages.put(otherUserId, msg);
            }
        }
        
        // Also include sent messages
        // For simplification, only show received message senders list
        
        for (Map.Entry<Long, Message> entry : latestMessages.entrySet()) {
            User user = userService.getUserById(entry.getKey());
            Message msg = entry.getValue();
            
            if (user != null) {
                long unreadCount = messages.stream()
                    .filter(m -> m.getFromUserId().equals(entry.getKey()) && !m.isRead())
                    .count();
                
                messageListView.getItems().add(
                    new MessageItem(user.getId(), user.getUsername(), 
                                  msg.getContent(), unreadCount)
                );
            }
        }
        
        // Update main view unread message count (if available)
        if (mainController != null) {
            mainController.updateUnreadCount();
        }
    }
    
    /**
     * Show conversation
     */
    private void showConversation(Long otherUserId) {
        selectedUserId = otherUserId;
        conversationBox.getChildren().clear();
        conversationBox.setAlignment(Pos.TOP_LEFT);
        
        User otherUser = userService.getUserById(otherUserId);
        if (otherUser == null) {
            return;
        }
        
        // Top: other user's name
        Label userLabel = new Label("Conversation with " + otherUser.getUsername());
        userLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Conversation history
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");
        
        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
        
        Long currentUserId = UserService.getCurrentUser().getId();
        List<Message> conversation = messageService.getConversation(currentUserId, otherUserId);
        
        for (Message msg : conversation) {
            boolean isFromMe = msg.getFromUserId().equals(currentUserId);
            messagesBox.getChildren().add(createMessageBubble(msg, isFromMe));
        }
        
        scrollPane.setContent(messagesBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Bottom: send message
        HBox sendBox = new HBox(10);
        sendBox.setAlignment(Pos.CENTER);
        
        TextField messageField = new TextField();
        messageField.setPromptText("Type message...");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        sendButton.setOnAction(e -> {
            String content = messageField.getText().trim();
            if (!content.isEmpty()) {
                handleSendMessage(otherUserId, content);
                messageField.clear();
            }
        });
        
        messageField.setOnAction(e -> sendButton.fire());
        
        sendBox.getChildren().addAll(messageField, sendButton);
        
        conversationBox.getChildren().addAll(userLabel, new Separator(), scrollPane, sendBox);
        
        // Mark messages as read
        messageService.markConversationAsRead(currentUserId, otherUserId);
        loadMessageList(); // Refresh list to update unread count
    }
    
    /**
     * Create message bubble
     */
    private HBox createMessageBubble(Message msg, boolean isFromMe) {
        HBox bubble = new HBox();
        bubble.setPadding(new Insets(5));
        
        VBox messageBox = new VBox(5);
        messageBox.setMaxWidth(400);
        messageBox.setPadding(new Insets(10));
        messageBox.setStyle("-fx-background-color: " + (isFromMe ? "#3498db" : "#ecf0f1") + 
                           "; -fx-background-radius: 10;");
        
        Label contentLabel = new Label(msg.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: " + (isFromMe ? "white" : "black") + ";");
        
        Label timeLabel = new Label(msg.getCreatedTime());
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + 
                          (isFromMe ? "#ecf0f1" : "#7f8c8d") + ";");
        
        messageBox.getChildren().addAll(contentLabel, timeLabel);
        
        if (isFromMe) {
            bubble.setAlignment(Pos.CENTER_RIGHT);
            bubble.getChildren().add(messageBox);
        } else {
            bubble.setAlignment(Pos.CENTER_LEFT);
            bubble.getChildren().add(messageBox);
        }
        
        return bubble;
    }
    
    /**
     * Handle send message
     */
    private void handleSendMessage(Long toUserId, String content) {
        String error = messageService.sendMessage(
            UserService.getCurrentUser().getId(),
            toUserId,
            content
        );
        
        if (error != null) {
            DialogUtils.showError("Send Failed", error);
        } else {
            // Refresh conversation
            showConversation(toUserId);
        }
    }

    /**
     * Open messages view and directly show conversation with a specific user
     */
    public void openConversation(Long otherUserId) {
        // Show the messages view first (sets up left and right panes)
        showMessagesView();
        // Then display the conversation with the given user
        showConversation(otherUserId);
    }
    
    /**
     * Message list item
     */
    private static class MessageItem {
        Long userId;
        String username;
        String lastMessage;
        long unreadCount;
        
        MessageItem(Long userId, String username, String lastMessage, long unreadCount) {
            this.userId = userId;
            this.username = username;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
        }
    }
    
    /**
     * Message list cell
     */
    private static class MessageListCell extends ListCell<MessageItem> {
        @Override
        protected void updateItem(MessageItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox box = new VBox(5);
                box.setPadding(new Insets(10));
                
                HBox topBox = new HBox(10);
                topBox.setAlignment(Pos.CENTER_LEFT);
                
                Label nameLabel = new Label(item.username);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                if (item.unreadCount > 0) {
                    Label unreadLabel = new Label(String.valueOf(item.unreadCount));
                    unreadLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                       "-fx-background-radius: 10; -fx-padding: 2 6;");
                    topBox.getChildren().addAll(nameLabel, unreadLabel);
                } else {
                    topBox.getChildren().add(nameLabel);
                }
                
                Label msgLabel = new Label(item.lastMessage);
                msgLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
                msgLabel.setMaxWidth(250);
                msgLabel.setWrapText(false);
                
                box.getChildren().addAll(topBox, msgLabel);
                setGraphic(box);
            }
        }
    }
}
