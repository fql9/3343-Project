package dao;

import model.Message;
import java.util.List;

/**
 * Message Data Access Object interface.
 */
public interface MessageDao {

    Message findById(Long id);

    List<Message> findConversation(Long user1, Long user2);

    List<Message> findInbox(Long userId);

    void save(Message message);

    void markRead(Long id);

    void delete(Long id);
}
