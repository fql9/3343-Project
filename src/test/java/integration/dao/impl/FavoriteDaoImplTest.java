package integration.dao.impl;

import integration.IntegrationTestBase;
import dao.impl.FavoriteDaoImpl;

import config.DatabaseConfig;
import dao.FavoriteDao;
import model.Favorite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class FavoriteDaoImplTest extends IntegrationTestBase {
    private FavoriteDao favoriteDao;
    private Favorite testFavorite;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ITEM_ID = 1L;

    @BeforeEach
    void setUp() {
        favoriteDao = new FavoriteDaoImpl();
        
        // 创建测试用户和商品（满足外键约束）
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR IGNORE INTO users (id, username, password_hash, email, role, active, created_time) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, TEST_USER_ID);
            ps.setString(2, "test_user_fav");
            ps.setString(3, "hash");
            ps.setString(4, "test_fav@test.com");
            ps.setString(5, "BUYER");
            ps.setInt(6, 1);
            ps.setString(7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR IGNORE INTO items (id, seller_id, title, description, price, category, active, created_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, TEST_ITEM_ID);
            ps.setLong(2, TEST_USER_ID);
            ps.setString(3, "Test Item");
            ps.setString(4, "Test Description");
            ps.setDouble(5, 100.0);
            ps.setString(6, "Test");
            ps.setInt(7, 1);
            ps.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 创建测试收藏
        testFavorite = new Favorite();
        testFavorite.setUserId(TEST_USER_ID);
        testFavorite.setItemId(TEST_ITEM_ID);
        testFavorite.setCreatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 确保测试数据不存在
        clearTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        clearTestData();
    }

    private void clearTestData() {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, TEST_USER_ID);
            ps.setLong(2, TEST_ITEM_ID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testFindById() {
        // 保存测试收藏
        favoriteDao.save(testFavorite);
        
        // 查找第一个收藏（假设数据库中只有这个测试收藏）
        Favorite savedFavorite = favoriteDao.findByUserId(TEST_USER_ID).get(0);
        assertNotNull(savedFavorite);
        
        // 通过ID查找收藏
        Favorite foundFavorite = favoriteDao.findById(savedFavorite.getId());
        
        // 验证结果
        assertNotNull(foundFavorite);
        assertEquals(savedFavorite.getId(), foundFavorite.getId());
    }

    @Test
    void testFindByUserId() {
        // 保存测试收藏
        favoriteDao.save(testFavorite);
        
        // 通过用户ID查找收藏
        assertNotNull(favoriteDao.findByUserId(TEST_USER_ID));
    }

    @Test
    void testFindByItemId() {
        // 保存测试收藏
        favoriteDao.save(testFavorite);
        
        // 通过商品ID查找收藏
        assertNotNull(favoriteDao.findByItemId(TEST_ITEM_ID));
    }

    @Test
    void testExists() {
        // 保存测试收藏
        favoriteDao.save(testFavorite);
        
        // 检查收藏是否存在
        assertTrue(favoriteDao.exists(TEST_USER_ID, TEST_ITEM_ID));
        assertFalse(favoriteDao.exists(TEST_USER_ID, 999L));
    }

    @Test
    void testSave() {
        // 保存测试收藏
        favoriteDao.save(testFavorite);
        
        // 验证保存成功
        assertTrue(favoriteDao.exists(TEST_USER_ID, TEST_ITEM_ID));
    }

    @Test
    void testDelete() {
        // 保存测试收藏
        favoriteDao.save(testFavorite);
        
        // 查找收藏并删除
        Favorite savedFavorite = favoriteDao.findByUserId(TEST_USER_ID).get(0);
        assertNotNull(savedFavorite);
        
        favoriteDao.delete(savedFavorite.getId());
        
        // 验证删除成功
        assertFalse(favoriteDao.exists(TEST_USER_ID, TEST_ITEM_ID));
    }

    @Test
    void testDeleteByUserAndItem() {
        // 保存测试收藏
        favoriteDao.save(testFavorite);
        
        // 通过用户ID和商品ID删除收藏
        favoriteDao.deleteByUserAndItem(TEST_USER_ID, TEST_ITEM_ID);
        
        // 验证删除成功
        assertFalse(favoriteDao.exists(TEST_USER_ID, TEST_ITEM_ID));
    }
}
