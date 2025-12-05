package dao;

import model.Favorite;
import java.util.List;

public interface FavoriteDao {

    Favorite findById(Long id);

    List<Favorite> findByUserId(Long userId);

    List<Favorite> findByItemId(Long itemId);

    boolean exists(Long userId, Long itemId);

    void save(Favorite favorite);

    void delete(Long id);

    void deleteByUserAndItem(Long userId, Long itemId);
}
