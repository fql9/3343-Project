package dao;

import model.Item;
import java.util.List;

public interface ItemDao {

    Item findById(Long id);

    List<Item> findAll();

    List<Item> findBySellerId(Long sellerId);

    List<Item> search(String keyword);

    /**
     * Search items with multiple criteria
     * @param keyword Keyword for title/description (can be null)
     * @param minPrice Minimum price (can be null)
     * @param maxPrice Maximum price (can be null)
     * @param category Category (can be null)
     * @param sortBy Sort order (e.g., "price_asc", "price_desc", "newest")
     * @return List of items matching criteria
     */
    List<Item> searchItems(String keyword, Double minPrice, Double maxPrice, String category, String sortBy);

    void save(Item item);

    void update(Item item);

    void delete(Long id);
}
