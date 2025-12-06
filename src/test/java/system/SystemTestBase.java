package system;

import config.DatabaseConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 系统测试基类
 * System Test - 验证整个系统的端到端功能
 * 
 * Modified Top-Down Test Strategy:
 * Level 1: System Tests (端到端业务场景)
 * Level 2: Service Integration Tests (服务层集成测试)
 * Level 3: DAO Integration Tests (数据访问层集成测试)
 * Level 4: Unit Tests (单元测试)
 */
public abstract class SystemTestBase {

    @BeforeAll
    static void setupTestDatabase() {
        // 启用测试模式，使用独立的测试数据库
        DatabaseConfig.setTestMode(true);
        
        // 初始化测试数据库
        DatabaseConfig.initDatabase();
        
        System.out.println("✓ 系统测试数据库已初始化: " + DatabaseConfig.getDatabaseUrl());
    }

    @AfterEach
    void cleanupAfterEachTest() {
        // 每个测试后清理数据，避免测试间相互影响
        if (DatabaseConfig.isTestMode()) {
            clearAllTables();
        }
    }

    @AfterAll
    static void tearDownTestDatabase() {
        // 测试完成后删除测试数据库文件
        File testDb = new File("test_secondhand.db");
        if (testDb.exists()) {
            boolean deleted = testDb.delete();
            if (deleted) {
                System.out.println("✓ 系统测试数据库已清理");
            }
        }
        
        DatabaseConfig.setTestMode(false);
    }

    /**
     * 清空所有测试表数据
     */
    protected void clearAllTables() {
        if (!DatabaseConfig.isTestMode()) {
            throw new IllegalStateException("❌ 安全错误: 不允许在非测试模式下清空数据!");
        }
        
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
}

