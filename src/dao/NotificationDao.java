package dao;

import model.Notification;
import java.util.List;

public interface NotificationDao {
    void save(Notification notification);
    List<Notification> findByUserId(Long userId);
    List<Notification> findUnreadByUserId(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}
