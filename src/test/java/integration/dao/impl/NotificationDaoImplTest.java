package integration.dao.impl;

import dao.NotificationDao;
import dao.impl.NotificationDaoImpl;
import integration.IntegrationTestBase;
import model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NotificationDaoImpl 集成测试
 */
class NotificationDaoImplTest extends IntegrationTestBase {

    private NotificationDao notificationDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        notificationDao = new NotificationDaoImpl();
    }

    @Test
    void testSaveNotification() {
        // Arrange
        Notification notification = createTestNotification(
            1L, "Test Title", "Test Content", false
        );

        // Act
        notificationDao.save(notification);

        // Assert
        List<Notification> notifications = notificationDao.findByUserId(1L);
        assertEquals(1, notifications.size());
        assertEquals("Test Title", notifications.get(0).getTitle());
        assertEquals("Test Content", notifications.get(0).getContent());
        assertFalse(notifications.get(0).isRead());
    }

    @Test
    void testFindByUserId() {
        // Arrange
        Long userId = 100L;
        notificationDao.save(createTestNotification(userId, "Title 1", "Content 1", false));
        notificationDao.save(createTestNotification(userId, "Title 2", "Content 2", true));
        notificationDao.save(createTestNotification(userId, "Title 3", "Content 3", false));
        
        // 为其他用户创建通知
        notificationDao.save(createTestNotification(200L, "Other", "Other", false));

        // Act
        List<Notification> notifications = notificationDao.findByUserId(userId);

        // Assert
        assertEquals(3, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> n.getUserId().equals(userId)));
    }

    @Test
    void testFindUnreadByUserId() {
        // Arrange
        Long userId = 300L;
        notificationDao.save(createTestNotification(userId, "Unread 1", "Content 1", false));
        notificationDao.save(createTestNotification(userId, "Read", "Content 2", true));
        notificationDao.save(createTestNotification(userId, "Unread 2", "Content 3", false));

        // Act
        List<Notification> unreadNotifications = notificationDao.findUnreadByUserId(userId);

        // Assert
        assertEquals(2, unreadNotifications.size());
        assertTrue(unreadNotifications.stream().allMatch(n -> !n.isRead()));
        assertTrue(unreadNotifications.stream()
            .anyMatch(n -> n.getTitle().equals("Unread 1")));
        assertTrue(unreadNotifications.stream()
            .anyMatch(n -> n.getTitle().equals("Unread 2")));
    }

    @Test
    void testMarkAsRead() {
        // Arrange
        Long userId = 400L;
        notificationDao.save(createTestNotification(userId, "Test", "Content", false));
        
        List<Notification> before = notificationDao.findByUserId(userId);
        Long notificationId = before.get(0).getId();
        assertFalse(before.get(0).isRead());

        // Act
        notificationDao.markAsRead(notificationId);

        // Assert
        List<Notification> after = notificationDao.findByUserId(userId);
        assertTrue(after.get(0).isRead());
    }

    @Test
    void testMarkAllAsRead() {
        // Arrange
        Long userId = 500L;
        notificationDao.save(createTestNotification(userId, "Unread 1", "Content", false));
        notificationDao.save(createTestNotification(userId, "Unread 2", "Content", false));
        notificationDao.save(createTestNotification(userId, "Unread 3", "Content", false));
        
        List<Notification> before = notificationDao.findUnreadByUserId(userId);
        assertEquals(3, before.size());

        // Act
        notificationDao.markAllAsRead(userId);

        // Assert
        List<Notification> afterUnread = notificationDao.findUnreadByUserId(userId);
        assertEquals(0, afterUnread.size());
        
        List<Notification> allNotifications = notificationDao.findByUserId(userId);
        assertTrue(allNotifications.stream().allMatch(Notification::isRead));
    }

    @Test
    void testFindByUserId_EmptyResult() {
        // Act
        List<Notification> notifications = notificationDao.findByUserId(99999L);

        // Assert
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testFindUnreadByUserId_EmptyResult() {
        // Arrange - 创建已读通知
        Long userId = 600L;
        notificationDao.save(createTestNotification(userId, "Read", "Content", true));

        // Act
        List<Notification> unread = notificationDao.findUnreadByUserId(userId);

        // Assert
        assertTrue(unread.isEmpty());
    }

    @Test
    void testMultipleUsersNotifications() {
        // Arrange
        Long user1 = 700L;
        Long user2 = 800L;
        
        notificationDao.save(createTestNotification(user1, "User1 N1", "Content", false));
        notificationDao.save(createTestNotification(user1, "User1 N2", "Content", false));
        notificationDao.save(createTestNotification(user2, "User2 N1", "Content", false));
        notificationDao.save(createTestNotification(user2, "User2 N2", "Content", false));
        notificationDao.save(createTestNotification(user2, "User2 N3", "Content", false));

        // Act
        List<Notification> user1Notifications = notificationDao.findByUserId(user1);
        List<Notification> user2Notifications = notificationDao.findByUserId(user2);

        // Assert
        assertEquals(2, user1Notifications.size());
        assertEquals(3, user2Notifications.size());
    }

    @Test
    void testNotificationOrdering() {
        // Arrange
        Long userId = 900L;
        
        // 按顺序创建通知
        for (int i = 1; i <= 5; i++) {
            notificationDao.save(createTestNotification(
                userId, 
                "Notification " + i, 
                "Content " + i, 
                false
            ));
            // 稍微延迟以确保时间戳不同
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Act
        List<Notification> notifications = notificationDao.findByUserId(userId);

        // Assert
        assertEquals(5, notifications.size());
        
        // 验证按创建时间倒序排列（最新的在前）
        for (int i = 0; i < notifications.size() - 1; i++) {
            assertTrue(notifications.get(i).getCreatedTime()
                .compareTo(notifications.get(i + 1).getCreatedTime()) >= 0);
        }
    }

    @Test
    void testMarkAsRead_NonExistentNotification() {
        // Act & Assert - 应该不抛出异常
        assertDoesNotThrow(() -> notificationDao.markAsRead(99999L));
    }

    @Test
    void testMarkAllAsRead_NoNotifications() {
        // Act & Assert - 应该不抛出异常
        assertDoesNotThrow(() -> notificationDao.markAllAsRead(99999L));
    }

    @Test
    void testNotificationWithLongContent() {
        // Arrange
        String longContent = "A".repeat(1000); // 1000字符的内容
        Notification notification = createTestNotification(
            1L, "Test", longContent, false
        );

        // Act
        notificationDao.save(notification);

        // Assert
        List<Notification> notifications = notificationDao.findByUserId(1L);
        assertEquals(1, notifications.size());
        assertEquals(longContent, notifications.get(0).getContent());
    }

    @Test
    void testMixedReadUnreadNotifications() {
        // Arrange
        Long userId = 1000L;
        notificationDao.save(createTestNotification(userId, "N1", "Content", false));
        notificationDao.save(createTestNotification(userId, "N2", "Content", true));
        notificationDao.save(createTestNotification(userId, "N3", "Content", false));
        notificationDao.save(createTestNotification(userId, "N4", "Content", true));
        notificationDao.save(createTestNotification(userId, "N5", "Content", false));

        // Act
        List<Notification> all = notificationDao.findByUserId(userId);
        List<Notification> unread = notificationDao.findUnreadByUserId(userId);

        // Assert
        assertEquals(5, all.size());
        assertEquals(3, unread.size());
        
        long readCount = all.stream().filter(Notification::isRead).count();
        assertEquals(2, readCount);
    }

    // Helper method
    private Notification createTestNotification(Long userId, String title, 
                                               String content, boolean isRead) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRead(isRead);
        notification.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        return notification;
    }
}

