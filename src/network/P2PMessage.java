package network;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * P2P message entity class representing a real-time chat message.
 * Implements Serializable for network transmission.
 */
public class P2PMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Message type enumeration for different kinds of P2P messages.
     */
    public enum MessageType {
        CHAT,           // Regular chat message
        HANDSHAKE,      // Connection handshake
        HEARTBEAT,      // Keep-alive signal
        DISCONNECT,     // Graceful disconnect
        TYPING,         // Typing indicator
        ACK             // Message acknowledgment
    }
    
    private MessageType type;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private String timestamp;
    private String messageId;
    
    /**
     * Default constructor.
     */
    public P2PMessage() {
        this.timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        this.messageId = generateMessageId();
    }
    
    /**
     * Constructor with basic parameters.
     * @param type Message type
     * @param senderId Sender user ID
     * @param senderName Sender username
     * @param content Message content
     */
    public P2PMessage(MessageType type, Long senderId, String senderName, String content) {
        this();
        this.type = type;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
    }
    
    /**
     * Generate a unique message ID.
     * @return Unique message ID string
     */
    private String generateMessageId() {
        return System.currentTimeMillis() + "-" + Math.random();
    }
    
    /**
     * Create a chat message.
     * @param senderId Sender user ID
     * @param senderName Sender username
     * @param receiverId Receiver user ID
     * @param content Message content
     * @return New P2PMessage instance
     */
    public static P2PMessage createChatMessage(Long senderId, String senderName, 
                                                Long receiverId, String content) {
        P2PMessage msg = new P2PMessage(MessageType.CHAT, senderId, senderName, content);
        msg.setReceiverId(receiverId);
        return msg;
    }
    
    /**
     * Create a handshake message for connection establishment.
     * @param senderId Sender user ID
     * @param senderName Sender username
     * @return New P2PMessage instance
     */
    public static P2PMessage createHandshake(Long senderId, String senderName) {
        return new P2PMessage(MessageType.HANDSHAKE, senderId, senderName, "HELLO");
    }
    
    /**
     * Create a heartbeat message for keep-alive.
     * @param senderId Sender user ID
     * @return New P2PMessage instance
     */
    public static P2PMessage createHeartbeat(Long senderId) {
        return new P2PMessage(MessageType.HEARTBEAT, senderId, null, "PING");
    }
    
    /**
     * Create a disconnect message for graceful disconnection.
     * @param senderId Sender user ID
     * @return New P2PMessage instance
     */
    public static P2PMessage createDisconnect(Long senderId) {
        return new P2PMessage(MessageType.DISCONNECT, senderId, null, "BYE");
    }
    
    /**
     * Create a typing indicator message.
     * @param senderId Sender user ID
     * @param isTyping Whether user is currently typing
     * @return New P2PMessage instance
     */
    public static P2PMessage createTypingIndicator(Long senderId, boolean isTyping) {
        return new P2PMessage(MessageType.TYPING, senderId, null, isTyping ? "1" : "0");
    }
    
    /**
     * Create an acknowledgment message.
     * @param senderId Sender user ID
     * @param originalMessageId ID of the message being acknowledged
     * @return New P2PMessage instance
     */
    public static P2PMessage createAck(Long senderId, String originalMessageId) {
        return new P2PMessage(MessageType.ACK, senderId, null, originalMessageId);
    }
    
    // Getters and Setters
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public Long getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    @Override
    public String toString() {
        return "P2PMessage{" +
                "type=" + type +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}

