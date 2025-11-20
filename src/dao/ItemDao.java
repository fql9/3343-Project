package dao;

import model.Item;
import java.util.List;

public interface ItemDao {
    Item findById(Long id);
    List<Item> findAll();
    List<Item> findBySeller(Long sellerId);
    List<Item> search(String keyword);
    void save(Item item);
    void update(Item item);
    void delete(Long id);
}
