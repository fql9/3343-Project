package integration.dao.impl;

import integration.IntegrationTestBase;
import dao.impl.OrderDaoImpl;
import dao.impl.UserDaoImpl;
import dao.impl.ItemDaoImpl;
import dao.OrderDao;
import dao.UserDao;
import dao.ItemDao;
import model.Order;
import model.User;
import model.Item;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 OrderDaoImpl 分支覆盖率的增强测试
 * 目标: 将分支覆盖率从 75% 提升至 90%+
 */
class OrderDaoImplBranchCoverageTest extends IntegrationTestBase {

    private OrderDao orderDao;
    private UserDao userDao;
    private ItemDao itemDao;

    @BeforeEach
    void setUp() {
        orderDao = new OrderDaoImpl();
        userDao = new UserDaoImpl();
        itemDao = new ItemDaoImpl();
    }

    // ========== findById 分支测试 ==========
    
    @Test
    void testFindById_Exists() {
        User seller = createAndSaveUser("order_findbyid_seller");
        User buyer = createAndSaveUser("order_findbyid_buyer");
        Item item = createAndSaveItem(seller.getId(), "Order FindById Item");
        
        Order order = createOrder(buyer.getId(), seller.getId(), item.getId());
        orderDao.save(order);
        
        List<Order> orders = orderDao.findByBuyerId(buyer.getId());
        assertFalse(orders.isEmpty());
        
        Order found = orderDao.findById(orders.get(0).getId());
        assertNotNull(found);
        assertEquals(buyer.getId(), found.getBuyerId());
    }

    @Test
    void testFindById_NotExists() {
        Order found = orderDao.findById(999999L);
        assertNull(found);
    }

    // ========== findByOrderNo 分支测试 ==========
    
    @Test
    void testFindByOrderNo_Exists() {
        User seller = createAndSaveUser("order_byno_seller");
        User buyer = createAndSaveUser("order_byno_buyer");
        Item item = createAndSaveItem(seller.getId(), "Order ByNo Item");
        
        String orderNo = "ORD_TEST_" + UUID.randomUUID().toString().substring(0, 8);
        Order order = createOrder(buyer.getId(), seller.getId(), item.getId());
        order.setOrderNo(orderNo);
        orderDao.save(order);
        
        Order found = orderDao.findByOrderNo(orderNo);
        assertNotNull(found);
        assertEquals(orderNo, found.getOrderNo());
    }

    @Test
    void testFindByOrderNo_NotExists() {
        Order found = orderDao.findByOrderNo("NONEXISTENT_ORDER_12345");
        assertNull(found);
    }

    // ========== findByBuyerId 分支测试 ==========
    
    @Test
    void testFindByBuyerId_Empty() {
        List<Order> orders = orderDao.findByBuyerId(999999L);
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testFindByBuyerId_WithMultipleOrders() {
        User seller = createAndSaveUser("order_multi_seller");
        User buyer = createAndSaveUser("order_multi_buyer");
        Item item1 = createAndSaveItem(seller.getId(), "Multi Order Item 1");
        Item item2 = createAndSaveItem(seller.getId(), "Multi Order Item 2");
        Item item3 = createAndSaveItem(seller.getId(), "Multi Order Item 3");
        
        orderDao.save(createOrder(buyer.getId(), seller.getId(), item1.getId()));
        orderDao.save(createOrder(buyer.getId(), seller.getId(), item2.getId()));
        orderDao.save(createOrder(buyer.getId(), seller.getId(), item3.getId()));
        
        List<Order> orders = orderDao.findByBuyerId(buyer.getId());
        
        assertNotNull(orders);
        assertEquals(3, orders.size());
    }

    // ========== findBySellerId 分支测试 ==========
    
    @Test
    void testFindBySellerId_Empty() {
        List<Order> orders = orderDao.findBySellerId(999999L);
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testFindBySellerId_WithMultipleOrders() {
        User seller = createAndSaveUser("order_seller_multi");
        User buyer1 = createAndSaveUser("order_buyer1");
        User buyer2 = createAndSaveUser("order_buyer2");
        Item item = createAndSaveItem(seller.getId(), "Seller Multi Item");
        
        orderDao.save(createOrder(buyer1.getId(), seller.getId(), item.getId()));
        orderDao.save(createOrder(buyer2.getId(), seller.getId(), item.getId()));
        
        List<Order> orders = orderDao.findBySellerId(seller.getId());
        
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    // ========== save 分支测试 ==========
    
    @Test
    void testSave_AllFields() {
        User seller = createAndSaveUser("order_save_seller");
        User buyer = createAndSaveUser("order_save_buyer");
        Item item = createAndSaveItem(seller.getId(), "Save Order Item");
        
        Order order = new Order();
        order.setOrderNo("ORD_FULL_" + UUID.randomUUID().toString().substring(0, 8));
        order.setBuyerId(buyer.getId());
        order.setSellerId(seller.getId());
        order.setItemId(item.getId());
        order.setAmount(199.99);
        order.setStatus("PENDING");
        order.setShippingAddress("123 Full Test Street, City, Country");
        order.setCreatedTime("2024-01-01 12:00:00");
        
        orderDao.save(order);
        
        Order found = orderDao.findByOrderNo(order.getOrderNo());
        assertNotNull(found);
        assertEquals(199.99, found.getAmount(), 0.01);
        assertEquals("PENDING", found.getStatus());
        assertEquals("123 Full Test Street, City, Country", found.getShippingAddress());
    }

    // ========== update 分支测试 ==========
    
    @Test
    void testUpdate_Status() {
        User seller = createAndSaveUser("order_update_seller");
        User buyer = createAndSaveUser("order_update_buyer");
        Item item = createAndSaveItem(seller.getId(), "Update Order Item");
        
        Order order = createOrder(buyer.getId(), seller.getId(), item.getId());
        order.setStatus("PENDING");
        orderDao.save(order);
        
        List<Order> orders = orderDao.findByBuyerId(buyer.getId());
        Order saved = orders.get(0);
        
        // 更新状态
        saved.setStatus("SHIPPED");
        orderDao.update(saved);
        
        Order updated = orderDao.findById(saved.getId());
        assertEquals("SHIPPED", updated.getStatus());
    }

    @Test
    void testUpdate_ShippingAddress() {
        User seller = createAndSaveUser("order_addr_seller");
        User buyer = createAndSaveUser("order_addr_buyer");
        Item item = createAndSaveItem(seller.getId(), "Addr Order Item");
        
        Order order = createOrder(buyer.getId(), seller.getId(), item.getId());
        order.setShippingAddress("Old Address");
        orderDao.save(order);
        
        List<Order> orders = orderDao.findByBuyerId(buyer.getId());
        Order saved = orders.get(0);
        
        // 更新地址
        saved.setShippingAddress("New Shipping Address, New City");
        orderDao.update(saved);
        
        Order updated = orderDao.findById(saved.getId());
        assertEquals("New Shipping Address, New City", updated.getShippingAddress());
    }

    @Test
    void testUpdate_StatusAndAddress() {
        User seller = createAndSaveUser("order_both_seller");
        User buyer = createAndSaveUser("order_both_buyer");
        Item item = createAndSaveItem(seller.getId(), "Both Order Item");
        
        Order order = createOrder(buyer.getId(), seller.getId(), item.getId());
        order.setStatus("PENDING");
        order.setShippingAddress("Initial Address");
        orderDao.save(order);
        
        List<Order> orders = orderDao.findByBuyerId(buyer.getId());
        Order saved = orders.get(0);
        
        // 同时更新状态和地址
        saved.setStatus("COMPLETED");
        saved.setShippingAddress("Final Delivery Address");
        orderDao.update(saved);
        
        Order updated = orderDao.findById(saved.getId());
        assertEquals("COMPLETED", updated.getStatus());
        assertEquals("Final Delivery Address", updated.getShippingAddress());
    }

    // ========== 辅助方法 ==========
    
    private User createAndSaveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash123");
        user.setEmail(username + "@test.com");
        user.setRole(UserRole.SELLER);
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
    
    private Order createOrder(Long buyerId, Long sellerId, Long itemId) {
        Order order = new Order();
        order.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8));
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setItemId(itemId);
        order.setAmount(100.0);
        order.setStatus("PENDING");
        order.setShippingAddress("123 Test St");
        order.setCreatedTime("2024-01-01 00:00:00");
        return order;
    }
}

