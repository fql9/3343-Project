package dao.impl;

import config.DatabaseConfig;
import dao.MessageDao;
import model.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class MessageDaoImplTest {
    private MessageDao messageDao;
    private Message testMessage;
    private static final Long TEST_FROM_USER_ID = 1L;
    private static final Long TEST_TO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        messageDao = new MessageDaoImpl();
        
        // 创建测试消息
        testMessage = new Message();
        testMessage.setFromUserId(TEST_FROM_USER_ID);
        testMessage.setToUserId(TEST_TO_USER_ID);
        testMessage.setContent("Test message content");
        testMessage.setCreatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        testMessage.setRead(false);
        
        // 确保测试数据不存在
        clearTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        clearTestData();
    }

    private void clearTestData() {
        String sql = "DELETE FROM messages WHERE content = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Test message content");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testFindById() {
        // 保存测试消息
        messageDao.save(testMessage);
        
        // 查找第一个消息（假设数据库中只有这个测试消息）
        Message savedMessage = messageDao.findInbox(TEST_TO_USER_ID).get(0);
        assertNotNull(savedMessage);
        
        // 通过ID查找消息
        Message foundMessage = messageDao.findById(savedMessage.getId());
        
        // 验证结果
        assertNotNull(foundMessage);
        assertEquals(savedMessage.getId(), foundMessage.getId());
        assertEquals("Test message content", foundMessage.getContent());
    }

    @Test
    void testFindConversation() {
        // 保存测试消息
        messageDao.save(testMessage);
        
        // 查找对话
        assertNotNull(messageDao.findConversation(TEST_FROM_USER_ID, TEST_TO_USER_ID));
    }

    @Test
    void testFindInbox() {
        // 保存测试消息
        messageDao.save(testMessage);
        
        // 查找收件箱
        assertNotNull(messageDao.findInbox(TEST_TO_USER_ID));
    }

    @Test
    void testSave() {
        // 保存测试消息
        messageDao.save(testMessage);
        
        // 验证保存成功
        Message savedMessage = messageDao.findInbox(TEST_TO_USER_ID).get(0);
        assertNotNull(savedMessage);
        assertEquals(TEST_FROM_USER_ID, savedMessage.getFromUserId());
        assertEquals(TEST_TO_USER_ID, savedMessage.getToUserId());
        assertEquals("Test message content", savedMessage.getContent());
        assertFalse(savedMessage.isRead());
    }

    @Test
    void testMarkRead() {
        // 保存测试消息
        messageDao.save(testMessage);
        
        // 查找消息并标记为已读
        Message savedMessage = messageDao.findInbox(TEST_TO_USER_ID).get(0);
        assertNotNull(savedMessage);
        
        messageDao.markRead(savedMessage.getId());
        
        // 验证消息已标记为已读
        Message readMessage = messageDao.findById(savedMessage.getId());
        assertNotNull(readMessage);
        assertTrue(readMessage.isRead());
    }

    @Test
    void testDelete() {
        // 保存测试消息
        messageDao.save(testMessage);
        
        // 查找消息并删除
        Message savedMessage = messageDao.findInbox(TEST_TO_USER_ID).get(0);
        assertNotNull(savedMessage);
        
        messageDao.delete(savedMessage.getId());
        
        // 验证删除成功
        Message deletedMessage = messageDao.findById(savedMessage.getId());
        assertNull(deletedMessage);
    }
}
