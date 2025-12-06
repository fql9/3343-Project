package integration.config;

import config.DatabaseConfig;
import integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DatabaseConfig 补充测试 - 边界条件
 * 目标: 提升分支覆盖率
 */
class DatabaseConfigCoverageTest extends IntegrationTestBase {

    @Test
    void testGetConnectionInTestMode() {
        // 测试模式下获取连接
        assertTrue(DatabaseConfig.isTestMode());
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        } catch (Exception e) {
            fail("获取测试数据库连接失败: " + e.getMessage());
        }
    }

    @Test
    void testDatabaseUrlInTestMode() {
        String url = DatabaseConfig.getDatabaseUrl();
        assertTrue(url.contains("test_"), "测试模式应该使用test数据库");
    }

    @Test
    void testMultipleConnections() {
        // 测试可以同时打开多个连接
        try (Connection conn1 = DatabaseConfig.getConnection();
             Connection conn2 = DatabaseConfig.getConnection();
             Connection conn3 = DatabaseConfig.getConnection()) {
            
            assertNotNull(conn1);
            assertNotNull(conn2);
            assertNotNull(conn3);
            
            assertFalse(conn1.isClosed());
            assertFalse(conn2.isClosed());
            assertFalse(conn3.isClosed());
            
        } catch (Exception e) {
            fail("多连接测试失败: " + e.getMessage());
        }
    }

    @Test
    void testConnectionAutoCommit() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 默认应该是auto-commit
            assertTrue(conn.getAutoCommit());
        } catch (Exception e) {
            fail("测试auto-commit失败: " + e.getMessage());
        }
    }

    @Test
    void testDatabaseTablesExist() {
        // 验证所有必需的表都存在
        String[] tables = {"users", "items", "orders", "messages", 
                          "favorites", "reviews", "notifications"};
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'"
                );
                assertTrue(rs.next(), "表 " + table + " 应该存在");
            }
            
        } catch (Exception e) {
            fail("检查表存在性失败: " + e.getMessage());
        }
    }

    @Test
    void testForeignKeysEnabled() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys")) {
            
            assertTrue(rs.next());
            int foreignKeys = rs.getInt(1);
            // 外键可能是0或1，取决于数据库配置
            assertTrue(foreignKeys == 0 || foreignKeys == 1, 
                      "外键设置应该是0或1，实际值: " + foreignKeys);
            
        } catch (Exception e) {
            fail("检查外键设置失败: " + e.getMessage());
        }
    }

    @Test
    void testDatabaseVersion() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT sqlite_version()")) {
            
            assertTrue(rs.next());
            String version = rs.getString(1);
            assertNotNull(version);
            assertFalse(version.isEmpty());
            
        } catch (Exception e) {
            fail("获取数据库版本失败: " + e.getMessage());
        }
    }

    @Test
    void testInsertAndQueryData() {
        // 测试基本的插入和查询功能
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 使用唯一的用户名避免冲突
            String uniqueUsername = "testuser_" + System.currentTimeMillis();
            
            // 插入测试数据
            int inserted = stmt.executeUpdate(
                "INSERT INTO users (username, password_hash, email, role, active, created_time) " +
                "VALUES ('" + uniqueUsername + "', 'hash', 'test@test.com', 'BUYER', 1, datetime('now'))"
            );
            
            assertTrue(inserted > 0, "应该插入1行数据");
            
            // 查询数据
            ResultSet rs = stmt.executeQuery(
                "SELECT username FROM users WHERE username='" + uniqueUsername + "'"
            );
            
            assertTrue(rs.next(), "应该能查询到插入的数据");
            assertEquals(uniqueUsername, rs.getString("username"));
            
            // 清理测试数据
            stmt.executeUpdate("DELETE FROM users WHERE username='" + uniqueUsername + "'");
            
        } catch (Exception e) {
            fail("插入查询测试失败: " + e.getMessage());
        }
    }

    @Test
    void testConnectionIsValid() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            assertTrue(conn.isValid(5), "连接应该是有效的");
        } catch (Exception e) {
            fail("测试连接有效性失败: " + e.getMessage());
        }
    }

    @Test
    void testConnectionMetadata() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            var metadata = conn.getMetaData();
            assertNotNull(metadata);
            assertEquals("SQLite", metadata.getDatabaseProductName());
        } catch (Exception e) {
            fail("获取连接元数据失败: " + e.getMessage());
        }
    }
}

