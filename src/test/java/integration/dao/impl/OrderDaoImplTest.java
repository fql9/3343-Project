package integration.dao.impl;

import config.DatabaseConfig;
import dao.OrderDao;
import dao.impl.OrderDaoImpl;
import integration.IntegrationTestBase;
import model.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class OrderDaoImplTest extends IntegrationTestBase {
    private OrderDao orderDao;
    private Order testOrder;
    private static final Long TEST_BUYER_ID = 1L;
    private static final Long TEST_SELLER_ID = 2L;
    private static final Long TEST_ITEM_ID = 1L;

    @BeforeEach
    void setUp() {
        orderDao = new OrderDaoImpl();
        
        // 创建测试订单
        testOrder = new Order();
        testOrder.setOrderNo("TEST-ORDER-" + System.currentTimeMillis());
        testOrder.setBuyerId(TEST_BUYER_ID);
        testOrder.setSellerId(TEST_SELLER_ID);
        testOrder.setItemId(TEST_ITEM_ID);
        testOrder.setAmount(99.99);
        testOrder.setStatus("Pending");
        testOrder.setShippingAddress("123 Test Street");
        testOrder.setCreatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 清理测试数据
        clearTestData();
    }

    @AfterEach
    void tearDown() {
        clearTestData();
    }

    private void clearTestData() {
        String sql = "DELETE FROM orders WHERE order_no LIKE 'TEST-ORDER-%'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSave() {
        // 保存订单
        orderDao.save(testOrder);
        
        // 验证保存成功
        var orders = orderDao.findByBuyerId(TEST_BUYER_ID);
        assertFalse(orders.isEmpty());
        
        Order savedOrder = orders.stream()
            .filter(o -> o.getOrderNo().equals(testOrder.getOrderNo()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(savedOrder);
        assertEquals(testOrder.getOrderNo(), savedOrder.getOrderNo());
        assertEquals(TEST_BUYER_ID, savedOrder.getBuyerId());
        assertEquals(TEST_SELLER_ID, savedOrder.getSellerId());
        assertEquals(99.99, savedOrder.getAmount());
    }

    @Test
    void testUpdate() {
        // 保存订单
        orderDao.save(testOrder);
        
        // 查找并更新
        var orders = orderDao.findByBuyerId(TEST_BUYER_ID);
        Order savedOrder = orders.stream()
            .filter(o -> o.getOrderNo().equals(testOrder.getOrderNo()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(savedOrder);
        
        savedOrder.setStatus("Completed");
        savedOrder.setShippingAddress("456 Updated Street");
        orderDao.update(savedOrder);
        
        // 验证更新成功
        Order updatedOrder = orderDao.findById(savedOrder.getId());
        assertNotNull(updatedOrder);
        assertEquals("Completed", updatedOrder.getStatus());
        assertEquals("456 Updated Street", updatedOrder.getShippingAddress());
    }

    @Test
    void testFindById() {
        // 保存订单
        orderDao.save(testOrder);
        
        // 通过买家ID查找
        var orders = orderDao.findByBuyerId(TEST_BUYER_ID);
        assertFalse(orders.isEmpty());
        
        Order savedOrder = orders.stream()
            .filter(o -> o.getOrderNo().equals(testOrder.getOrderNo()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(savedOrder);
        
        // 通过ID查找
        Order foundOrder = orderDao.findById(savedOrder.getId());
        assertNotNull(foundOrder);
        assertEquals(savedOrder.getId(), foundOrder.getId());
        assertEquals(testOrder.getOrderNo(), foundOrder.getOrderNo());
    }

    @Test
    void testFindById_NotFound() {
        // 查找不存在的ID
        Order notFound = orderDao.findById(999999L);
        assertNull(notFound);
    }

    @Test
    void testFindByOrderNo() {
        // 保存订单
        orderDao.save(testOrder);
        
        // 通过订单号查找
        Order foundOrder = orderDao.findByOrderNo(testOrder.getOrderNo());
        
        assertNotNull(foundOrder);
        assertEquals(testOrder.getOrderNo(), foundOrder.getOrderNo());
        assertEquals(TEST_BUYER_ID, foundOrder.getBuyerId());
        assertEquals(TEST_SELLER_ID, foundOrder.getSellerId());
        assertEquals(99.99, foundOrder.getAmount());
    }

    @Test
    void testFindByOrderNo_NotFound() {
        // 查找不存在的订单号
        Order notFound = orderDao.findByOrderNo("NON-EXISTENT-ORDER");
        assertNull(notFound);
    }

    @Test
    void testFindByBuyerId() {
        // 保存订单
        orderDao.save(testOrder);
        
        // 通过买家ID查找
        var orders = orderDao.findByBuyerId(TEST_BUYER_ID);
        
        assertNotNull(orders);
        assertTrue(orders.stream().anyMatch(o -> o.getOrderNo().equals(testOrder.getOrderNo())));
    }

    @Test
    void testFindByBuyerId_EmptyResult() {
        // 查找没有订单的买家
        var orders = orderDao.findByBuyerId(999999L);
        
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testFindBySellerId() {
        // 保存订单
        orderDao.save(testOrder);
        
        // 通过卖家ID查找
        var orders = orderDao.findBySellerId(TEST_SELLER_ID);
        
        assertNotNull(orders);
        assertTrue(orders.stream().anyMatch(o -> o.getOrderNo().equals(testOrder.getOrderNo())));
    }

    @Test
    void testFindBySellerId_EmptyResult() {
        // 查找没有订单的卖家
        var orders = orderDao.findBySellerId(999999L);
        
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }
}
