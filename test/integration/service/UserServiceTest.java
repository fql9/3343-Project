package integration.service;

import integration.IntegrationTestBase;
import service.UserService;
import config.DatabaseConfig;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class UserServiceTest extends IntegrationTestBase {

    private UserService userService;
    private static final String TEST_USER = "test_login_user";
    private static final String TEST_PASS = "password123";
    private static final String TEST_EMAIL = "test_login@example.com";

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @AfterEach
    void tearDown() {
        userService.logout();
    }



    @Test
    void testLoginSuccess() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);

        // Act
        String result = userService.login(TEST_USER, TEST_PASS);

        // Assert
        assertNull(result, "Login should be successful (return null)");
        assertTrue(UserService.isLoggedIn(), "User should be logged in");
        assertEquals(TEST_USER, UserService.getCurrentUser().getUsername(), "Current user should match");
    }

    @Test
    void testLoginUserNotFound() {
        // Act
        String result = userService.login("non_existent_user", "any_password");

        // Assert
        assertEquals("User does not exist", result);
        assertFalse(UserService.isLoggedIn());
    }

    @Test
    void testLoginIncorrectPassword() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);

        // Act
        String result = userService.login(TEST_USER, "wrong_password");

        // Assert
        assertEquals("Incorrect password", result);
        assertFalse(UserService.isLoggedIn());
    }

    @Test
    void testLoginBannedUser() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        
        // Login to get the ID (to ban the user)
        userService.login(TEST_USER, TEST_PASS);
        User user = UserService.getCurrentUser();
        userService.logout();
        
        // Ban the user
        userService.setUserActive(user.getId(), false);

        // Act
        String result = userService.login(TEST_USER, TEST_PASS);

        // Assert
        assertEquals("Account has been banned", result);
        assertFalse(UserService.isLoggedIn());
    }

    @Test
    void testRegisterSuccess() {
        // Act
        String result = userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);

        // Assert
        assertNull(result, "Registration should be successful");
    }

    @Test
    void testRegisterDuplicateUsername() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);

        // Act
        String result = userService.register(TEST_USER, "newpass", "new@example.com", UserRole.BUYER);

        // Assert
        assertEquals("Username already exists", result);
    }

    @Test
    void testRegisterInvalidUsername() {
        // Act
        String result = userService.register("ab", TEST_PASS, TEST_EMAIL, UserRole.BUYER); // Too short

        // Assert
        assertNotNull(result);
        // The exact message depends on ValidationUtils, but it should not be null
    }

    @Test
    void testRegisterInvalidPassword() {
        // Act
        String result = userService.register(TEST_USER, "123", TEST_EMAIL, UserRole.BUYER); // Too short

        // Assert
        assertNotNull(result);
    }
    
    @Test
    void testRegisterInvalidEmail() {
        // Act
        String result = userService.register(TEST_USER, TEST_PASS, "invalid-email", UserRole.BUYER);

        // Assert
        assertEquals("Invalid email format", result);
    }
    
    @Test
    void testLogout() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);
        assertTrue(UserService.isLoggedIn());

        // Act
        userService.logout();

        // Assert
        assertFalse(UserService.isLoggedIn());
        assertNull(UserService.getCurrentUser());
    }

    @Test
    void testIsAdmin_AdminUser() {
        // Arrange
        String registerResult = userService.register("admin_user", TEST_PASS, "admin@example.com", UserRole.ADMIN);
        assertNull(registerResult, "Admin registration should succeed, but got: " + registerResult);
        
        String loginResult = userService.login("admin_user", TEST_PASS);
        assertNull(loginResult, "Admin login should succeed, but got: " + loginResult);
        assertTrue(UserService.isLoggedIn(), "User should be logged in after login");

        // Act & Assert
        assertTrue(UserService.isAdmin(), "Logged in user should be admin");
        
        // Cleanup
        User admin = UserService.getCurrentUser();
        assertNotNull(admin, "Current user should not be null");
        userService.logout();
        userService.deleteUser(admin.getId());
    }


    @Test
    void testIsAdmin_NonAdminUser() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);

        // Act & Assert
        assertFalse(UserService.isAdmin());
    }

    @Test
    void testIsAdmin_NotLoggedIn() {
        // Act & Assert
        assertFalse(UserService.isAdmin());
    }

    @Test
    void testGetUserById() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);
        Long userId = UserService.getCurrentUser().getId();
        userService.logout();

        // Act
        User user = userService.getUserById(userId);

        // Assert
        assertNotNull(user);
        assertEquals(TEST_USER, user.getUsername());
        assertEquals(TEST_EMAIL, user.getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        // Act
        User user = userService.getUserById(99999L);

        // Assert
        assertNull(user);
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.register("test_user2", TEST_PASS, "test2@example.com", UserRole.SELLER);

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertNotNull(users);
        assertTrue(users.size() >= 2);
        
        // Cleanup
        try (java.sql.Connection conn = DatabaseConfig.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users WHERE username = 'test_user2'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSetUserActive_Activate() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);
        Long userId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // Ban first
        userService.setUserActive(userId, false);

        // Act
        userService.setUserActive(userId, true);

        // Assert
        User user = userService.getUserById(userId);
        assertTrue(user.isActive());
    }

    @Test
    void testSetUserActive_NonExistentUser() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> userService.setUserActive(99999L, false));
    }

    @Test
    void testDeleteUser() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);
        Long userId = UserService.getCurrentUser().getId();
        userService.logout();

        // Act
        userService.deleteUser(userId);

        // Assert
        User user = userService.getUserById(userId);
        assertNull(user);
    }

    @Test
    void testChangePassword_Success() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);

        // Act
        String result = userService.changePassword(TEST_PASS, "newPassword123");

        // Assert
        assertNull(result);
        
        // Verify new password works
        userService.logout();
        String loginResult = userService.login(TEST_USER, "newPassword123");
        assertNull(loginResult);
    }

    @Test
    void testChangePassword_NotLoggedIn() {
        // Act
        String result = userService.changePassword(TEST_PASS, "newPassword123");

        // Assert
        assertEquals("Not logged in", result);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);

        // Act
        String result = userService.changePassword("wrongPassword", "newPassword123");

        // Assert
        assertEquals("Incorrect old password", result);
    }

    @Test
    void testChangePassword_InvalidNewPassword() {
        // Arrange
        userService.register(TEST_USER, TEST_PASS, TEST_EMAIL, UserRole.BUYER);
        userService.login(TEST_USER, TEST_PASS);

        // Act
        String result = userService.changePassword(TEST_PASS, "123"); // Too short

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Password") || result.contains("password"));
    }

    @Test
    void testRegisterWithSellerRole() {
        // Act
        String result = userService.register("seller_user", TEST_PASS, "seller@example.com", UserRole.SELLER);

        // Assert
        assertNull(result);
        
        // Verify role
        userService.login("seller_user", TEST_PASS);
        assertEquals(UserRole.SELLER, UserService.getCurrentUser().getRole());
        
        // Cleanup
        Long userId = UserService.getCurrentUser().getId();
        userService.logout();
        userService.deleteUser(userId);
    }

}
