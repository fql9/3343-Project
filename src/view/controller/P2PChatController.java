package view.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.Message;
import model.PeerInfo;
import model.User;
import network.*;
import service.MessageService;
import service.UserService;
import view.util.DialogUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * P2P Chat Controller - manages real-time P2P chat interface.
 * Provides direct peer-to-peer messaging with live connection status.
 * Uses UDP broadcast for automatic LAN peer discovery.
 */
public class P2PChatController implements P2PMessageListener, P2PConnectionManager.PeerDiscoveryListener {
    
    private BorderPane mainLayout;
    private MainController mainController;
    private P2PService p2pService;
    private MessageService messageService;
    private UserService userService;
    
    // UI Components
    private ListView<PeerListItem> peerListView;
    private VBox chatBox;
    private ScrollPane chatScrollPane;
    private VBox messagesContainer;
    private TextField messageField;
    private Label connectionStatusLabel;
    private Label typingIndicatorLabel;
    private Label peerCountLabel;
    private Long selectedPeerId;
    
    // Track peer typing status
    private final Map<Long, Boolean> peerTypingStatus = new ConcurrentHashMap<>();
    
    /**
     * Constructor for P2P Chat Controller.
     * @param mainLayout Main application layout
     * @param mainController Main controller reference
     */
    public P2PChatController(BorderPane mainLayout, MainController mainController) {
        this.mainLayout = mainLayout;
        this.mainController = mainController;
        this.p2pService = P2PService.getInstance();
        this.messageService = new MessageService();
        this.userService = new UserService();
    }
    
    /**
     * Show the P2P chat view.
     * Initializes P2P service if needed and displays the chat interface.
     */
    public void showP2PChatView() {
        // Initialize P2P service if not already
        if (!p2pService.isInitialized()) {
            try {
                p2pService.initialize();
            } catch (IOException e) {
                DialogUtils.showError("P2P Error", 
                    "Failed to initialize P2P service: " + e.getMessage());
                return;
            }
        }
        
        // Add this controller as listeners
        p2pService.addMessageListener(this);
        p2pService.addDiscoveryListener(this);
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f6fa;");
        
        // Left: peer list panel
        VBox leftPanel = createPeerListPanel();
        leftPanel.setPrefWidth(300);
        
        // Center: chat area
        chatBox = createChatBox();
        
        root.setLeft(leftPanel);
        root.setCenter(chatBox);
        
        mainLayout.setCenter(root);
        
        // Load peer list
        loadPeerList();
        
        // Force announce presence
        p2pService.forceAnnounce();
    }
    
    /**
     * Create the peer list panel.
     * @return VBox containing peer list
     */
    private VBox createPeerListPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        panel.setEffect(new javafx.scene.effect.DropShadow(10, Color.gray(0.3)));
        
        // Header
        Label titleLabel = new Label("LAN Users (Auto-Discovery)");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Peer count
        peerCountLabel = new Label("Searching for peers...");
        peerCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
        
        // Connection info
        String localInfo = p2pService.getLocalAddressInfo();
        Label infoLabel = new Label("Your address: " + localInfo);
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        infoLabel.setWrapText(true);
        
        // Show broadcast addresses for debugging
        P2PBroadcastDiscovery discovery = p2pService.getConnectionManager().getBroadcastDiscovery();
        String broadcastInfo = discovery != null ? 
            "Broadcasting to: " + String.join(", ", discovery.getBroadcastAddressStrings()) : 
            "Broadcast not available";
        Label broadcastLabel = new Label(broadcastInfo);
        broadcastLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #95a5a6;");
        broadcastLabel.setWrapText(true);
        
        // Peer list
        peerListView = new ListView<>();
        peerListView.setCellFactory(lv -> new PeerListCell());
        peerListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null && newVal.userId != null) {
                    selectPeer(newVal.userId, newVal.ipAddress, newVal.port);
                }
            }
        );
        peerListView.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(peerListView, Priority.ALWAYS);
        
        // Button box
        HBox buttonBox = new HBox(5);
        
        // Refresh button
        Button refreshButton = new Button("⟳ Refresh");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(refreshButton, Priority.ALWAYS);
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                              "-fx-font-size: 12px; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> {
            p2pService.forceAnnounce();
            loadPeerList();
        });
        
        // Manual connect button
        Button manualButton = new Button("+ Manual");
        manualButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(manualButton, Priority.ALWAYS);
        manualButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; " +
                             "-fx-font-size: 12px; -fx-cursor: hand;");
        manualButton.setOnAction(e -> showManualConnectDialog());
        
        buttonBox.getChildren().addAll(refreshButton, manualButton);
        
        // Help text
        Label helpLabel = new Label("Peers on the same network will appear automatically. " +
                                   "Use 'Manual' to connect by IP if needed.");
        helpLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
        helpLabel.setWrapText(true);
        
        panel.getChildren().addAll(titleLabel, peerCountLabel, infoLabel, broadcastLabel,
                                   new Separator(), peerListView, buttonBox, helpLabel);
        
        return panel;
    }
    
    /**
     * Show manual connection dialog.
     */
    private void showManualConnectDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Manual P2P Connection");
        dialog.setHeaderText("Enter the peer's IP address and port");
        
        // Set the button types
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);
        
        // Create the fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField ipField = new TextField();
        ipField.setPromptText("e.g., 192.168.1.100");
        
        TextField portField = new TextField();
        portField.setPromptText("e.g., 50001");
        
        grid.add(new Label("IP Address:"), 0, 0);
        grid.add(ipField, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(portField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the IP field
        Platform.runLater(ipField::requestFocus);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return new String[]{ipField.getText(), portField.getText()};
            }
            return null;
        });
        
        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(values -> {
            try {
                String ip = values[0].trim();
                int port = Integer.parseInt(values[1].trim());
                
                if (ip.isEmpty()) {
                    DialogUtils.showError("Invalid Input", "IP address cannot be empty");
                    return;
                }
                
                // Try to connect
                connectManually(ip, port);
                
            } catch (NumberFormatException e) {
                DialogUtils.showError("Invalid Input", "Port must be a valid number");
            }
        });
    }
    
    /**
     * Connect to a peer manually by IP and port.
     * @param ip IP address
     * @param port Port number
     */
    private void connectManually(String ip, int port) {
        DialogUtils.showInfo("Connecting", "Attempting to connect to " + ip + ":" + port + "...");
        
        p2pService.connectToPeerManual(ip, port)
            .thenAccept(session -> {
                Platform.runLater(() -> {
                    DialogUtils.showInfo("Connected", 
                        "Successfully connected to " + session.getPeerName());
                    loadPeerList();
                    selectPeer(session.getPeerId(), ip, port);
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    DialogUtils.showError("Connection Failed", 
                        "Could not connect: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Create the main chat area.
     * @return VBox containing chat components
     */
    private VBox createChatBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        box.setEffect(new javafx.scene.effect.DropShadow(10, Color.gray(0.3)));
        BorderPane.setMargin(box, new Insets(0, 0, 0, 15));
        
        // Show empty state initially
        showEmptyChatState(box);
        
        return box;
    }
    
    /**
     * Show empty chat state when no peer is selected.
     * @param box Container to show state in
     */
    private void showEmptyChatState(VBox box) {
        box.getChildren().clear();
        box.setAlignment(Pos.CENTER);
        
        Label emptyLabel = new Label("Select a user to start chatting");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #bdc3c7;");
        
        Label hintLabel = new Label("Peers on your local network will appear automatically.\n" +
                                   "Make sure both devices are on the same WiFi/LAN.");
        hintLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #bdc3c7;");
        hintLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        box.getChildren().addAll(emptyLabel, hintLabel);
    }
    
    /**
     * Select a peer and show chat interface.
     * @param peerId Selected peer's user ID
     * @param ipAddress Peer's IP address
     * @param port Peer's port
     */
    private void selectPeer(Long peerId, String ipAddress, int port) {
        selectedPeerId = peerId;
        
        // Try to get user from local database, otherwise use peer info
        User peerUser = userService.getUserById(peerId);
        String peerName = peerUser != null ? peerUser.getUsername() : "User #" + peerId;
        
        chatBox.getChildren().clear();
        chatBox.setAlignment(Pos.TOP_LEFT);
        
        // Header with peer info and connection status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        Label nameLabel = new Label(peerName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label addressLabel = new Label("(" + ipAddress + ":" + port + ")");
        addressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        connectionStatusLabel = new Label();
        updateConnectionStatus(peerId);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button connectButton = new Button("Connect");
        connectButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        connectButton.setOnAction(e -> connectToPeer(peerId, ipAddress, port));
        
        header.getChildren().addAll(nameLabel, addressLabel, connectionStatusLabel, spacer, connectButton);
        
        // Typing indicator
        typingIndicatorLabel = new Label();
        typingIndicatorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-style: italic;");
        updateTypingIndicator(peerId);
        
        // Messages container
        messagesContainer = new VBox(8);
        messagesContainer.setPadding(new Insets(10));
        
        chatScrollPane = new ScrollPane(messagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);
        
        // Load existing conversation
        loadConversation(peerId);
        
        // Input area
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10, 0, 0, 0));
        
        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setStyle("-fx-font-size: 14px;");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        
        // Typing indicator on key press
        messageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (p2pService.isConnectedTo(peerId)) {
                p2pService.sendTypingIndicator(peerId, !newVal.isEmpty());
            }
        });
        
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                           "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 8 20;");
        sendButton.setOnAction(e -> sendMessage(ipAddress, port));
        
        messageField.setOnAction(e -> sendMessage(ipAddress, port));
        
        inputBox.getChildren().addAll(messageField, sendButton);
        
        chatBox.getChildren().addAll(header, typingIndicatorLabel, 
                                     new Separator(), chatScrollPane, inputBox);
    }
    
    /**
     * Legacy select peer method.
     */
    private void selectPeer(Long peerId) {
        // Try to find peer info
        List<PeerInfo> peers = p2pService.getDiscoveredPeers();
        for (PeerInfo peer : peers) {
            if (peer.getUserId().equals(peerId)) {
                selectPeer(peerId, peer.getIpAddress(), peer.getPort());
                return;
            }
        }
        // Fallback
        selectPeer(peerId, "unknown", 0);
    }
    
    /**
     * Load peer list from broadcast discovery.
     */
    private void loadPeerList() {
        peerListView.getItems().clear();
        
        // Get peers from broadcast discovery (LAN peers)
        List<PeerInfo> discoveredPeers = p2pService.getDiscoveredPeers();
        
        for (PeerInfo peer : discoveredPeers) {
            boolean connected = p2pService.isConnectedTo(peer.getUserId());
            peerListView.getItems().add(
                new PeerListItem(peer.getUserId(), peer.getUsername(), 
                               peer.getIpAddress(), peer.getPort(), connected)
            );
        }
        
        // Update peer count
        int count = peerListView.getItems().size();
        if (count == 0) {
            peerCountLabel.setText("No peers found on LAN");
            peerCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            
            // Show no peers message
            PeerListItem emptyItem = new PeerListItem(null, "Waiting for peers...", "", 0, false);
            peerListView.getItems().add(emptyItem);
        } else {
            peerCountLabel.setText(count + " peer(s) found on LAN");
            peerCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
        }
    }
    
    /**
     * Load conversation history with a peer.
     * @param peerId Peer's user ID
     */
    private void loadConversation(Long peerId) {
        messagesContainer.getChildren().clear();
        
        Long currentUserId = UserService.getCurrentUser().getId();
        List<Message> messages = messageService.getConversation(currentUserId, peerId);
        
        for (Message msg : messages) {
            boolean isFromMe = msg.getFromUserId().equals(currentUserId);
            addMessageBubble(msg.getContent(), msg.getCreatedTime(), isFromMe);
        }
        
        scrollToBottom();
    }
    
    /**
     * Add a message bubble to the chat.
     * @param content Message content
     * @param time Message timestamp
     * @param isFromMe Whether message is from current user
     */
    private void addMessageBubble(String content, String time, boolean isFromMe) {
        HBox bubble = new HBox();
        bubble.setPadding(new Insets(2));
        
        VBox messageBox = new VBox(3);
        messageBox.setMaxWidth(350);
        messageBox.setPadding(new Insets(10, 12, 10, 12));
        
        String bgColor = isFromMe ? "#3498db" : "#ecf0f1";
        String textColor = isFromMe ? "white" : "#2c3e50";
        String timeColor = isFromMe ? "#d6eaf8" : "#7f8c8d";
        String radius = isFromMe ? "15 15 5 15" : "15 15 15 5";
        
        messageBox.setStyle("-fx-background-color: " + bgColor + "; " +
                           "-fx-background-radius: " + radius + ";");
        
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px;");
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + timeColor + ";");
        
        messageBox.getChildren().addAll(contentLabel, timeLabel);
        
        if (isFromMe) {
            bubble.setAlignment(Pos.CENTER_RIGHT);
        } else {
            bubble.setAlignment(Pos.CENTER_LEFT);
        }
        
        bubble.getChildren().add(messageBox);
        messagesContainer.getChildren().add(bubble);
    }
    
    /**
     * Send a message to selected peer.
     * @param ipAddress Peer's IP address
     * @param port Peer's port
     */
    private void sendMessage(String ipAddress, int port) {
        if (selectedPeerId == null || messageField == null) return;
        
        String content = messageField.getText().trim();
        if (content.isEmpty()) return;
        
        messageField.clear();
        
        // Add bubble immediately for responsiveness
        String time = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        addMessageBubble(content, time, true);
        scrollToBottom();
        
        // Send via P2P service (also persists to database)
        p2pService.sendMessage(selectedPeerId, content)
            .thenAccept(sent -> {
                if (!sent) {
                    Platform.runLater(() -> {
                        DialogUtils.showWarning("Send Warning", 
                            "Message saved but P2P delivery failed. Click 'Connect' to establish connection first.");
                    });
                }
            });
        
        // Clear typing indicator
        if (p2pService.isConnectedTo(selectedPeerId)) {
            p2pService.sendTypingIndicator(selectedPeerId, false);
        }
    }
    
    /**
     * Connect to a peer via P2P.
     * @param peerId Peer's user ID
     * @param ipAddress Peer's IP address
     * @param port Peer's port
     */
    private void connectToPeer(Long peerId, String ipAddress, int port) {
        connectionStatusLabel.setText("Connecting...");
        connectionStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 12px;");
        
        p2pService.getConnectionManager().connectToPeer(ipAddress, port, peerId)
            .thenAccept(session -> {
                Platform.runLater(() -> {
                    updateConnectionStatus(peerId);
                    loadPeerList();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    updateConnectionStatus(peerId);
                    DialogUtils.showError("Connection Failed", 
                        "Could not connect: " + ex.getMessage() + 
                        "\n\nMake sure:\n" +
                        "1. Both devices are on the same network\n" +
                        "2. Firewall is not blocking port " + port + "\n" +
                        "3. The other user has P2P Chat open");
                });
                return null;
            });
    }
    
    /**
     * Update connection status label.
     * @param peerId Peer's user ID
     */
    private void updateConnectionStatus(Long peerId) {
        if (connectionStatusLabel == null) return;
        
        if (p2pService.isConnectedTo(peerId)) {
            connectionStatusLabel.setText("● Connected");
            connectionStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
        } else {
            connectionStatusLabel.setText("○ Not connected");
            connectionStatusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        }
    }
    
    /**
     * Update typing indicator based on peer status.
     * @param peerId Peer's user ID
     */
    private void updateTypingIndicator(Long peerId) {
        if (typingIndicatorLabel == null) return;
        
        Boolean isTyping = peerTypingStatus.get(peerId);
        if (Boolean.TRUE.equals(isTyping)) {
            typingIndicatorLabel.setText("typing...");
        } else {
            typingIndicatorLabel.setText("");
        }
    }
    
    /**
     * Scroll chat to bottom.
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (chatScrollPane != null) {
                chatScrollPane.setVvalue(1.0);
            }
        });
    }
    
    // P2PMessageListener implementation
    
    @Override
    public void onMessageReceived(P2PMessage message) {
        Platform.runLater(() -> {
            // If this message is from currently selected peer, add to chat
            if (message.getSenderId().equals(selectedPeerId)) {
                addMessageBubble(message.getContent(), message.getTimestamp(), false);
                scrollToBottom();
            }
            
            // Update main controller unread count
            if (mainController != null) {
                mainController.updateUnreadCount();
            }
        });
    }
    
    @Override
    public void onPeerConnected(Long peerId, String peerName) {
        Platform.runLater(() -> {
            loadPeerList();
            if (peerId.equals(selectedPeerId)) {
                updateConnectionStatus(peerId);
            }
        });
    }
    
    @Override
    public void onPeerDisconnected(Long peerId) {
        Platform.runLater(() -> {
            loadPeerList();
            if (peerId.equals(selectedPeerId)) {
                updateConnectionStatus(peerId);
            }
        });
    }
    
    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            DialogUtils.showError("P2P Error", error);
        });
    }
    
    @Override
    public void onTypingStatusChanged(Long peerId, boolean isTyping) {
        peerTypingStatus.put(peerId, isTyping);
        Platform.runLater(() -> {
            if (peerId.equals(selectedPeerId)) {
                updateTypingIndicator(peerId);
            }
        });
    }
    
    // PeerDiscoveryListener implementation
    
    @Override
    public void onPeerDiscovered(Long peerId, String peerName, String ipAddress, int port) {
        Platform.runLater(() -> {
            System.out.println("[P2P Chat] New peer discovered: " + peerName + " at " + ipAddress);
            loadPeerList();
        });
    }
    
    @Override
    public void onPeerLost(Long peerId) {
        Platform.runLater(this::loadPeerList);
    }
    
    /**
     * Cleanup when leaving this view.
     */
    public void cleanup() {
        p2pService.removeMessageListener(this);
        p2pService.removeDiscoveryListener(this);
    }
    
    /**
     * Open P2P chat with a specific user.
     * @param otherUserId User ID to chat with
     */
    public void openChatWith(Long otherUserId) {
        showP2PChatView();
        selectPeer(otherUserId);
    }
    
    // Inner classes for list items
    
    /**
     * Peer list item data class.
     */
    private static class PeerListItem {
        Long userId;
        String username;
        String ipAddress;
        int port;
        boolean connected;
        
        PeerListItem(Long userId, String username, String ipAddress, int port, boolean connected) {
            this.userId = userId;
            this.username = username;
            this.ipAddress = ipAddress;
            this.port = port;
            this.connected = connected;
        }
    }
    
    /**
     * Custom cell renderer for peer list.
     */
    private static class PeerListCell extends ListCell<PeerListItem> {
        @Override
        protected void updateItem(PeerListItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else if (item.userId == null) {
                // Empty state item
                setText(null);
                Label label = new Label(item.username);
                label.setStyle("-fx-text-fill: #bdc3c7; -fx-font-style: italic;");
                setGraphic(label);
            } else {
                setText(null);
                
                HBox box = new HBox(10);
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new Insets(8, 10, 8, 10));
                
                // Status indicator
                Circle statusCircle = new Circle(5);
                statusCircle.setFill(item.connected ? Color.web("#27ae60") : Color.web("#3498db"));
                
                // User info
                VBox infoBox = new VBox(2);
                
                Label nameLabel = new Label(item.username);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
                
                Label addressLabel = new Label(item.ipAddress + ":" + item.port);
                addressLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");
                
                Label statusLabel = new Label(item.connected ? "Connected" : "Available");
                statusLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: " + 
                                    (item.connected ? "#27ae60" : "#3498db") + ";");
                
                infoBox.getChildren().addAll(nameLabel, addressLabel, statusLabel);
                
                box.getChildren().addAll(statusCircle, infoBox);
                setGraphic(box);
            }
        }
    }
}
