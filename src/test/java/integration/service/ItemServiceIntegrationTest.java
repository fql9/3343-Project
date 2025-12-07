package integration.service;

import integration.IntegrationTestBase;
import model.Item;
import service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceIntegrationTest extends IntegrationTestBase {

    private ItemService itemService;
    private static final Long TEST_SELLER_ID = 999L;

    @BeforeEach
    void setUp() {
        itemService = new ItemService();
    }

    @Test
    void testPublishItemWithNullImageUrl() {
        // 测试imageUrl为null的情况
        String result = itemService.publishItem(
            TEST_SELLER_ID,
            "Test Item",
            "Description",
            100.0,
            "Electronics",
            null  // null imageUrl
        );
        assertNull(result, "null imageUrl应该被接受");
    }

    @Test
    void testPublishItemWithEmptyImageUrl() {
        // 测试imageUrl为空字符串的情况
        String result = itemService.publishItem(
            TEST_SELLER_ID,
            "Test Item",
            "Description",
            100.0,
            "Electronics",
            ""  // empty imageUrl
        );
        assertNull(result, "空imageUrl应该被接受");
    }

    @Test
    void testPublishItemWithZeroPrice() {
        // 测试价格为0的边界情况
        String result = itemService.publishItem(
            TEST_SELLER_ID,
            "Test Item",
            "Description",
            0.0,  // 价格为0
            "Electronics",
            null
        );
        assertEquals("Item price must be greater than 0", result);
    }

    @Test
    void testPublishItemWithNegativePrice() {
        // 测试负价格
        String result = itemService.publishItem(
            TEST_SELLER_ID,
            "Test Item",
            "Description",
            -50.0,
            "Electronics",
            null
        );
        assertEquals("Item price must be greater than 0", result);
    }

    @Test
    void testPublishItemWithWhitespaceTitle() {
        // 测试只有空格的标题
        String result = itemService.publishItem(
            TEST_SELLER_ID,
            "   ",  // 只有空格
            "Description",
            100.0,
            "Electronics",
            null
        );
        assertEquals("Item title cannot be empty", result);
    }

    @Test
    void testPublishItemWithNullTitle() {
        // 测试null标题
        String result = itemService.publishItem(
            TEST_SELLER_ID,
            null,
            "Description",
            100.0,
            "Electronics",
            null
        );
        assertEquals("Item title cannot be empty", result);
    }

    @Test
    void testUpdateItemWithNullTitle() {
        // 先创建一个item
        itemService.publishItem(TEST_SELLER_ID, "Original", "Desc", 100.0, "Cat", null);
        List<Item> items = itemService.getItemsBySeller(TEST_SELLER_ID);
        Item item = items.get(0);
        
        // 尝试更新为null标题
        item.setTitle(null);
        String result = itemService.updateItem(item);
        assertEquals("Item title cannot be empty", result);
    }

    @Test
    void testUpdateItemWithWhitespaceTitle() {
        // 先创建一个item
        itemService.publishItem(TEST_SELLER_ID, "Original", "Desc", 100.0, "Cat", null);
        List<Item> items = itemService.getItemsBySeller(TEST_SELLER_ID);
        Item item = items.get(0);
        
        // 尝试更新为空格标题
        item.setTitle("   ");
        String result = itemService.updateItem(item);
        assertEquals("Item title cannot be empty", result);
    }

    @Test
    void testUpdateItemWithZeroPrice() {
        // 先创建一个item
        itemService.publishItem(TEST_SELLER_ID, "Original", "Desc", 100.0, "Cat", null);
        List<Item> items = itemService.getItemsBySeller(TEST_SELLER_ID);
        Item item = items.get(0);
        
        // 尝试更新为0价格
        item.setPrice(0.0);
        String result = itemService.updateItem(item);
        assertEquals("Item price must be greater than 0", result);
    }

    @Test
    void testUpdateItemWithNegativePrice() {
        // 先创建一个item
        itemService.publishItem(TEST_SELLER_ID, "Original", "Desc", 100.0, "Cat", null);
        List<Item> items = itemService.getItemsBySeller(TEST_SELLER_ID);
        Item item = items.get(0);
        
        // 尝试更新为负价格
        item.setPrice(-100.0);
        String result = itemService.updateItem(item);
        assertEquals("Item price must be greater than 0", result);
    }

    @Test
    void testSearchItemsWithEmptyKeyword() {
        // 创建一些测试数据
        itemService.publishItem(TEST_SELLER_ID, "Item1", "Desc1", 100.0, "Cat1", null);
        itemService.publishItem(TEST_SELLER_ID, "Item2", "Desc2", 200.0, "Cat2", null);
        
        // 使用空字符串搜索
        List<Item> results = itemService.searchItems("");
        assertEquals(2, results.size(), "空字符串应该返回所有active items");
    }

    @Test
    void testSearchItemsWithWhitespaceKeyword() {
        // 创建一些测试数据
        itemService.publishItem(TEST_SELLER_ID, "Item1", "Desc1", 100.0, "Cat1", null);
        
        // 使用空格搜索
        List<Item> results = itemService.searchItems("   ");
        assertEquals(1, results.size(), "空格应该返回所有active items");
    }

    @Test
    void testGetItemsByNonExistentCategory() {
        // 创建一些测试数据
        itemService.publishItem(TEST_SELLER_ID, "Item1", "Desc1", 100.0, "Electronics", null);
        
        // 搜索不存在的类别
        List<Item> results = itemService.getItemsByCategory("NonExistent");
        assertEquals(0, results.size(), "不存在的类别应该返回空列表");
    }

    @Test
    void testDeactivateNonExistentItem() {
        // 尝试停用不存在的item
        assertDoesNotThrow(() -> itemService.deactivateItem(99999L));
    }

    @Test
    void testActivateNonExistentItem() {
        // 尝试激活不存在的item
        assertDoesNotThrow(() -> itemService.activateItem(99999L));
    }

    @Test
    void testIsItemOwnerWithNonExistentItem() {
        // 检查不存在的item的所有权
        boolean result = itemService.isItemOwner(99999L, TEST_SELLER_ID);
        assertFalse(result);
    }

    @Test
    void testGetItemByIdReturnsNull() {
        // 获取不存在的item
        Item item = itemService.getItemById(99999L);
        assertNull(item);
    }

    @Test
    void testGetItemsBySellerWithNoItems() {
        // 获取没有item的seller
        List<Item> items = itemService.getItemsBySeller(88888L);
        assertTrue(items.isEmpty());
    }

    @Test
    void testGetAllItemsIncludesInactive() {
        // 创建active和inactive items
        itemService.publishItem(TEST_SELLER_ID, "Active", "Desc", 100.0, "Cat", null);
        List<Item> items = itemService.getItemsBySeller(TEST_SELLER_ID);
        Long itemId = items.get(0).getId();
        
        itemService.deactivateItem(itemId);
        
        // getAllItems应该包含inactive items
        List<Item> allItems = itemService.getAllItems();
        assertTrue(allItems.stream().anyMatch(i -> i.getId().equals(itemId)));
    }

    @Test
    void testGetAllActiveItemsExcludesInactive() {
        // 创建active和inactive items
        itemService.publishItem(TEST_SELLER_ID, "Active", "Desc", 100.0, "Cat", null);
        List<Item> items = itemService.getItemsBySeller(TEST_SELLER_ID);
        Long itemId = items.get(0).getId();
        
        itemService.deactivateItem(itemId);
        
        // getAllActiveItems不应该包含inactive items
        List<Item> activeItems = itemService.getAllActiveItems();
        assertFalse(activeItems.stream().anyMatch(i -> i.getId().equals(itemId)));
    }
}

