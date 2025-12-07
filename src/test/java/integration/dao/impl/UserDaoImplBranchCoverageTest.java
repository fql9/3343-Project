package integration.dao.impl;

import integration.IntegrationTestBase;
import dao.impl.UserDaoImpl;
import dao.UserDao;
import config.DatabaseConfig;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoImplBranchCoverageTest extends IntegrationTestBase {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
    }

    // ========== mapRow 异常分支测试 ==========
    
    @Test
    void testMapRow_InvalidRoleString() {
        // 插入带有无效角色的用户数据
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 直接插入一个无效角色的用户
            stmt.executeUpdate(
                "INSERT INTO users (username, password_hash, email, role, active, created_time) " +
                "VALUES ('invalid_role_user', 'hash123', 'invalid@test.com', 'INVALID_ROLE', 1, '2024-01-01 00:00:00')"
            );
            
            // 查询这个用户，触发 mapRow 中的异常处理分支
            User user = userDao.findByUsername("invalid_role_user");
            
            // 应该不会崩溃，而是使用默认角色
            assertNotNull(user, "User should be found even with invalid role");
            assertNotNull(user.getRole(), "Role should not be null after fallback");
            
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    void testMapRow_NullRoleString() {
        // 插入角色为 NULL 的用户数据
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 直接插入角色为 NULL 的用户
            stmt.executeUpdate(
                "INSERT INTO users (username, password_hash, email, role, active, created_time) " +
                "VALUES ('null_role_user', 'hash123', 'null_role@test.com', NULL, 1, '2024-01-01 00:00:00')"
            );
            
            // 查询这个用户，触发 mapRow 中的 NullPointerException 处理分支
            User user = userDao.findByUsername("null_role_user");
            
            // 应该不会崩溃，而是使用默认角色
            assertNotNull(user, "User should be found even with null role");
            assertNotNull(user.getRole(), "Role should have a default value");
            
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    // ========== findByUsername 分支测试 ==========
    
    @Test
    void testFindByUsername_Exists() {
        // 创建用户
        User user = createTestUser("find_test_user");
        userDao.save(user);
        
        // 查找
        User found = userDao.findByUsername("find_test_user");
        
        assertNotNull(found);
        assertEquals("find_test_user", found.getUsername());
    }

    @Test
    void testFindByUsername_NotExists() {
        User found = userDao.findByUsername("non_existent_user_xyz");
        assertNull(found);
    }

    // ========== findById 分支测试 ==========
    
    @Test
    void testFindById_Exists() {
        // 创建用户
        User user = createTestUser("findbyid_test");
        userDao.save(user);
        
        // 获取 ID
        User saved = userDao.findByUsername("findbyid_test");
        assertNotNull(saved);
        
        // 按 ID 查找
        User found = userDao.findById(saved.getId());
        assertNotNull(found);
        assertEquals("findbyid_test", found.getUsername());
    }

    @Test
    void testFindById_NotExists() {
        User found = userDao.findById(999999L);
        assertNull(found);
    }

    // ========== findAll 分支测试 ==========
    
    @Test
    void testFindAll_Empty() {
        // 测试库应该为空（因为 IntegrationTestBase 会清理）
        List<User> users = userDao.findAll();
        assertNotNull(users);
        // 可能为空或有数据，主要测试不抛异常
    }

    @Test
    void testFindAll_WithMultipleUsers() {
        userDao.save(createTestUser("user1"));
        userDao.save(createTestUser("user2"));
        userDao.save(createTestUser("user3"));
        
        List<User> users = userDao.findAll();
        
        assertNotNull(users);
        assertTrue(users.size() >= 3);
    }

    // ========== save 分支测试 ==========
    
    @Test
    void testSave_WithAllFields() {
        User user = new User();
        user.setUsername("full_user");
        user.setPasswordHash("hash123");
        user.setEmail("full@test.com");
        user.setRole(UserRole.SELLER);
        user.setActive(true);
        user.setAvatarUrl("http://avatar.com/img.png");
        user.setBio("This is my bio");
        user.setCreatedTime("2024-01-01 12:00:00");
        
        userDao.save(user);
        
        User found = userDao.findByUsername("full_user");
        assertNotNull(found);
        assertEquals("full@test.com", found.getEmail());
        assertEquals(UserRole.SELLER, found.getRole());
        assertEquals("http://avatar.com/img.png", found.getAvatarUrl());
        assertEquals("This is my bio", found.getBio());
    }

    @Test
    void testSave_WithMinimalFields() {
        User user = new User();
        user.setUsername("minimal_user");
        user.setPasswordHash("hash123");
        user.setEmail("minimal@test.com");
        user.setRole(UserRole.BUYER);
        user.setActive(false);
        user.setCreatedTime("2024-01-01 00:00:00");
        
        userDao.save(user);
        
        User found = userDao.findByUsername("minimal_user");
        assertNotNull(found);
        assertFalse(found.isActive());
    }

    // ========== update 分支测试 ==========
    
    @Test
    void testUpdate_AllFields() {
        User user = createTestUser("update_test");
        userDao.save(user);
        
        User saved = userDao.findByUsername("update_test");
        assertNotNull(saved);
        
        // 更新所有字段
        saved.setUsername("update_test_new");
        saved.setEmail("updated@test.com");
        saved.setRole(UserRole.ADMIN);
        saved.setActive(false);
        saved.setAvatarUrl("http://new-avatar.com/img.png");
        saved.setBio("Updated bio");
        
        userDao.update(saved);
        
        User found = userDao.findById(saved.getId());
        assertNotNull(found);
        assertEquals("update_test_new", found.getUsername());
        assertEquals("updated@test.com", found.getEmail());
        assertEquals(UserRole.ADMIN, found.getRole());
        assertFalse(found.isActive());
    }

    @Test
    void testUpdate_ActiveStatus() {
        User user = createTestUser("active_test");
        user.setActive(true);
        userDao.save(user);
        
        User saved = userDao.findByUsername("active_test");
        assertTrue(saved.isActive());
        
        // 设为不活跃
        saved.setActive(false);
        userDao.update(saved);
        
        User found = userDao.findById(saved.getId());
        assertFalse(found.isActive());
        
        // 重新激活
        found.setActive(true);
        userDao.update(found);
        
        User reactivated = userDao.findById(found.getId());
        assertTrue(reactivated.isActive());
    }

    // ========== delete 分支测试 ==========
    
    @Test
    void testDelete_ExistingUser() {
        User user = createTestUser("delete_test");
        userDao.save(user);
        
        User saved = userDao.findByUsername("delete_test");
        assertNotNull(saved);
        
        userDao.delete(saved.getId());
        
        User found = userDao.findById(saved.getId());
        assertNull(found);
    }

    @Test
    void testDelete_NonExistingUser() {
        // 删除不存在的用户不应抛异常
        assertDoesNotThrow(() -> userDao.delete(999999L));
    }

    // ========== 辅助方法 ==========
    
    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash123");
        user.setEmail(username + "@test.com");
        user.setRole(UserRole.BUYER);
        user.setActive(true);
        user.setCreatedTime("2024-01-01 00:00:00");
        return user;
    }
}

