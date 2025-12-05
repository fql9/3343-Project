package config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:test_secondhand.db";
    private String originalDbUrl;

    @BeforeEach
    void setUp() {
        // 获取原始的数据库URL（通过反射）
        try {
            java.lang.reflect.Field field = DatabaseConfig.class.getDeclaredField("DB_URL");
            field.setAccessible(true);
            originalDbUrl = (String) field.get(null);
            
            // 修改为测试数据库URL
            field.set(null, TEST_DB_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 确保测试数据库文件不存在
        File testDbFile = new File("test_secondhand.db");
        if (testDbFile.exists()) {
            testDbFile.delete();
        }
    }

    @AfterEach
    void tearDown() {
        // 恢复原始的数据库URL
        try {
            java.lang.reflect.Field field = DatabaseConfig.class.getDeclaredField("DB_URL");
            field.setAccessible(true);
            field.set(null, originalDbUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 清理测试数据库文件
        File testDbFile = new File("test_secondhand.db");
        if (testDbFile.exists()) {
            testDbFile.delete();
        }
    }

    @Test
    void testGetConnection() {
        // 测试获取数据库连接
        Connection connection = DatabaseConfig.getConnection();
        assertNotNull(connection);
        
        try {
            // 验证连接是否有效
            assertFalse(connection.isClosed());
            connection.close();
        } catch (Exception e) {
            fail("连接测试失败: " + e.getMessage());
        }
    }

    @Test
    void testInitDatabase() {
        // 测试初始化数据库
        DatabaseConfig.initDatabase();
        
        // 验证数据库表是否创建成功
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 检查users表是否存在
            ResultSet rsUsers = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'");
            assertTrue(rsUsers.next(), "users表未创建");
            
            // 检查items表是否存在
            ResultSet rsItems = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='items'");
            assertTrue(rsItems.next(), "items表未创建");
            
            // 检查favorites表是否存在
            ResultSet rsFavorites = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='favorites'");
            assertTrue(rsFavorites.next(), "favorites表未创建");
            
            // 检查messages表是否存在
            ResultSet rsMessages = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='messages'");
            assertTrue(rsMessages.next(), "messages表未创建");
            
        } catch (Exception e) {
            fail("数据库初始化测试失败: " + e.getMessage());
        }
    }

    @Test
    void testConnectionException() {
        // 修改为无效的数据库URL（使用不存在的协议）
        try {
            java.lang.reflect.Field field = DatabaseConfig.class.getDeclaredField("DB_URL");
            field.setAccessible(true);
            field.set(null, "jdbc:nonexistent:invalid.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 验证获取连接时是否抛出异常
        assertThrows(RuntimeException.class, () -> {
            DatabaseConfig.getConnection();
        });
    }
}
