package service;

import dao.MessageDao;
import dao.impl.MessageDaoImpl;
import model.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Message service class - handles message-related business logic.
 * Provides messaging, inbox, and conversation management functions.
 */
public class MessageService {

    private final MessageDao messageDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public MessageService() {
        this.messageDao = new MessageDaoImpl();
    }
    
    /**
     * Send message
     * @param fromUserId Sender ID
     * @param toUserId Receiver ID
     * @param content Message content
     * @return Send result message
     */
    public String sendMessage(Long fromUserId, Long toUserId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Message content cannot be empty";
        }
        
        if (fromUserId.equals(toUserId)) {
            return "Cannot send message to yourself";
        }
        
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        message.setRead(false);
        
        messageDao.save(message);
        return null; // Success returns null
    }
    
    /**
     * Get inbox (received messages)
     * @param userId User ID
     * @return Message list
     */
    public List<Message> getInbox(Long userId) {
        return messageDao.findInbox(userId);
    }
    
    /**
     * Get unread message count
     * @param userId User ID
     * @return Unread message count
     */
    public long getUnreadCount(Long userId) {
        return messageDao.findInbox(userId).stream()
            .filter(msg -> !msg.isRead())
            .count();
    }
    
    /**
     * Get conversation between two users
     * @param user1 User 1 ID
     * @param user2 User 2 ID
     * @return Message list
     */
    public List<Message> getConversation(Long user1, Long user2) {
        return messageDao.findConversation(user1, user2);
    }
    
    /**
     * Mark message as read
     * @param messageId Message ID
     */
    public void markAsRead(Long messageId) {
        messageDao.markRead(messageId);
    }
    
    /**
     * Mark all messages from a user as read
     * @param currentUserId Current user ID
     * @param fromUserId Sender ID
     */
    public void markConversationAsRead(Long currentUserId, Long fromUserId) {
        List<Message> messages = messageDao.findInbox(currentUserId);
        messages.stream()
            .filter(msg -> msg.getFromUserId().equals(fromUserId) && !msg.isRead())
            .forEach(msg -> messageDao.markRead(msg.getId()));
    }
    
    /**
     * Delete message
     * @param messageId Message ID
     */
    public void deleteMessage(Long messageId) {
        messageDao.delete(messageId);
    }
    
    /**
     * Get message by ID
     * @param messageId Message ID
     * @return Message object
     */
    public Message getMessageById(Long messageId) {
        return messageDao.findById(messageId);
    }
}
