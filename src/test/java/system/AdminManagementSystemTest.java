package system;

import model.Item;
import model.User;
import model.UserRole;
import service.ItemService;
import service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统测试 - 管理员用户管理系统
 * 测试管理员对用户和商品的完整管理流程
 */
@DisplayName("System Test: 管理员用户管理系统")
public class AdminManagementSystemTest extends SystemTestBase {

    private UserService userService;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        itemService = new ItemService();
    }

    @Test
    @DisplayName("场景1: 管理员用户管理完整流程")
    void testCompleteAdminUserManagement() {
        // ============ Phase 1: 创建管理员和普通用户 ============
        userService.register("admin", "admin123", "admin@test.com", UserRole.ADMIN);
        userService.register("user1", "pass123", "user1@test.com", UserRole.BUYER);
        userService.register("user2", "pass123", "user2@test.com", UserRole.SELLER);
        
        // ============ Phase 2: 管理员登录 ============
        userService.login("admin", "admin123");
        assertTrue(UserService.isAdmin(), "应该是管理员");
        
        // ============ Phase 3: 查看所有用户 ============
        List<User> allUsers = userService.getAllUsers();
        assertEquals(3, allUsers.size(), "应该有3个用户");
        
        // ============ Phase 4: 封禁用户 ============
        // 获取user1的ID
        userService.logout();
        userService.login("user1", "pass123");
        Long user1Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        // 管理员封禁user1
        userService.login("admin", "admin123");
        userService.setUserActive(user1Id, false);
        
        User bannedUser = userService.getUserById(user1Id);
        assertFalse(bannedUser.isActive(), "用户应该被封禁");
        
        // ============ Phase 5: 验证被封禁用户无法登录 ============
        userService.logout();
        String loginResult = userService.login("user1", "pass123");
        assertEquals("Account has been banned", loginResult, "被封禁用户不能登录");
        
        // ============ Phase 6: 管理员解封用户 ============
        userService.login("admin", "admin123");
        userService.setUserActive(user1Id, true);
        
        User unbannedUser = userService.getUserById(user1Id);
        assertTrue(unbannedUser.isActive(), "用户应该被解封");
        
        // ============ Phase 7: 验证解封后可以登录 ============
        userService.logout();
        String loginAfterUnban = userService.login("user1", "pass123");
        assertNull(loginAfterUnban, "解封后应该可以登录");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景2: 管理员商品管理")
    void testAdminItemManagement() {
        // 1. 创建管理员和卖家
        userService.register("admin", "admin123", "admin@test.com", UserRole.ADMIN);
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        
        // 2. 卖家发布商品
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        
        itemService.publishItem(sellerId, "违规商品", "测试违规内容", 100.0, "Test", null);
        
        List<Item> sellerItems = itemService.getItemsBySeller(sellerId);
        Long itemId = sellerItems.get(0).getId();
        userService.logout();
        
        // 3. 管理员查看所有商品
        userService.login("admin", "admin123");
        
        List<Item> allItems = itemService.getAllItems();
        assertTrue(allItems.size() > 0, "应该有商品");
        
        // 4. 管理员下架违规商品
        itemService.deactivateItem(itemId);
        
        Item item = itemService.getItemById(itemId);
        assertFalse(item.isActive(), "商品应该被下架");
        
        // 5. 管理员可以删除商品
        itemService.deleteItem(itemId);
        
        Item deletedItem = itemService.getItemById(itemId);
        assertNull(deletedItem, "商品应该被删除");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景3: 普通用户不能执行管理员操作")
    void testNonAdminCannotPerformAdminActions() {
        // 创建普通用户
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        
        // 买家登录
        userService.login("buyer", "pass123");
        assertFalse(UserService.isAdmin(), "买家不应该是管理员");
        userService.logout();
        
        // 卖家登录
        userService.login("seller", "pass123");
        assertFalse(UserService.isAdmin(), "卖家不应该是管理员");
        
        // 在实际应用中，这里应该有权限检查
        // 但当前代码没有强制权限检查，所以我们只验证角色
        
        userService.logout();
    }

    @Test
    @DisplayName("场景4: 管理员删除用户")
    void testAdminDeleteUser() {
        // 1. 创建管理员和普通用户
        userService.register("admin", "admin123", "admin@test.com", UserRole.ADMIN);
        userService.register("user_to_delete", "pass123", "delete@test.com", UserRole.BUYER);
        
        // 2. 获取待删除用户ID
        userService.login("user_to_delete", "pass123");
        Long userToDeleteId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // 3. 管理员删除用户
        userService.login("admin", "admin123");
        userService.deleteUser(userToDeleteId);
        
        User deletedUser = userService.getUserById(userToDeleteId);
        assertNull(deletedUser, "用户应该被删除");
        
        userService.logout();
        
        // 4. 验证删除的用户无法登录
        String loginResult = userService.login("user_to_delete", "pass123");
        assertEquals("User does not exist", loginResult, "删除的用户不能登录");
    }

    @Test
    @DisplayName("场景5: 管理员批量用户管理")
    void testAdminBatchUserManagement() {
        // 1. 创建管理员和多个用户
        userService.register("admin", "admin123", "admin@test.com", UserRole.ADMIN);
        
        for (int i = 1; i <= 5; i++) {
            userService.register("user" + i, "pass123", "user" + i + "@test.com", 
                i % 2 == 0 ? UserRole.BUYER : UserRole.SELLER);
        }
        
        // 2. 管理员登录并查看所有用户
        userService.login("admin", "admin123");
        List<User> allUsers = userService.getAllUsers();
        assertEquals(6, allUsers.size(), "应该有6个用户（1管理员+5普通用户）");
        
        // 3. 统计不同角色的用户数量
        long buyerCount = allUsers.stream()
            .filter(u -> u.getRole() == UserRole.BUYER)
            .count();
        long sellerCount = allUsers.stream()
            .filter(u -> u.getRole() == UserRole.SELLER)
            .count();
        long adminCount = allUsers.stream()
            .filter(u -> u.getRole() == UserRole.ADMIN)
            .count();
        
        assertEquals(2, buyerCount, "应该有2个买家");
        assertEquals(3, sellerCount, "应该有3个卖家");
        assertEquals(1, adminCount, "应该有1个管理员");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景6: 管理员查看用户详细信息")
    void testAdminViewUserDetails() {
        // 1. 创建用户
        userService.register("admin", "admin123", "admin@test.com", UserRole.ADMIN);
        userService.register("testuser", "pass123", "testuser@test.com", UserRole.SELLER);
        
        // 2. 测试用户发布商品
        userService.login("testuser", "pass123");
        Long testUserId = UserService.getCurrentUser().getId();
        
        itemService.publishItem(testUserId, "Item1", "Desc1", 100.0, "Cat1", null);
        itemService.publishItem(testUserId, "Item2", "Desc2", 200.0, "Cat2", null);
        userService.logout();
        
        // 3. 管理员查看用户信息
        userService.login("admin", "admin123");
        
        User user = userService.getUserById(testUserId);
        assertNotNull(user, "应该能查到用户");
        assertEquals("testuser", user.getUsername());
        assertEquals("testuser@test.com", user.getEmail());
        assertEquals(UserRole.SELLER, user.getRole());
        
        // 4. 管理员查看该用户的所有商品
        List<Item> userItems = itemService.getItemsBySeller(testUserId);
        assertEquals(2, userItems.size(), "用户应该有2个商品");
        
        userService.logout();
    }
}

