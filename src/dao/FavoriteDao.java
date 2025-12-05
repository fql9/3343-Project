package dao;

import model.Favorite;
import java.util.List;

/**
 * Favorite Data Access Object interface.
 * Note: The favorites table uses a composite primary key (user_id, item_id),
 * so operations should use these fields instead of a separate id.
 */
public interface FavoriteDao {

    List<Favorite> findByUserId(Long userId);

    List<Favorite> findByItemId(Long itemId);

    boolean exists(Long userId, Long itemId);

    void save(Favorite favorite);

    void deleteByUserAndItem(Long userId, Long itemId);
}
