package service;

import dao.NotificationDao;
import dao.impl.NotificationDaoImpl;
import model.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationService {

    private final NotificationDao notificationDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationService() {
        this.notificationDao = new NotificationDaoImpl();
    }

    public void createNotification(Long userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRead(false);
        notification.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        notificationDao.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationDao.findUnreadByUserId(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationDao.markAsRead(notificationId);
    }

    public void markAllAsRead(Long userId) {
        notificationDao.markAllAsRead(userId);
    }
}
