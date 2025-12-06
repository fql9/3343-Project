package integration.dao.impl;

import integration.IntegrationTestBase;
import dao.impl.ItemDaoImpl;

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

class ItemDaoImplTest extends IntegrationTestBase {
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

    @Test
    void testSearchItems_NoFilters() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 搜索不带任何过滤条件
        var results = itemDao.searchItems(null, null, null, null, null);
        
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testSearchItems_WithKeyword() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 使用关键词搜索
        var results = itemDao.searchItems("Test", null, null, null, null);
        
        assertNotNull(results);
        assertTrue(results.stream().anyMatch(item -> item.getTitle().contains("Test")));
    }

    @Test
    void testSearchItems_WithPriceRange() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 使用价格范围搜索
        var results = itemDao.searchItems(null, 5.0, 15.0, null, null);
        
        assertNotNull(results);
        assertTrue(results.stream().allMatch(item -> 
            item.getPrice() >= 5.0 && item.getPrice() <= 15.0));
    }

    @Test
    void testSearchItems_WithMinPriceOnly() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 只使用最低价格搜索
        var results = itemDao.searchItems(null, 5.0, null, null, null);
        
        assertNotNull(results);
        assertTrue(results.stream().allMatch(item -> item.getPrice() >= 5.0));
    }

    @Test
    void testSearchItems_WithMaxPriceOnly() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 只使用最高价格搜索
        var results = itemDao.searchItems(null, null, 50.0, null, null);
        
        assertNotNull(results);
        assertTrue(results.stream().allMatch(item -> item.getPrice() <= 50.0));
    }

    @Test
    void testSearchItems_WithCategory() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 使用类别搜索
        var results = itemDao.searchItems(null, null, null, "Electronics", null);
        
        assertNotNull(results);
        assertTrue(results.stream().allMatch(item -> "Electronics".equals(item.getCategory())));
    }

    @Test
    void testSearchItems_WithAllCategoriesFilter() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 使用 "All Categories" 应该不过滤
        var results = itemDao.searchItems(null, null, null, "All Categories", null);
        
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testSearchItems_SortByPriceLowToHigh() {
        // 保存多个不同价格的商品
        itemDao.save(testItem);
        
        Item item2 = new Item();
        item2.setSellerId(TEST_SELLER_ID);
        item2.setTitle("Test Item 2");
        item2.setDescription("Another test item");
        item2.setPrice(5.99);
        item2.setCategory("Electronics");
        item2.setActive(true);
        item2.setCreatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        itemDao.save(item2);
        
        // 按价格从低到高排序
        var results = itemDao.searchItems("Test", null, null, null, "Price: Low to High");
        
        assertNotNull(results);
        assertTrue(results.size() >= 2);
        // 验证排序
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getPrice() <= results.get(i + 1).getPrice());
        }
        
        // 清理额外数据
        String sql = "DELETE FROM items WHERE title = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Test Item 2");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSearchItems_SortByPriceHighToLow() {
        // 保存多个不同价格的商品
        itemDao.save(testItem);
        
        Item item2 = new Item();
        item2.setSellerId(TEST_SELLER_ID);
        item2.setTitle("Test Item 3");
        item2.setDescription("Another test item");
        item2.setPrice(25.99);
        item2.setCategory("Electronics");
        item2.setActive(true);
        item2.setCreatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        itemDao.save(item2);
        
        // 按价格从高到低排序
        var results = itemDao.searchItems("Test", null, null, null, "Price: High to Low");
        
        assertNotNull(results);
        assertTrue(results.size() >= 2);
        // 验证排序
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getPrice() >= results.get(i + 1).getPrice());
        }
        
        // 清理额外数据
        String sql = "DELETE FROM items WHERE title = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Test Item 3");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSearchItems_SortByNewestFirst() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 按最新优先排序（默认）
        var results = itemDao.searchItems(null, null, null, null, "Newest First");
        
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testSearchItems_CombinedFilters() {
        // 保存测试商品
        itemDao.save(testItem);
        
        // 组合多个过滤条件
        var results = itemDao.searchItems("Test", 5.0, 20.0, "Electronics", "Price: Low to High");
        
        assertNotNull(results);
        if (!results.isEmpty()) {
            assertTrue(results.stream().allMatch(item -> 
                item.getPrice() >= 5.0 && 
                item.getPrice() <= 20.0 && 
                "Electronics".equals(item.getCategory())));
        }
    }
}
