package integration.dao.impl;

import dao.ItemDao;
import dao.impl.ItemDaoImpl;
import integration.IntegrationTestBase;
import model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ItemDaoImpl 补充测试 - 边界条件和异常情况
 * 目标: 提升分支覆盖率
 */
class ItemDaoImplCoverageTest extends IntegrationTestBase {

    private ItemDao itemDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        itemDao = new ItemDaoImpl();
    }

    @Test
    void testSaveItemWithNullImageUrl() {
        Item item = createTestItem("Test", 100.0, "Cat", null);
        itemDao.save(item);
        
        List<Item> items = itemDao.findAll();
        assertEquals(1, items.size());
        assertNull(items.get(0).getImageUrl());
    }

    @Test
    void testSaveItemWithEmptyImageUrl() {
        Item item = createTestItem("Test", 100.0, "Cat", "");
        itemDao.save(item);
        
        List<Item> items = itemDao.findAll();
        assertEquals(1, items.size());
        assertEquals("", items.get(0).getImageUrl());
    }

    @Test
    void testUpdateNonExistentItem() {
        Item item = createTestItem("Test", 100.0, "Cat", null);
        item.setId(99999L); // 不存在的ID
        
        // 应该不抛出异常
        assertDoesNotThrow(() -> itemDao.update(item));
    }

    @Test
    void testDeleteNonExistentItem() {
        // 删除不存在的item
        assertDoesNotThrow(() -> itemDao.delete(99999L));
    }

    @Test
    void testFindByIdReturnsNull() {
        Item item = itemDao.findById(99999L);
        assertNull(item);
    }

    @Test
    void testFindBySellerIdWithNoItems() {
        List<Item> items = itemDao.findBySellerId(99999L);
        assertTrue(items.isEmpty());
    }

    @Test
    void testSearchWithNoResults() {
        itemDao.save(createTestItem("Apple", 100.0, "Cat", null));
        
        List<Item> results = itemDao.search("NonExistentKeyword");
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchCaseInsensitive() {
        itemDao.save(createTestItem("iPhone Pro Max", 8999.0, "Electronics", null));
        
        // 测试大小写不敏感
        List<Item> results1 = itemDao.search("iphone");
        assertEquals(1, results1.size());
        
        List<Item> results2 = itemDao.search("IPHONE");
        assertEquals(1, results2.size());
        
        List<Item> results3 = itemDao.search("IpHoNe");
        assertEquals(1, results3.size());
    }

    @Test
    void testSearchInDescription() {
        Item item = createTestItem("Product", 100.0, "Cat", null);
        item.setDescription("This is a unique description keyword");
        itemDao.save(item);
        
        // 搜索描述中的关键词
        List<Item> results = itemDao.search("unique");
        assertEquals(1, results.size());
    }

    @Test
    void testFindAllIncludesInactiveItems() {
        // 创建active item
        Item activeItem = createTestItem("Active", 100.0, "Cat", null);
        activeItem.setActive(true);
        itemDao.save(activeItem);
        
        // 创建inactive item
        Item inactiveItem = createTestItem("Inactive", 200.0, "Cat", null);
        inactiveItem.setActive(false);
        itemDao.save(inactiveItem);
        
        List<Item> allItems = itemDao.findAll();
        assertEquals(2, allItems.size());
        
        long activeCount = allItems.stream().filter(Item::isActive).count();
        long inactiveCount = allItems.stream().filter(i -> !i.isActive()).count();
        
        assertEquals(1, activeCount);
        assertEquals(1, inactiveCount);
    }

    @Test
    void testSaveItemWithVeryLongTitle() {
        String longTitle = "A".repeat(500);
        Item item = createTestItem(longTitle, 100.0, "Cat", null);
        
        assertDoesNotThrow(() -> itemDao.save(item));
        
        List<Item> items = itemDao.findAll();
        assertEquals(1, items.size());
        assertEquals(longTitle, items.get(0).getTitle());
    }

    @Test
    void testSaveItemWithVeryLongDescription() {
        String longDesc = "B".repeat(2000);
        Item item = createTestItem("Title", 100.0, "Cat", null);
        item.setDescription(longDesc);
        
        assertDoesNotThrow(() -> itemDao.save(item));
        
        List<Item> items = itemDao.findAll();
        assertEquals(1, items.size());
        assertEquals(longDesc, items.get(0).getDescription());
    }

    @Test
    void testSaveItemWithVeryHighPrice() {
        Item item = createTestItem("Expensive", 999999999.99, "Luxury", null);
        itemDao.save(item);
        
        List<Item> items = itemDao.findAll();
        assertEquals(1, items.size());
        assertEquals(999999999.99, items.get(0).getPrice(), 0.01);
    }

    @Test
    void testSaveItemWithVeryLowPrice() {
        Item item = createTestItem("Cheap", 0.01, "Budget", null);
        itemDao.save(item);
        
        List<Item> items = itemDao.findAll();
        assertEquals(1, items.size());
        assertEquals(0.01, items.get(0).getPrice(), 0.001);
    }

    @Test
    void testUpdateItemChangesAllFields() {
        // 保存初始item
        Item original = createTestItem("Original", 100.0, "Cat1", "img1.jpg");
        itemDao.save(original);
        
        Long itemId = itemDao.findAll().get(0).getId();
        
        // 更新所有字段
        Item updated = new Item();
        updated.setId(itemId);
        updated.setSellerId(999L);
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Description");
        updated.setPrice(200.0);
        updated.setCategory("Cat2");
        updated.setImageUrl("img2.jpg");
        updated.setActive(false);
        updated.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        
        itemDao.update(updated);
        
        Item result = itemDao.findById(itemId);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(200.0, result.getPrice(), 0.01);
        assertEquals("Cat2", result.getCategory());
        assertEquals("img2.jpg", result.getImageUrl());
        assertFalse(result.isActive());
    }

    @Test
    void testSearchWithSpecialCharacters() {
        itemDao.save(createTestItem("Item with 'quotes'", 100.0, "Cat", null));
        itemDao.save(createTestItem("Item with \"double quotes\"", 100.0, "Cat", null));
        
        List<Item> results1 = itemDao.search("quotes");
        assertEquals(2, results1.size());
    }

    @Test
    void testMultipleItemsFromSameSeller() {
        Long sellerId = 777L;
        
        for (int i = 1; i <= 10; i++) {
            Item item = createTestItem("Item " + i, i * 100.0, "Cat", null);
            item.setSellerId(sellerId);
            itemDao.save(item);
        }
        
        List<Item> items = itemDao.findBySellerId(sellerId);
        assertEquals(10, items.size());
    }

    // Helper method
    private Item createTestItem(String title, double price, String category, String imageUrl) {
        Item item = new Item();
        item.setSellerId(123L);
        item.setTitle(title);
        item.setDescription("Test description");
        item.setPrice(price);
        item.setCategory(category);
        item.setImageUrl(imageUrl);
        item.setActive(true);
        item.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        return item;
    }
}

