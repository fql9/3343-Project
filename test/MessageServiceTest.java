
import config.DatabaseConfig;
import model.Message;
import model.UserRole;
import service.MessageService;
import service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

    private MessageService messageService;
    private UserService userService;
    private Long testUser1Id;
    private Long testUser2Id;
    private Long testUser3Id;

    @BeforeAll
    static void setupDatabase() {
        // Ensure database is initialized
        DatabaseConfig.initDatabase();
    }

    @BeforeEach
    void setUp() {
        messageService = new MessageService();
        userService = new UserService();
        
        // Clean up test data
        cleanupTestData();
        
        // Create test users
        userService.register("msg_test_user1", "password123", "user1@test.com", UserRole.BUYER);
        userService.login("msg_test_user1", "password123");
        testUser1Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.register("msg_test_user2", "password123", "user2@test.com", UserRole.SELLER);
        userService.login("msg_test_user2", "password123");
        testUser2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.register("msg_test_user3", "password123", "user3@test.com", UserRole.BUYER);
        userService.login("msg_test_user3", "password123");
        testUser3Id = UserService.getCurrentUser().getId();
        userService.logout();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try (java.sql.Connection conn = DatabaseConfig.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            // Delete test messages
            stmt.executeUpdate("DELETE FROM messages WHERE from_user_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'msg_test_%')");
            stmt.executeUpdate("DELETE FROM messages WHERE to_user_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'msg_test_%')");
            // Delete test users
            stmt.executeUpdate("DELETE FROM users WHERE username LIKE 'msg_test_%'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSendMessage_Success() {
        // Act
        String result = messageService.sendMessage(testUser1Id, testUser2Id, "Hello, this is a test message");

        // Assert
        assertNull(result, "Sending message should succeed");
        
        // Verify message was saved
        List<Message> inbox = messageService.getInbox(testUser2Id);
        assertFalse(inbox.isEmpty(), "Inbox should contain the message");
        assertEquals("Hello, this is a test message", inbox.get(0).getContent());
        assertEquals(testUser1Id, inbox.get(0).getFromUserId());
        assertEquals(testUser2Id, inbox.get(0).getToUserId());
        assertFalse(inbox.get(0).isRead(), "New message should be unread");
    }

    @Test
    void testSendMessage_EmptyContent() {
        // Act
        String result = messageService.sendMessage(testUser1Id, testUser2Id, "");

        // Assert
        assertEquals("Message content cannot be empty", result);
    }

    @Test
    void testSendMessage_NullContent() {
        // Act
        String result = messageService.sendMessage(testUser1Id, testUser2Id, null);

        // Assert
        assertEquals("Message content cannot be empty", result);
    }

    @Test
    void testSendMessage_WhitespaceContent() {
        // Act
        String result = messageService.sendMessage(testUser1Id, testUser2Id, "   ");

        // Assert
        assertEquals("Message content cannot be empty", result);
    }

    @Test
    void testSendMessage_ToSelf() {
        // Act
        String result = messageService.sendMessage(testUser1Id, testUser1Id, "Message to myself");

        // Assert
        assertEquals("Cannot send message to yourself", result);
    }

    @Test
    void testGetInbox() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 1");
        messageService.sendMessage(testUser3Id, testUser2Id, "Message 2");
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 3");

        // Act
        List<Message> inbox = messageService.getInbox(testUser2Id);

        // Assert
        assertNotNull(inbox);
        assertEquals(3, inbox.size());
    }

    @Test
    void testGetInbox_Empty() {
        // Act
        List<Message> inbox = messageService.getInbox(testUser1Id);

        // Assert
        assertNotNull(inbox);
        assertTrue(inbox.isEmpty());
    }

    @Test
    void testGetUnreadCount() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 1");
        messageService.sendMessage(testUser3Id, testUser2Id, "Message 2");
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 3");

        // Act
        long unreadCount = messageService.getUnreadCount(testUser2Id);

        // Assert
        assertEquals(3, unreadCount);
    }

    @Test
    void testGetUnreadCount_AfterReading() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 1");
        messageService.sendMessage(testUser3Id, testUser2Id, "Message 2");
        List<Message> inbox = messageService.getInbox(testUser2Id);
        messageService.markAsRead(inbox.get(0).getId());

        // Act
        long unreadCount = messageService.getUnreadCount(testUser2Id);

        // Assert
        assertEquals(1, unreadCount);
    }

    @Test
    void testGetUnreadCount_NoMessages() {
        // Act
        long unreadCount = messageService.getUnreadCount(testUser1Id);

        // Assert
        assertEquals(0, unreadCount);
    }

    @Test
    void testGetConversation() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 1 from user1");
        messageService.sendMessage(testUser2Id, testUser1Id, "Reply from user2");
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 2 from user1");
        messageService.sendMessage(testUser3Id, testUser1Id, "Message from user3"); // Should not be in conversation

        // Act
        List<Message> conversation = messageService.getConversation(testUser1Id, testUser2Id);

        // Assert
        assertNotNull(conversation);
        assertEquals(3, conversation.size());
        
        // Verify all messages are between user1 and user2
        for (Message msg : conversation) {
            assertTrue((msg.getFromUserId().equals(testUser1Id) && msg.getToUserId().equals(testUser2Id)) ||
                      (msg.getFromUserId().equals(testUser2Id) && msg.getToUserId().equals(testUser1Id)));
        }
    }

    @Test
    void testGetConversation_NoMessages() {
        // Act
        List<Message> conversation = messageService.getConversation(testUser1Id, testUser2Id);

        // Assert
        assertNotNull(conversation);
        assertTrue(conversation.isEmpty());
    }

    @Test
    void testMarkAsRead() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Test message");
        List<Message> inbox = messageService.getInbox(testUser2Id);
        Long messageId = inbox.get(0).getId();
        assertFalse(inbox.get(0).isRead(), "Message should be unread initially");

        // Act
        messageService.markAsRead(messageId);

        // Assert
        Message message = messageService.getMessageById(messageId);
        assertTrue(message.isRead(), "Message should be marked as read");
    }

    @Test
    void testMarkConversationAsRead() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 1");
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 2");
        messageService.sendMessage(testUser3Id, testUser2Id, "Message from user3");
        
        assertEquals(3, messageService.getUnreadCount(testUser2Id));

        // Act
        messageService.markConversationAsRead(testUser2Id, testUser1Id);

        // Assert
        long unreadCount = messageService.getUnreadCount(testUser2Id);
        assertEquals(1, unreadCount, "Only message from user3 should remain unread");
        
        // Verify messages from user1 are read
        List<Message> conversation = messageService.getConversation(testUser1Id, testUser2Id);
        for (Message msg : conversation) {
            if (msg.getFromUserId().equals(testUser1Id)) {
                assertTrue(msg.isRead(), "Messages from user1 should be marked as read");
            }
        }
    }

    @Test
    void testMarkConversationAsRead_NoUnreadMessages() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Message 1");
        List<Message> inbox = messageService.getInbox(testUser2Id);
        messageService.markAsRead(inbox.get(0).getId());

        // Act - should not throw exception
        assertDoesNotThrow(() -> messageService.markConversationAsRead(testUser2Id, testUser1Id));
    }

    @Test
    void testDeleteMessage() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Message to delete");
        List<Message> inbox = messageService.getInbox(testUser2Id);
        Long messageId = inbox.get(0).getId();
        assertEquals(1, inbox.size());

        // Act
        messageService.deleteMessage(messageId);

        // Assert
        Message deletedMessage = messageService.getMessageById(messageId);
        assertNull(deletedMessage, "Message should be deleted");
        
        List<Message> inboxAfterDelete = messageService.getInbox(testUser2Id);
        assertTrue(inboxAfterDelete.isEmpty(), "Inbox should be empty after deletion");
    }

    @Test
    void testDeleteMessage_NonExistent() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> messageService.deleteMessage(99999L));
    }

    @Test
    void testGetMessageById() {
        // Arrange
        messageService.sendMessage(testUser1Id, testUser2Id, "Test message content");
        List<Message> inbox = messageService.getInbox(testUser2Id);
        Long messageId = inbox.get(0).getId();

        // Act
        Message message = messageService.getMessageById(messageId);

        // Assert
        assertNotNull(message);
        assertEquals(messageId, message.getId());
        assertEquals("Test message content", message.getContent());
        assertEquals(testUser1Id, message.getFromUserId());
        assertEquals(testUser2Id, message.getToUserId());
    }

    @Test
    void testGetMessageById_NotFound() {
        // Act
        Message message = messageService.getMessageById(99999L);

        // Assert
        assertNull(message);
    }

    @Test
    void testMultipleConversations() {
        // Arrange - Create messages between multiple users
        messageService.sendMessage(testUser1Id, testUser2Id, "User1 to User2");
        messageService.sendMessage(testUser2Id, testUser1Id, "User2 to User1");
        messageService.sendMessage(testUser1Id, testUser3Id, "User1 to User3");
        messageService.sendMessage(testUser3Id, testUser1Id, "User3 to User1");

        // Act
        List<Message> conversation1_2 = messageService.getConversation(testUser1Id, testUser2Id);
        List<Message> conversation1_3 = messageService.getConversation(testUser1Id, testUser3Id);

        // Assert
        assertEquals(2, conversation1_2.size());
        assertEquals(2, conversation1_3.size());
    }

    @Test
    void testMessageTimestamp() {
        // Arrange & Act
        messageService.sendMessage(testUser1Id, testUser2Id, "Test message");
        List<Message> inbox = messageService.getInbox(testUser2Id);

        // Assert
        assertNotNull(inbox.get(0).getCreatedTime());
        assertFalse(inbox.get(0).getCreatedTime().isEmpty());
        // Verify timestamp format (yyyy-MM-dd HH:mm:ss)
        assertTrue(inbox.get(0).getCreatedTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testLongMessageContent() {
        // Arrange
        String longMessage = "This is a very long message. ".repeat(50);

        // Act
        String result = messageService.sendMessage(testUser1Id, testUser2Id, longMessage);

        // Assert
        assertNull(result);
        List<Message> inbox = messageService.getInbox(testUser2Id);
        assertEquals(longMessage, inbox.get(0).getContent());
    }

    @Test
    void testSpecialCharactersInMessage() {
        // Arrange
        String specialMessage = "Special chars: @#$%^&*()_+-=[]{}|;':\",./<>?`~";

        // Act
        String result = messageService.sendMessage(testUser1Id, testUser2Id, specialMessage);

        // Assert
        assertNull(result);
        List<Message> inbox = messageService.getInbox(testUser2Id);
        assertEquals(specialMessage, inbox.get(0).getContent());
    }

    @Test
    void testUnicodeInMessage() {
        // Arrange
        String unicodeMessage = "Hello 你好 こんにちは 안녕하세요 🎉🎊";

        // Act
        String result = messageService.sendMessage(testUser1Id, testUser2Id, unicodeMessage);

        // Assert
        assertNull(result);
        List<Message> inbox = messageService.getInbox(testUser2Id);
        assertEquals(unicodeMessage, inbox.get(0).getContent());
    }
}
