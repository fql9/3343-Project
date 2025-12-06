package unit.service;

import service.FavoriteService;
import dao.FavoriteDao;
import dao.ItemDao;
import model.Favorite;
import model.Item;

import dao.FavoriteDao;
import dao.ItemDao;
import model.Favorite;
import model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceTest {

    @Mock
    private FavoriteDao favoriteDao;
    
    @Mock
    private ItemDao itemDao;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddFavoriteSuccess() {
        Long userId = 1L;
        Long itemId = 1L;
        
        Item item = new Item();
        item.setId(itemId);
        
        when(itemDao.findById(itemId)).thenReturn(item);
        when(favoriteDao.exists(userId, itemId)).thenReturn(false);

        String result = favoriteService.addFavorite(userId, itemId);
        assertNull(result); // Success returns null

        // 验证是否调用了save方法
        verify(favoriteDao, times(1)).save(any(Favorite.class));
    }

    @Test
    void testAddFavoriteItemDoesNotExist() {
        Long userId = 1L;
        Long itemId = 1L;
        
        when(itemDao.findById(itemId)).thenReturn(null);

        String result = favoriteService.addFavorite(userId, itemId);
        assertNotNull(result);
        assertEquals("Item does not exist", result);
        verify(favoriteDao, never()).save(any(Favorite.class));
    }

    @Test
    void testAddFavoriteAlreadyFavorited() {
        Long userId = 1L;
        Long itemId = 1L;
        
        Item item = new Item();
        item.setId(itemId);
        
        when(itemDao.findById(itemId)).thenReturn(item);
        when(favoriteDao.exists(userId, itemId)).thenReturn(true);

        String result = favoriteService.addFavorite(userId, itemId);
        assertNotNull(result);
        assertEquals("Already favorited this item", result);
        verify(favoriteDao, never()).save(any(Favorite.class));
    }

    @Test
    void testRemoveFavorite() {
        Long userId = 1L;
        Long itemId = 1L;
        
        favoriteService.removeFavorite(userId, itemId);
        verify(favoriteDao, times(1)).deleteByUserAndItem(userId, itemId);
    }

    @Test
    void testIsFavorite() {
        Long userId = 1L;
        Long itemId = 1L;
        
        when(favoriteDao.exists(userId, itemId)).thenReturn(true);
        
        boolean result = favoriteService.isFavorite(userId, itemId);
        assertTrue(result);
        verify(favoriteDao, times(1)).exists(userId, itemId);
    }

    @Test
    void testIsNotFavorite() {
        Long userId = 1L;
        Long itemId = 1L;
        
        when(favoriteDao.exists(userId, itemId)).thenReturn(false);
        
        boolean result = favoriteService.isFavorite(userId, itemId);
        assertFalse(result);
        verify(favoriteDao, times(1)).exists(userId, itemId);
    }

    @Test
    void testGetUserFavorites() {
        Long userId = 1L;
        List<Favorite> favorites = new ArrayList<>();
        Favorite favorite1 = new Favorite();
        favorite1.setId(1L);
        favorite1.setUserId(userId);
        favorite1.setItemId(1L);
        favorites.add(favorite1);
        
        Favorite favorite2 = new Favorite();
        favorite2.setId(2L);
        favorite2.setUserId(userId);
        favorite2.setItemId(2L);
        favorites.add(favorite2);

        when(favoriteDao.findByUserId(userId)).thenReturn(favorites);

        List<Favorite> result = favoriteService.getUserFavorites(userId);
        assertEquals(2, result.size());
        assertEquals(favorites, result);
        verify(favoriteDao, times(1)).findByUserId(userId);
    }

    @Test
    void testGetUserFavoriteItems() {
        Long userId = 1L;
        List<Favorite> favorites = new ArrayList<>();
        Favorite favorite1 = new Favorite();
        favorite1.setId(1L);
        favorite1.setUserId(userId);
        favorite1.setItemId(1L);
        favorites.add(favorite1);
        
        Favorite favorite2 = new Favorite();
        favorite2.setId(2L);
        favorite2.setUserId(userId);
        favorite2.setItemId(2L);
        favorites.add(favorite2);
        
        Item item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Item 1");
        
        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Item 2");

        when(favoriteDao.findByUserId(userId)).thenReturn(favorites);
        when(itemDao.findById(1L)).thenReturn(item1);
        when(itemDao.findById(2L)).thenReturn(item2);

        List<Item> result = favoriteService.getUserFavoriteItems(userId);
        assertEquals(2, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(1));
        
        verify(favoriteDao, times(1)).findByUserId(userId);
        verify(itemDao, times(1)).findById(1L);
        verify(itemDao, times(1)).findById(2L);
    }

    @Test
    void testGetUserFavoriteItemsWithNullItems() {
        Long userId = 1L;
        List<Favorite> favorites = new ArrayList<>();
        Favorite favorite1 = new Favorite();
        favorite1.setId(1L);
        favorite1.setUserId(userId);
        favorite1.setItemId(1L);
        favorites.add(favorite1);
        
        Favorite favorite2 = new Favorite();
        favorite2.setId(2L);
        favorite2.setUserId(userId);
        favorite2.setItemId(2L);
        favorites.add(favorite2);

        when(favoriteDao.findByUserId(userId)).thenReturn(favorites);
        when(itemDao.findById(1L)).thenReturn(new Item());
        when(itemDao.findById(2L)).thenReturn(null); // Item not found

        List<Item> result = favoriteService.getUserFavoriteItems(userId);
        assertEquals(1, result.size()); // Only 1 valid item
        
        verify(favoriteDao, times(1)).findByUserId(userId);
        verify(itemDao, times(1)).findById(1L);
        verify(itemDao, times(1)).findById(2L);
    }

    @Test
    void testGetFavoriteCount() {
        Long itemId = 1L;
        List<Favorite> favorites = new ArrayList<>();
        favorites.add(new Favorite());
        favorites.add(new Favorite());

        when(favoriteDao.findByItemId(itemId)).thenReturn(favorites);

        int result = favoriteService.getFavoriteCount(itemId);
        assertEquals(2, result);
        verify(favoriteDao, times(1)).findByItemId(itemId);
    }

    @Test
    void testDeleteFavorite() {
        Long favoriteId = 1L;
        
        favoriteService.deleteFavorite(favoriteId);
        verify(favoriteDao, times(1)).delete(favoriteId);
    }
}
