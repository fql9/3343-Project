package service;

import dao.FavoriteDao;
import dao.ItemDao;
import dao.impl.FavoriteDaoImpl;
import dao.impl.ItemDaoImpl;
import model.Favorite;
import model.Item;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Favorite service class - handles favorite-related business logic.
 * Provides favorite management functions for users.
 */
public class FavoriteService {

    private final FavoriteDao favoriteDao;
    private final ItemDao itemDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public FavoriteService() {
        this.favoriteDao = new FavoriteDaoImpl();
        this.itemDao = new ItemDaoImpl();
    }
    
    /**
     * Add favorite
     * @param userId User ID
     * @param itemId Item ID
     * @return Add result message
     */
    public String addFavorite(Long userId, Long itemId) {
        // Check if item exists
        Item item = itemDao.findById(itemId);
        if (item == null) {
            return "Item does not exist";
        }
        
        // Check if already favorited
        if (favoriteDao.exists(userId, itemId)) {
            return "Already favorited this item";
        }
        
        // Create favorite
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);
        favorite.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        
        favoriteDao.save(favorite);
        return null; // Success returns null
    }
    
    /**
     * Remove favorite
     * @param userId User ID
     * @param itemId Item ID
     */
    public void removeFavorite(Long userId, Long itemId) {
        favoriteDao.deleteByUserAndItem(userId, itemId);
    }
    
    /**
     * Check if favorited
     * @param userId User ID
     * @param itemId Item ID
     * @return Whether favorited
     */
    public boolean isFavorite(Long userId, Long itemId) {
        return favoriteDao.exists(userId, itemId);
    }
    
    /**
     * Get user's favorite list
     * @param userId User ID
     * @return Favorite list
     */
    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteDao.findByUserId(userId);
    }
    
    /**
     * Get user's favorited items
     * @param userId User ID
     * @return Item list
     */
    public List<Item> getUserFavoriteItems(Long userId) {
        List<Favorite> favorites = favoriteDao.findByUserId(userId);
        return favorites.stream()
            .map(fav -> itemDao.findById(fav.getItemId()))
            .filter(item -> item != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Get favorite count for item
     * @param itemId Item ID
     * @return Favorite count
     */
    public int getFavoriteCount(Long itemId) {
        return favoriteDao.findByItemId(itemId).size();
    }
    
}
