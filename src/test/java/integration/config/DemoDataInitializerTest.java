package integration.config;

import config.DatabaseConfig;
import config.DemoDataInitializer;
import integration.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DemoDataInitializerTest extends IntegrationTestBase {

    @BeforeEach
    void setUp() {
        // Clean all data before each test
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM favorites");
            stmt.executeUpdate("DELETE FROM messages");
            stmt.executeUpdate("DELETE FROM orders");
            stmt.executeUpdate("DELETE FROM reviews");
            stmt.executeUpdate("DELETE FROM notifications");
            stmt.executeUpdate("DELETE FROM items");
            stmt.executeUpdate("DELETE FROM users");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up after tests
        setUp();
    }

    @Test
    void testInitializeIfNeeded_EmptyDatabase() {
        // Act
        DemoDataInitializer.initializeIfNeeded();

        // Assert - Check users created
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            assertTrue(rs.next());
            int userCount = rs.getInt(1);
            assertEquals(15, userCount, "Should create 15 users (1 admin + 7 sellers + 7 buyers)");
            
            // Check admin exists
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin' AND role = 'ADMIN'");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "Should have 1 admin user");
            
            // Check items created
            rs = stmt.executeQuery("SELECT COUNT(*) FROM items");
            assertTrue(rs.next());
            int itemCount = rs.getInt(1);
            assertTrue(itemCount > 0, "Should create items, got: " + itemCount);
            
            // Check messages created  
            rs = stmt.executeQuery("SELECT COUNT(*) FROM messages");
            assertTrue(rs.next());
            int messageCount = rs.getInt(1);
            assertTrue(messageCount > 0, "Should create messages, got: " + messageCount);
            
            // Check orders created
            rs = stmt.executeQuery("SELECT COUNT(*) FROM orders");
            assertTrue(rs.next());
            int orderCount = rs.getInt(1);
            assertTrue(orderCount > 0, "Should create orders, got: " + orderCount);
            
            // Check favorites created
            rs = stmt.executeQuery("SELECT COUNT(*) FROM favorites");
            assertTrue(rs.next());
            int favoriteCount = rs.getInt(1);
            assertTrue(favoriteCount > 0, "Should create favorites, got: " + favoriteCount);
            
        } catch (Exception e) {
            fail("Failed to verify demo data: " + e.getMessage());
        }
    }

    @Test
    void testInitializeIfNeeded_DatabaseAlreadyHasData() {
        // Arrange - Insert one user
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "INSERT INTO users (username, password_hash, email, role) " +
                "VALUES ('existing_user', 'hash', 'existing@test.com', 'BUYER')"
            );
        } catch (Exception e) {
            fail("Failed to setup test data: " + e.getMessage());
        }

        // Act
        DemoDataInitializer.initializeIfNeeded();

        // Assert - Should not add more users
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "Should not initialize when data already exists");
        } catch (Exception e) {
            fail("Failed to verify: " + e.getMessage());
        }
    }

    @Test
    void testDemoDataIntegrity() {
        // Act
        DemoDataInitializer.initializeIfNeeded();

        // Assert - Verify data integrity
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // All items should have valid seller_id
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM items WHERE seller_id NOT IN (SELECT id FROM users)"
            );
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "All items should have valid seller references");
            
            // All orders should have valid buyer and seller
            rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM orders WHERE buyer_id NOT IN (SELECT id FROM users) " +
                "OR seller_id NOT IN (SELECT id FROM users)"
            );
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "All orders should have valid user references");
            
            // All messages should have valid from and to users
            rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM messages WHERE from_user_id NOT IN (SELECT id FROM users) " +
                "OR to_user_id NOT IN (SELECT id FROM users)"
            );
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "All messages should have valid user references");
            
        } catch (Exception e) {
            fail("Data integrity check failed: " + e.getMessage());
        }
    }
}
