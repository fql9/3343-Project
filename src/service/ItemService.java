package service;

import dao.ItemDao;
import dao.impl.ItemDaoImpl;
import model.Item;
import util.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item service class - handles item-related business logic
 */
public class ItemService {

    private final ItemDao itemDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public ItemService() {
        this.itemDao = new ItemDaoImpl();
    }
    
    /**
     * Publish item
     * @param sellerId Seller ID
     * @param title Title
     * @param description Description
     * @param price Price
     * @param category Category
     * @return Publish result message
     */
    public String publishItem(Long sellerId, String title, String description, 
                              double price, String category) {
        // Validate title
        if (!ValidationUtils.isNotEmpty(title)) {
            return "Item title cannot be empty";
        }
        
        // Validate price
        if (!ValidationUtils.isValidPrice(price)) {
            return "Item price must be greater than 0";
        }
        
        // Create item
        Item item = new Item();
        item.setSellerId(sellerId);
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setActive(true);
        item.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        
        itemDao.save(item);
        return null; // Success returns null
    }
    
    /**
     * Get all active items
     * @return Item list
     */
    public List<Item> getAllActiveItems() {
        return itemDao.findAll().stream()
            .filter(Item::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all items (including inactive)
     * @return Item list
     */
    public List<Item> getAllItems() {
        return itemDao.findAll();
    }
    
    /**
     * Get item by ID
     * @param itemId Item ID
     * @return Item object
     */
    public Item getItemById(Long itemId) {
        return itemDao.findById(itemId);
    }
    
    /**
     * Get items by seller
     * @param sellerId Seller ID
     * @return Item list
     */
    public List<Item> getItemsBySeller(Long sellerId) {
        return itemDao.findBySellerId(sellerId);
    }
    
    /**
     * Search items
     * @param keyword Keyword
     * @return Item list
     */
    public List<Item> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveItems();
        }
        return itemDao.search(keyword).stream()
            .filter(Item::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Filter items by category
     * @param category Category
     * @return Item list
     */
    public List<Item> getItemsByCategory(String category) {
        return getAllActiveItems().stream()
            .filter(item -> category.equals(item.getCategory()))
            .collect(Collectors.toList());
    }
    
    /**
     * Update item
     * @param item Item object
     * @return Update result message
     */
    public String updateItem(Item item) {
        if (!ValidationUtils.isNotEmpty(item.getTitle())) {
            return "Item title cannot be empty";
        }
        
        if (!ValidationUtils.isValidPrice(item.getPrice())) {
            return "Item price must be greater than 0";
        }
        
        itemDao.update(item);
        return null;
    }
    
    /**
     * Deactivate item
     * @param itemId Item ID
     */
    public void deactivateItem(Long itemId) {
        Item item = itemDao.findById(itemId);
        if (item != null) {
            item.setActive(false);
            itemDao.update(item);
        }
    }
    
    /**
     * Activate item
     * @param itemId Item ID
     */
    public void activateItem(Long itemId) {
        Item item = itemDao.findById(itemId);
        if (item != null) {
            item.setActive(true);
            itemDao.update(item);
        }
    }
    
    /**
     * Delete item
     * @param itemId Item ID
     */
    public void deleteItem(Long itemId) {
        itemDao.delete(itemId);
    }
    
    /**
     * Check if user is the item owner
     * @param itemId Item ID
     * @param userId User ID
     * @return Whether is owner
     */
    public boolean isItemOwner(Long itemId, Long userId) {
        Item item = itemDao.findById(itemId);
        return item != null && item.getSellerId().equals(userId);
    }
}
