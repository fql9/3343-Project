package integration.dao.impl;

import integration.IntegrationTestBase;
import dao.impl.MessageDaoImpl;
import dao.impl.UserDaoImpl;
import dao.MessageDao;
import dao.UserDao;
import model.Message;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 MessageDaoImpl 分支覆盖率的增强测试
 * 目标: 将分支覆盖率从 78% 提升至 90%+
 */
class MessageDaoImplBranchCoverageTest extends IntegrationTestBase {

    private MessageDao messageDao;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        messageDao = new MessageDaoImpl();
        userDao = new UserDaoImpl();
    }

    // ========== findById 分支测试 ==========
    
    @Test
    void testFindById_Exists() {
        User user1 = createAndSaveUser("msg_findbyid_user1");
        User user2 = createAndSaveUser("msg_findbyid_user2");
        
        Message msg = createMessage(user1.getId(), user2.getId(), "Test message");
        messageDao.save(msg);
        
        List<Message> msgs = messageDao.findInbox(user2.getId());
        assertFalse(msgs.isEmpty());
        
        Message found = messageDao.findById(msgs.get(0).getId());
        assertNotNull(found);
        assertEquals("Test message", found.getContent());
    }

    @Test
    void testFindById_NotExists() {
        Message found = messageDao.findById(999999L);
        assertNull(found);
    }

    // ========== findConversation 分支测试 ==========
    
    @Test
    void testFindConversation_Empty() {
        List<Message> msgs = messageDao.findConversation(999999L, 888888L);
        assertNotNull(msgs);
        assertTrue(msgs.isEmpty());
    }

    @Test
    void testFindConversation_WithMessages() {
        User user1 = createAndSaveUser("msg_conv_user1");
        User user2 = createAndSaveUser("msg_conv_user2");
        
        // 双向对话
        messageDao.save(createMessage(user1.getId(), user2.getId(), "Hello from user1"));
        messageDao.save(createMessage(user2.getId(), user1.getId(), "Hello from user2"));
        messageDao.save(createMessage(user1.getId(), user2.getId(), "How are you?"));
        
        List<Message> msgs = messageDao.findConversation(user1.getId(), user2.getId());
        
        assertNotNull(msgs);
        assertEquals(3, msgs.size());
    }

    @Test
    void testFindConversation_ReverseOrder() {
        User user1 = createAndSaveUser("msg_rev_user1");
        User user2 = createAndSaveUser("msg_rev_user2");
        
        messageDao.save(createMessage(user1.getId(), user2.getId(), "Message 1"));
        messageDao.save(createMessage(user2.getId(), user1.getId(), "Message 2"));
        
        // 反向查询应该得到相同结果
        List<Message> msgs1 = messageDao.findConversation(user1.getId(), user2.getId());
        List<Message> msgs2 = messageDao.findConversation(user2.getId(), user1.getId());
        
        assertEquals(msgs1.size(), msgs2.size());
    }

    // ========== findInbox 分支测试 ==========
    
    @Test
    void testFindInbox_Empty() {
        List<Message> msgs = messageDao.findInbox(999999L);
        assertNotNull(msgs);
        assertTrue(msgs.isEmpty());
    }

    @Test
    void testFindInbox_WithMessages() {
        User receiver = createAndSaveUser("msg_inbox_receiver");
        User sender1 = createAndSaveUser("msg_inbox_sender1");
        User sender2 = createAndSaveUser("msg_inbox_sender2");
        
        messageDao.save(createMessage(sender1.getId(), receiver.getId(), "Message from sender1"));
        messageDao.save(createMessage(sender2.getId(), receiver.getId(), "Message from sender2"));
        
        List<Message> msgs = messageDao.findInbox(receiver.getId());
        
        assertNotNull(msgs);
        assertEquals(2, msgs.size());
    }

    // ========== save 分支测试 ==========
    
    @Test
    void testSave_UnreadMessage() {
        User user1 = createAndSaveUser("msg_save_user1");
        User user2 = createAndSaveUser("msg_save_user2");
        
        Message msg = createMessage(user1.getId(), user2.getId(), "Unread message");
        msg.setRead(false);
        messageDao.save(msg);
        
        List<Message> msgs = messageDao.findInbox(user2.getId());
        assertFalse(msgs.isEmpty());
        assertFalse(msgs.get(0).isRead());
    }

    @Test
    void testSave_ReadMessage() {
        User user1 = createAndSaveUser("msg_read_user1");
        User user2 = createAndSaveUser("msg_read_user2");
        
        Message msg = createMessage(user1.getId(), user2.getId(), "Pre-read message");
        msg.setRead(true);
        messageDao.save(msg);
        
        List<Message> msgs = messageDao.findInbox(user2.getId());
        assertFalse(msgs.isEmpty());
        assertTrue(msgs.get(0).isRead());
    }

    // ========== markRead 分支测试 ==========
    
    @Test
    void testMarkRead_Success() {
        User user1 = createAndSaveUser("msg_mark_user1");
        User user2 = createAndSaveUser("msg_mark_user2");
        
        Message msg = createMessage(user1.getId(), user2.getId(), "To be marked as read");
        msg.setRead(false);
        messageDao.save(msg);
        
        List<Message> msgs = messageDao.findInbox(user2.getId());
        assertFalse(msgs.isEmpty());
        assertFalse(msgs.get(0).isRead());
        
        // 标记为已读
        messageDao.markRead(msgs.get(0).getId());
        
        Message updated = messageDao.findById(msgs.get(0).getId());
        assertTrue(updated.isRead());
    }

    @Test
    void testMarkRead_NonExisting() {
        // 标记不存在的消息不应抛异常
        assertDoesNotThrow(() -> messageDao.markRead(999999L));
    }

    // ========== delete 分支测试 ==========
    
    @Test
    void testDelete_ExistingMessage() {
        User user1 = createAndSaveUser("msg_del_user1");
        User user2 = createAndSaveUser("msg_del_user2");
        
        Message msg = createMessage(user1.getId(), user2.getId(), "To be deleted");
        messageDao.save(msg);
        
        List<Message> msgs = messageDao.findInbox(user2.getId());
        assertFalse(msgs.isEmpty());
        Long msgId = msgs.get(0).getId();
        
        messageDao.delete(msgId);
        
        Message found = messageDao.findById(msgId);
        assertNull(found);
    }

    @Test
    void testDelete_NonExisting() {
        // 删除不存在的消息不应抛异常
        assertDoesNotThrow(() -> messageDao.delete(999999L));
    }

    // ========== 辅助方法 ==========
    
    private User createAndSaveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash123");
        user.setEmail(username + "@test.com");
        user.setRole(UserRole.BUYER);
        user.setActive(true);
        user.setCreatedTime("2024-01-01 00:00:00");
        userDao.save(user);
        return userDao.findByUsername(username);
    }
    
    private Message createMessage(Long fromUserId, Long toUserId, String content) {
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content);
        msg.setCreatedTime("2024-01-01 00:00:00");
        msg.setRead(false);
        return msg;
    }
}

