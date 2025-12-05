package integration;

import config.DatabaseConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 集成测试基类
 * 所有集成测试都应继承此类，以确保使用独立的测试数据库
 */
public abstract class IntegrationTestBase {

    @BeforeAll
    static void setupTestDatabase() {
        // 启用测试模式，使用独立的测试数据库
        DatabaseConfig.setTestMode(true);
        
        // 初始化测试数据库
        DatabaseConfig.initDatabase();
        
        System.out.println("✓ 测试数据库已初始化: " + DatabaseConfig.getDatabaseUrl());
    }

    @AfterEach
    void cleanupAfterEachTest() {
        // 每个测试后清理数据，避免测试间相互影响
        clearAllTables();
    }

    @AfterAll
    static void tearDownTestDatabase() {
        // 测试完成后删除测试数据库文件
        DatabaseConfig.setTestMode(false);
        
        File testDb = new File("test_secondhand.db");
        if (testDb.exists()) {
            boolean deleted = testDb.delete();
            if (deleted) {
                System.out.println("✓ 测试数据库已清理");
            }
        }
    }

    /**
     * 清空所有测试表数据
     */
    protected void clearAllTables() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 禁用外键约束
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            // 清空所有表
            stmt.executeUpdate("DELETE FROM reviews");
            stmt.executeUpdate("DELETE FROM notifications");
            stmt.executeUpdate("DELETE FROM orders");
            stmt.executeUpdate("DELETE FROM messages");
            stmt.executeUpdate("DELETE FROM favorites");
            stmt.executeUpdate("DELETE FROM items");
            stmt.executeUpdate("DELETE FROM users");
            
            // 重置自增ID
            stmt.executeUpdate("DELETE FROM sqlite_sequence");
            
            // 重新启用外键约束
            stmt.execute("PRAGMA foreign_keys = ON");
            
        } catch (Exception e) {
            System.err.println("清理测试数据失败: " + e.getMessage());
        }
    }

    /**
     * 清空指定表的数据
     */
    protected void clearTable(String tableName) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + tableName);
        } catch (Exception e) {
            System.err.println("清理表 " + tableName + " 失败: " + e.getMessage());
        }
    }
}
