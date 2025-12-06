package unit.service;

import service.ItemService;
import dao.ItemDao;
import model.Item;

import dao.ItemDao;
import model.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemDao itemDao;

    @InjectMocks
    private ItemService itemService;

    @Test
    void testPublishItemSuccess() {
        Long sellerId = 1L;
        String title = "Test Item";
        String description = "This is a test item";
        double price = 100.0;
        String category = "Electronics";
        String imageUrl = "http://example.com/image.jpg";

        String result = itemService.publishItem(sellerId, title, description, price, category, imageUrl);
        assertNull(result); // Success returns null

        // 验证是否调用了save方法
        verify(itemDao, times(1)).save(any(Item.class));
    }

    @Test
    void testPublishItemEmptyTitle() {
        Long sellerId = 1L;
        String title = "";
        String description = "This is a test item";
        double price = 100.0;
        String category = "Electronics";

        String result = itemService.publishItem(sellerId, title, description, price, category, null);
        assertNotNull(result);
        assertEquals("Item title cannot be empty", result);
        verify(itemDao, never()).save(any(Item.class));
    }

    @Test
    void testPublishItemInvalidPrice() {
        Long sellerId = 1L;
        String title = "Test Item";
        String description = "This is a test item";
        double price = -100.0; // Invalid price
        String category = "Electronics";

        String result = itemService.publishItem(sellerId, title, description, price, category, null);
        assertNotNull(result);
        assertEquals("Item price must be greater than 0", result);
        verify(itemDao, never()).save(any(Item.class));
    }

    @Test
    void testGetAllActiveItems() {
        List<Item> items = new ArrayList<>();
        Item activeItem = new Item();
        activeItem.setId(1L);
        activeItem.setActive(true);
        items.add(activeItem);
        
        Item inactiveItem = new Item();
        inactiveItem.setId(2L);
        inactiveItem.setActive(false);
        items.add(inactiveItem);

        when(itemDao.findAll()).thenReturn(items);

        List<Item> result = itemService.getAllActiveItems();
        assertEquals(1, result.size());
        assertEquals(activeItem, result.get(0));
        verify(itemDao, times(1)).findAll();
    }

    @Test
    void testGetAllItems() {
        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        Item item2 = new Item();
        item2.setId(2L);
        items.add(item1);
        items.add(item2);

        when(itemDao.findAll()).thenReturn(items);

        List<Item> result = itemService.getAllItems();
        assertEquals(2, result.size());
        assertEquals(items, result);
        verify(itemDao, times(1)).findAll();
    }

    @Test
    void testGetItemById() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setTitle("Test Item");

        when(itemDao.findById(itemId)).thenReturn(item);

        Item result = itemService.getItemById(itemId);
        assertEquals(item, result);
        verify(itemDao, times(1)).findById(itemId);
    }

    @Test
    void testGetItemsBySeller() {
        Long sellerId = 1L;
        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setSellerId(sellerId);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setSellerId(sellerId);
        items.add(item1);
        items.add(item2);

        when(itemDao.findBySellerId(sellerId)).thenReturn(items);

        List<Item> result = itemService.getItemsBySeller(sellerId);
        assertEquals(2, result.size());
        assertEquals(items, result);
        verify(itemDao, times(1)).findBySellerId(sellerId);
    }

    @Test
    void testSearchItemsWithKeyword() {
        String keyword = "test";
        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Test Item");
        item1.setActive(true);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Another Test Item");
        item2.setActive(true);
        items.add(item1);
        items.add(item2);

        when(itemDao.search(keyword)).thenReturn(items);

        List<Item> result = itemService.searchItems(keyword);
        assertEquals(2, result.size());
        verify(itemDao, times(1)).search(keyword);
    }

    @Test
    void testSearchItemsWithoutKeyword() {
        List<Item> activeItems = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setActive(true);
        activeItems.add(item1);

        when(itemDao.findAll()).thenReturn(activeItems);

        List<Item> result = itemService.searchItems(null);
        assertEquals(1, result.size());
        verify(itemDao, times(1)).findAll();
        verify(itemDao, never()).search(anyString());
    }

    @Test
    void testGetItemsByCategory() {
        String category = "Electronics";
        List<Item> allActiveItems = new ArrayList<>();
        Item electronicsItem = new Item();
        electronicsItem.setId(1L);
        electronicsItem.setCategory(category);
        electronicsItem.setActive(true);
        allActiveItems.add(electronicsItem);
        
        Item clothingItem = new Item();
        clothingItem.setId(2L);
        clothingItem.setCategory("Clothing");
        clothingItem.setActive(true);
        allActiveItems.add(clothingItem);

        when(itemDao.findAll()).thenReturn(allActiveItems);

        List<Item> result = itemService.getItemsByCategory(category);
        assertEquals(1, result.size());
        assertEquals(electronicsItem, result.get(0));
        verify(itemDao, times(1)).findAll();
    }

    @Test
    void testUpdateItemSuccess() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Updated Test Item");
        item.setPrice(150.0);

        String result = itemService.updateItem(item);
        assertNull(result); // Success returns null
        verify(itemDao, times(1)).update(item);
    }

    @Test
    void testUpdateItemEmptyTitle() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle(""); // Empty title
        item.setPrice(150.0);

        String result = itemService.updateItem(item);
        assertNotNull(result);
        assertEquals("Item title cannot be empty", result);
        verify(itemDao, never()).update(item);
    }

    @Test
    void testUpdateItemInvalidPrice() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setPrice(-50.0); // Invalid price

        String result = itemService.updateItem(item);
        assertNotNull(result);
        assertEquals("Item price must be greater than 0", result);
        verify(itemDao, never()).update(item);
    }

    @Test
    void testDeactivateItem() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setActive(true);

        when(itemDao.findById(itemId)).thenReturn(item);

        itemService.deactivateItem(itemId);
        assertFalse(item.isActive());
        verify(itemDao, times(1)).update(item);
    }

    @Test
    void testActivateItem() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setActive(false);

        when(itemDao.findById(itemId)).thenReturn(item);

        itemService.activateItem(itemId);
        assertTrue(item.isActive());
        verify(itemDao, times(1)).update(item);
    }

    @Test
    void testDeleteItem() {
        Long itemId = 1L;
        
        itemService.deleteItem(itemId);
        verify(itemDao, times(1)).delete(itemId);
    }

    @Test
    void testIsItemOwner() {
        Long itemId = 1L;
        Long ownerId = 1L;
        Long notOwnerId = 2L;
        
        Item item = new Item();
        item.setId(itemId);
        item.setSellerId(ownerId);

        when(itemDao.findById(itemId)).thenReturn(item);

        // 测试所有者
        boolean result1 = itemService.isItemOwner(itemId, ownerId);
        assertTrue(result1);
        
        // 测试非所有者
        boolean result2 = itemService.isItemOwner(itemId, notOwnerId);
        assertFalse(result2);
        
        verify(itemDao, times(2)).findById(itemId);
    }
}
