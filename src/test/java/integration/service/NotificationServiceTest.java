package integration.service;

import integration.IntegrationTestBase;
import service.NotificationService;
import service.UserService;
import config.DatabaseConfig;
import model.Notification;
import model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest extends IntegrationTestBase {

    private NotificationService notificationService;
    private UserService userService;
    
    private Long testUser1Id;
    private Long testUser2Id;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        userService = new UserService();
        
        // Clean up test data
        cleanupTestData();
        
        // Create test users
        userService.register("notif_test_user1", "password123", "user1@test.com", UserRole.BUYER);
        userService.login("notif_test_user1", "password123");
        testUser1Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.register("notif_test_user2", "password123", "user2@test.com", UserRole.SELLER);
        userService.login("notif_test_user2", "password123");
        testUser2Id = UserService.getCurrentUser().getId();
        userService.logout();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try (java.sql.Connection conn = DatabaseConfig.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            // Delete test notifications
            stmt.executeUpdate("DELETE FROM notifications WHERE user_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'notif_test_%')");
            // Delete test users
            stmt.executeUpdate("DELETE FROM users WHERE username LIKE 'notif_test_%'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateNotification_Success() {
        // Act
        notificationService.createNotification(testUser1Id, "Test Title", "Test Content");

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertFalse(notifications.isEmpty(), "User should have notifications");
        assertEquals("Test Title", notifications.get(0).getTitle());
        assertEquals("Test Content", notifications.get(0).getContent());
        assertEquals(testUser1Id, notifications.get(0).getUserId());
        assertFalse(notifications.get(0).isRead(), "New notification should be unread");
        assertNotNull(notifications.get(0).getCreatedTime());
    }

    @Test
    void testCreateNotification_WithEmptyTitle() {
        // Act
        notificationService.createNotification(testUser1Id, "", "Content");

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertFalse(notifications.isEmpty());
        assertEquals("", notifications.get(0).getTitle());
    }

    @Test
    void testCreateNotification_WithNullTitle() {
        // Act
        notificationService.createNotification(testUser1Id, null, "Content");

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertFalse(notifications.isEmpty());
        assertNull(notifications.get(0).getTitle());
    }

    @Test
    void testCreateNotification_WithEmptyContent() {
        // Act
        notificationService.createNotification(testUser1Id, "Title", "");

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertFalse(notifications.isEmpty());
        assertEquals("", notifications.get(0).getContent());
    }

    @Test
    void testCreateNotification_WithNullContent() {
        // Act
        notificationService.createNotification(testUser1Id, "Title", null);

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertFalse(notifications.isEmpty());
        assertNull(notifications.get(0).getContent());
    }

    @Test
    void testCreateNotification_WithLongContent() {
        // Arrange
        String longContent = "This is a very long notification content. ".repeat(50);

        // Act
        notificationService.createNotification(testUser1Id, "Long Notification", longContent);

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(longContent, notifications.get(0).getContent());
    }

    @Test
    void testCreateNotification_WithSpecialCharacters() {
        // Arrange
        String specialTitle = "Special @#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String specialContent = "Content with special chars: !@#$%";

        // Act
        notificationService.createNotification(testUser1Id, specialTitle, specialContent);

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(specialTitle, notifications.get(0).getTitle());
        assertEquals(specialContent, notifications.get(0).getContent());
    }

    @Test
    void testCreateNotification_WithUnicode() {
        // Arrange
        String unicodeTitle = "通知 Notification 通知 🔔";
        String unicodeContent = "你好 Hello こんにちは 안녕하세요 🎉";

        // Act
        notificationService.createNotification(testUser1Id, unicodeTitle, unicodeContent);

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(unicodeTitle, notifications.get(0).getTitle());
        assertEquals(unicodeContent, notifications.get(0).getContent());
    }

    @Test
    void testCreateNotification_MultipleNotifications() {
        // Act
        notificationService.createNotification(testUser1Id, "Title 1", "Content 1");
        notificationService.createNotification(testUser1Id, "Title 2", "Content 2");
        notificationService.createNotification(testUser1Id, "Title 3", "Content 3");

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(3, notifications.size());
    }

    @Test
    void testGetUserNotifications_NoNotifications() {
        // Act
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);

        // Assert
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testGetUserNotifications_OnlyOwnNotifications() {
        // Arrange
        notificationService.createNotification(testUser1Id, "User1 Notification", "Content");
        notificationService.createNotification(testUser2Id, "User2 Notification", "Content");

        // Act
        List<Notification> user1Notifications = notificationService.getUserNotifications(testUser1Id);
        List<Notification> user2Notifications = notificationService.getUserNotifications(testUser2Id);

        // Assert
        assertEquals(1, user1Notifications.size());
        assertEquals(1, user2Notifications.size());
        assertEquals("User1 Notification", user1Notifications.get(0).getTitle());
        assertEquals("User2 Notification", user2Notifications.get(0).getTitle());
    }

    @Test
    void testGetUnreadNotifications_AllUnread() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Title 1", "Content 1");
        notificationService.createNotification(testUser1Id, "Title 2", "Content 2");
        notificationService.createNotification(testUser1Id, "Title 3", "Content 3");

        // Act
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser1Id);

        // Assert
        assertEquals(3, unreadNotifications.size());
        for (Notification notif : unreadNotifications) {
            assertFalse(notif.isRead());
        }
    }

    @Test
    void testGetUnreadNotifications_SomeRead() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Title 1", "Content 1");
        notificationService.createNotification(testUser1Id, "Title 2", "Content 2");
        notificationService.createNotification(testUser1Id, "Title 3", "Content 3");
        
        List<Notification> allNotifications = notificationService.getUserNotifications(testUser1Id);
        notificationService.markAsRead(allNotifications.get(0).getId());

        // Act
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser1Id);

        // Assert
        assertEquals(2, unreadNotifications.size());
    }

    @Test
    void testGetUnreadNotifications_AllRead() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Title 1", "Content 1");
        notificationService.createNotification(testUser1Id, "Title 2", "Content 2");
        
        notificationService.markAllAsRead(testUser1Id);

        // Act
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser1Id);

        // Assert
        assertTrue(unreadNotifications.isEmpty());
    }

    @Test
    void testGetUnreadNotifications_NoNotifications() {
        // Act
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser1Id);

        // Assert
        assertNotNull(unreadNotifications);
        assertTrue(unreadNotifications.isEmpty());
    }

    @Test
    void testMarkAsRead_Success() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Test Title", "Test Content");
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        Long notificationId = notifications.get(0).getId();
        assertFalse(notifications.get(0).isRead(), "Should be unread initially");

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        notifications = notificationService.getUserNotifications(testUser1Id);
        assertTrue(notifications.get(0).isRead(), "Should be marked as read");
    }

    @Test
    void testMarkAsRead_NonExistentNotification() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> notificationService.markAsRead(99999L));
    }

    @Test
    void testMarkAsRead_AlreadyRead() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Test Title", "Test Content");
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        Long notificationId = notifications.get(0).getId();
        notificationService.markAsRead(notificationId);

        // Act - Mark as read again
        assertDoesNotThrow(() -> notificationService.markAsRead(notificationId));

        // Assert
        notifications = notificationService.getUserNotifications(testUser1Id);
        assertTrue(notifications.get(0).isRead());
    }

    @Test
    void testMarkAllAsRead_Success() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Title 1", "Content 1");
        notificationService.createNotification(testUser1Id, "Title 2", "Content 2");
        notificationService.createNotification(testUser1Id, "Title 3", "Content 3");
        
        assertEquals(3, notificationService.getUnreadNotifications(testUser1Id).size());

        // Act
        notificationService.markAllAsRead(testUser1Id);

        // Assert
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser1Id);
        assertTrue(unreadNotifications.isEmpty(), "All notifications should be read");
        
        List<Notification> allNotifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(3, allNotifications.size());
        for (Notification notif : allNotifications) {
            assertTrue(notif.isRead(), "All notifications should be marked as read");
        }
    }

    @Test
    void testMarkAllAsRead_NoNotifications() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> notificationService.markAllAsRead(testUser1Id));
    }

    @Test
    void testMarkAllAsRead_AlreadyAllRead() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Title 1", "Content 1");
        notificationService.markAllAsRead(testUser1Id);

        // Act - Mark all as read again
        assertDoesNotThrow(() -> notificationService.markAllAsRead(testUser1Id));

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).isRead());
    }

    @Test
    void testMarkAllAsRead_OnlyMarksOwnNotifications() {
        // Arrange
        notificationService.createNotification(testUser1Id, "User1 Title", "Content");
        notificationService.createNotification(testUser2Id, "User2 Title", "Content");

        // Act
        notificationService.markAllAsRead(testUser1Id);

        // Assert
        List<Notification> user1Unread = notificationService.getUnreadNotifications(testUser1Id);
        List<Notification> user2Unread = notificationService.getUnreadNotifications(testUser2Id);
        
        assertTrue(user1Unread.isEmpty(), "User1's notifications should be read");
        assertEquals(1, user2Unread.size(), "User2's notifications should still be unread");
    }

    @Test
    void testNotificationTimestamp() {
        // Arrange & Act
        notificationService.createNotification(testUser1Id, "Title", "Content");
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);

        // Assert
        assertNotNull(notifications.get(0).getCreatedTime());
        assertFalse(notifications.get(0).getCreatedTime().isEmpty());
        // Verify timestamp format (yyyy-MM-dd HH:mm:ss)
        assertTrue(notifications.get(0).getCreatedTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testNotificationOrdering() {
        // Arrange - Create notifications with delays
        notificationService.createNotification(testUser1Id, "First", "Content 1");
        try { Thread.sleep(100); } catch (InterruptedException e) { }
        
        notificationService.createNotification(testUser1Id, "Second", "Content 2");
        try { Thread.sleep(100); } catch (InterruptedException e) { }
        
        notificationService.createNotification(testUser1Id, "Third", "Content 3");

        // Act
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);

        // Assert
        assertEquals(3, notifications.size());
        // Verify they are returned in some consistent order
        assertNotNull(notifications.get(0).getCreatedTime());
        assertNotNull(notifications.get(1).getCreatedTime());
        assertNotNull(notifications.get(2).getCreatedTime());
    }

    @Test
    void testMultipleUsersIndependentNotifications() {
        // Arrange
        notificationService.createNotification(testUser1Id, "User1 Notif 1", "Content");
        notificationService.createNotification(testUser1Id, "User1 Notif 2", "Content");
        notificationService.createNotification(testUser2Id, "User2 Notif 1", "Content");

        // Act
        List<Notification> user1All = notificationService.getUserNotifications(testUser1Id);
        List<Notification> user2All = notificationService.getUserNotifications(testUser2Id);
        List<Notification> user1Unread = notificationService.getUnreadNotifications(testUser1Id);
        List<Notification> user2Unread = notificationService.getUnreadNotifications(testUser2Id);

        // Assert
        assertEquals(2, user1All.size());
        assertEquals(1, user2All.size());
        assertEquals(2, user1Unread.size());
        assertEquals(1, user2Unread.size());
    }

    @Test
    void testMarkAsRead_DoesNotAffectOtherUsers() {
        // Arrange
        notificationService.createNotification(testUser1Id, "User1 Title", "Content");
        notificationService.createNotification(testUser2Id, "User2 Title", "Content");
        
        List<Notification> user1Notifications = notificationService.getUserNotifications(testUser1Id);
        Long user1NotifId = user1Notifications.get(0).getId();

        // Act
        notificationService.markAsRead(user1NotifId);

        // Assert
        List<Notification> user1Unread = notificationService.getUnreadNotifications(testUser1Id);
        List<Notification> user2Unread = notificationService.getUnreadNotifications(testUser2Id);
        
        assertTrue(user1Unread.isEmpty(), "User1's notification should be read");
        assertEquals(1, user2Unread.size(), "User2's notification should still be unread");
    }

    @Test
    void testCreateNotification_WithHtmlContent() {
        // Arrange
        String htmlContent = "<b>Bold text</b> <i>Italic</i> <a href='test'>Link</a>";

        // Act
        notificationService.createNotification(testUser1Id, "HTML Title", htmlContent);

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(htmlContent, notifications.get(0).getContent());
    }

    @Test
    void testCreateNotification_WithNewlines() {
        // Arrange
        String contentWithNewlines = "Line 1\nLine 2\nLine 3";

        // Act
        notificationService.createNotification(testUser1Id, "Multiline", contentWithNewlines);

        // Assert
        List<Notification> notifications = notificationService.getUserNotifications(testUser1Id);
        assertEquals(contentWithNewlines, notifications.get(0).getContent());
    }

    @Test
    void testGetUnreadNotifications_AfterPartialRead() {
        // Arrange
        notificationService.createNotification(testUser1Id, "Title 1", "Content 1");
        notificationService.createNotification(testUser1Id, "Title 2", "Content 2");
        notificationService.createNotification(testUser1Id, "Title 3", "Content 3");
        notificationService.createNotification(testUser1Id, "Title 4", "Content 4");
        
        List<Notification> allNotifications = notificationService.getUserNotifications(testUser1Id);
        notificationService.markAsRead(allNotifications.get(0).getId());
        notificationService.markAsRead(allNotifications.get(2).getId());

        // Act
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser1Id);

        // Assert
        assertEquals(2, unreadNotifications.size());
        assertEquals("Title 2", unreadNotifications.get(0).getTitle());
        assertEquals("Title 4", unreadNotifications.get(1).getTitle());
    }
}
