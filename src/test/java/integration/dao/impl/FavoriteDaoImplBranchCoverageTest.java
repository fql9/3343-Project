package integration.dao.impl;

import integration.IntegrationTestBase;
import dao.impl.FavoriteDaoImpl;
import dao.impl.UserDaoImpl;
import dao.impl.ItemDaoImpl;
import dao.FavoriteDao;
import dao.UserDao;
import dao.ItemDao;
import model.Favorite;
import model.User;
import model.Item;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 FavoriteDaoImpl 分支覆盖率的增强测试
 * 目标: 将分支覆盖率从 70% 提升至 90%+
 */
class FavoriteDaoImplBranchCoverageTest extends IntegrationTestBase {

    private FavoriteDao favoriteDao;
    private UserDao userDao;
    private ItemDao itemDao;

    @BeforeEach
    void setUp() {
        favoriteDao = new FavoriteDaoImpl();
        userDao = new UserDaoImpl();
        itemDao = new ItemDaoImpl();
    }

    // ========== findById 分支测试 ==========
    
    @Test
    void testFindById_Exists() {
        User user = createAndSaveUser("fav_findbyid_user");
        Item item = createAndSaveItem(user.getId(), "Fav FindById Item");
        
        Favorite fav = new Favorite();
        fav.setUserId(user.getId());
        fav.setItemId(item.getId());
        fav.setCreatedTime("2024-01-01 00:00:00");
        favoriteDao.save(fav);
        
        // 获取保存后的收藏
        List<Favorite> favs = favoriteDao.findByUserId(user.getId());
        assertFalse(favs.isEmpty());
        
        Favorite found = favoriteDao.findById(favs.get(0).getId());
        assertNotNull(found);
        assertEquals(user.getId(), found.getUserId());
        assertEquals(item.getId(), found.getItemId());
    }

    @Test
    void testFindById_NotExists() {
        Favorite found = favoriteDao.findById(999999L);
        assertNull(found);
    }

    // ========== findByUserId 分支测试 ==========
    
    @Test
    void testFindByUserId_Empty() {
        List<Favorite> favs = favoriteDao.findByUserId(999999L);
        assertNotNull(favs);
        assertTrue(favs.isEmpty());
    }

    @Test
    void testFindByUserId_WithMultipleFavorites() {
        User user = createAndSaveUser("fav_multi_user");
        Item item1 = createAndSaveItem(user.getId(), "Fav Item 1");
        Item item2 = createAndSaveItem(user.getId(), "Fav Item 2");
        Item item3 = createAndSaveItem(user.getId(), "Fav Item 3");
        
        favoriteDao.save(createFavorite(user.getId(), item1.getId()));
        favoriteDao.save(createFavorite(user.getId(), item2.getId()));
        favoriteDao.save(createFavorite(user.getId(), item3.getId()));
        
        List<Favorite> favs = favoriteDao.findByUserId(user.getId());
        
        assertNotNull(favs);
        assertEquals(3, favs.size());
    }

    // ========== findByItemId 分支测试 ==========
    
    @Test
    void testFindByItemId_Empty() {
        List<Favorite> favs = favoriteDao.findByItemId(999999L);
        assertNotNull(favs);
        assertTrue(favs.isEmpty());
    }

    @Test
    void testFindByItemId_WithMultipleUsers() {
        User seller = createAndSaveUser("fav_item_seller");
        Item item = createAndSaveItem(seller.getId(), "Popular Item");
        
        User user1 = createAndSaveUser("fav_item_user1");
        User user2 = createAndSaveUser("fav_item_user2");
        User user3 = createAndSaveUser("fav_item_user3");
        
        favoriteDao.save(createFavorite(user1.getId(), item.getId()));
        favoriteDao.save(createFavorite(user2.getId(), item.getId()));
        favoriteDao.save(createFavorite(user3.getId(), item.getId()));
        
        List<Favorite> favs = favoriteDao.findByItemId(item.getId());
        
        assertNotNull(favs);
        assertEquals(3, favs.size());
    }

    // ========== exists 分支测试 ==========
    
    @Test
    void testExists_True() {
        User user = createAndSaveUser("fav_exists_user");
        Item item = createAndSaveItem(user.getId(), "Exists Item");
        
        favoriteDao.save(createFavorite(user.getId(), item.getId()));
        
        boolean exists = favoriteDao.exists(user.getId(), item.getId());
        assertTrue(exists);
    }

    @Test
    void testExists_False_NoFavorite() {
        boolean exists = favoriteDao.exists(999999L, 888888L);
        assertFalse(exists);
    }

    @Test
    void testExists_False_WrongUser() {
        User user = createAndSaveUser("fav_wrong_user");
        Item item = createAndSaveItem(user.getId(), "Wrong User Item");
        
        favoriteDao.save(createFavorite(user.getId(), item.getId()));
        
        // 不同的用户 ID
        boolean exists = favoriteDao.exists(999999L, item.getId());
        assertFalse(exists);
    }

    @Test
    void testExists_False_WrongItem() {
        User user = createAndSaveUser("fav_wrong_item_user");
        Item item = createAndSaveItem(user.getId(), "Wrong Item Test");
        
        favoriteDao.save(createFavorite(user.getId(), item.getId()));
        
        // 不同的商品 ID
        boolean exists = favoriteDao.exists(user.getId(), 999999L);
        assertFalse(exists);
    }

    // ========== save 分支测试 ==========
    
    @Test
    void testSave_NewFavorite() {
        User user = createAndSaveUser("fav_save_user");
        Item item = createAndSaveItem(user.getId(), "Save Fav Item");
        
        Favorite fav = createFavorite(user.getId(), item.getId());
        favoriteDao.save(fav);
        
        boolean exists = favoriteDao.exists(user.getId(), item.getId());
        assertTrue(exists);
    }

    // ========== delete 分支测试 ==========
    
    @Test
    void testDelete_ExistingFavorite() {
        User user = createAndSaveUser("fav_delete_user");
        Item item = createAndSaveItem(user.getId(), "Delete Fav Item");
        
        favoriteDao.save(createFavorite(user.getId(), item.getId()));
        
        List<Favorite> favs = favoriteDao.findByUserId(user.getId());
        assertFalse(favs.isEmpty());
        
        favoriteDao.delete(favs.get(0).getId());
        
        boolean exists = favoriteDao.exists(user.getId(), item.getId());
        assertFalse(exists);
    }

    @Test
    void testDelete_NonExisting() {
        // 删除不存在的收藏不应抛异常
        assertDoesNotThrow(() -> favoriteDao.delete(999999L));
    }

    // ========== deleteByUserAndItem 分支测试 ==========
    
    @Test
    void testDeleteByUserAndItem_Exists() {
        User user = createAndSaveUser("fav_del_ua_user");
        Item item = createAndSaveItem(user.getId(), "Del UA Item");
        
        favoriteDao.save(createFavorite(user.getId(), item.getId()));
        assertTrue(favoriteDao.exists(user.getId(), item.getId()));
        
        favoriteDao.deleteByUserAndItem(user.getId(), item.getId());
        
        assertFalse(favoriteDao.exists(user.getId(), item.getId()));
    }

    @Test
    void testDeleteByUserAndItem_NotExists() {
        // 删除不存在的收藏不应抛异常
        assertDoesNotThrow(() -> favoriteDao.deleteByUserAndItem(999999L, 888888L));
    }

    // ========== 辅助方法 ==========
    
    private User createAndSaveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash123");
        user.setEmail(username + "@test.com");
        user.setRole(UserRole.BUYER);
        user.setActive(true);
        user.setCreatedTime("2024-01-01 00:00:00");
        userDao.save(user);
        return userDao.findByUsername(username);
    }
    
    private Item createAndSaveItem(Long sellerId, String title) {
        Item item = new Item();
        item.setSellerId(sellerId);
        item.setTitle(title);
        item.setDescription("Test description");
        item.setPrice(100.0);
        item.setCategory("Electronics");
        item.setActive(true);
        item.setCreatedTime("2024-01-01 00:00:00");
        itemDao.save(item);
        
        List<Item> items = itemDao.findBySellerId(sellerId);
        return items.get(items.size() - 1);
    }
    
    private Favorite createFavorite(Long userId, Long itemId) {
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setItemId(itemId);
        fav.setCreatedTime("2024-01-01 00:00:00");
        return fav;
    }
}

