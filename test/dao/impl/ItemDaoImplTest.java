package dao.impl;

import config.DatabaseConfig;
import dao.ItemDao;
import model.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class ItemDaoImplTest {
    private ItemDao itemDao;
    private Item testItem;
    private static final Long TEST_SELLER_ID = 1L;

    @BeforeEach
    void setUp() {
        itemDao = new ItemDaoImpl();
        
        // 创建测试商品
        testItem = new Item();
        testItem.setSellerId(TEST_SELLER_ID);
        testItem.setTitle("Test Item");
        testItem.setDescription("This is a test item");
        testItem.setPrice(10.99);
        testItem.setCategory("Electronics");
        testItem.setActive(true);
        testItem.setCreatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 确保测试数据不存在
        clearTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        clearTestData();
    }

    private void clearTestData() {
        String sql = "DELETE FROM items WHERE title = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Test Item");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testFindById() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 查找第一个商品（假设数据库中只有这个测试商品）
        Item savedItem = itemDao.findAll().get(0);
        assertNotNull(savedItem);
        
        // 通过ID查找商品
        Item foundItem = itemDao.findById(savedItem.getId());
        
        // 验证结果
        assertNotNull(foundItem);
        assertEquals(savedItem.getId(), foundItem.getId());
        assertEquals("Test Item", foundItem.getTitle());
    }

    @Test
    void testFindAll() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 获取所有商品
        assertNotNull(itemDao.findAll());
    }

    @Test
    void testFindBySellerId() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 通过卖家ID查找商品
        assertNotNull(itemDao.findBySellerId(TEST_SELLER_ID));
    }

    @Test
    void testSearch() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 搜索商品
        assertNotNull(itemDao.search("Test"));
        assertNotNull(itemDao.search("item"));
        assertNotNull(itemDao.search("test item"));
    }

    @Test
    void testSave() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 验证保存成功
        Item savedItem = itemDao.findAll().get(0);
        assertNotNull(savedItem);
        assertEquals("Test Item", savedItem.getTitle());
        assertEquals("This is a test item", savedItem.getDescription());
        assertEquals(10.99, savedItem.getPrice());
        assertEquals("Electronics", savedItem.getCategory());
        assertTrue(savedItem.isActive());
    }

    @Test
    void testUpdate() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 查找商品并更新
        Item savedItem = itemDao.findAll().get(0);
        assertNotNull(savedItem);
        
        savedItem.setTitle("Updated Test Item");
        savedItem.setPrice(15.99);
        savedItem.setActive(false);
        
        itemDao.update(savedItem);
        
        // 验证更新成功
        Item updatedItem = itemDao.findById(savedItem.getId());
        assertNotNull(updatedItem);
        assertEquals("Updated Test Item", updatedItem.getTitle());
        assertEquals(15.99, updatedItem.getPrice());
        assertFalse(updatedItem.isActive());
    }

    @Test
    void testDelete() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 查找商品并删除
        Item savedItem = itemDao.findAll().get(0);
        assertNotNull(savedItem);
        
        itemDao.delete(savedItem.getId());
        
        // 验证删除成功
        Item deletedItem = itemDao.findById(savedItem.getId());
        assertNull(deletedItem);
    }
}
