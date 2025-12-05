package dao.impl;

import dao.impl.UserDaoImpl;

import config.DatabaseConfig;
import dao.UserDao;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class UserDaoImplTest {
    private UserDao userDao;
    private User testUser;

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
        
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hash123");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.BUYER);
        testUser.setActive(true);
        
        // 确保测试数据不存在
        clearTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        clearTestData();
    }

    private void clearTestData() {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "testuser");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testFindByUsername() {
        // 保存测试用户
        userDao.save(testUser);
        
        // 查找用户
        User foundUser = userDao.findByUsername("testuser");
        
        // 验证结果
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testFindById() {
        // 保存测试用户
        userDao.save(testUser);
        
        // 先通过用户名查找获取ID
        User savedUser = userDao.findByUsername("testuser");
        assertNotNull(savedUser);
        
        // 通过ID查找用户
        User foundUser = userDao.findById(savedUser.getId());
        
        // 验证结果
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void testFindAll() {
        // 保存测试用户
        userDao.save(testUser);
        
        // 获取所有用户
        assertNotNull(userDao.findAll());
    }

    @Test
    void testSave() {
        // 保存测试用户
        userDao.save(testUser);
        
        // 验证保存成功
        User foundUser = userDao.findByUsername("testuser");
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
        assertEquals(UserRole.BUYER, foundUser.getRole());
        assertTrue(foundUser.isActive());
    }

    @Test
    void testUpdate() {
        // 保存测试用户
        userDao.save(testUser);
        
        // 查找用户并更新
        User foundUser = userDao.findByUsername("testuser");
        assertNotNull(foundUser);
        
        foundUser.setEmail("updated@example.com");
        foundUser.setRole(UserRole.SELLER);
        foundUser.setActive(false);
        
        userDao.update(foundUser);
        
        // 验证更新成功
        User updatedUser = userDao.findByUsername("testuser");
        assertNotNull(updatedUser);
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(UserRole.SELLER, updatedUser.getRole());
        assertFalse(updatedUser.isActive());
    }

    @Test
    void testDelete() {
        // 保存测试用户
        userDao.save(testUser);
        
        // 查找用户并删除
        User foundUser = userDao.findByUsername("testuser");
        assertNotNull(foundUser);
        
        userDao.delete(foundUser.getId());
        
        // 验证删除成功
        User deletedUser = userDao.findByUsername("testuser");
        assertNull(deletedUser);
    }
}
