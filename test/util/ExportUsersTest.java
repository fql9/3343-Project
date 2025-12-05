package util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import config.DatabaseConfig;
import static org.junit.jupiter.api.Assertions.*;

class ExportUsersTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:test_export_users.db";
    private static final String TEST_OUTPUT_FILE = "users_export.txt";
    private String originalDbUrl;

    @BeforeEach
    void setUp() {
        // 保存原始的数据库URL
        try {
            java.lang.reflect.Field field = DatabaseConfig.class.getDeclaredField("DB_URL");
            field.setAccessible(true);
            originalDbUrl = (String) field.get(null);
            field.set(null, TEST_DB_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 创建测试数据库
        initTestDatabase();

        // 清理可能存在的输出文件
        cleanUpOutputFile();
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

        // 删除测试数据库文件
        File testDbFile = new File("test_export_users.db");
        if (testDbFile.exists()) {
            testDbFile.delete();
        }

        // 清理输出文件
        cleanUpOutputFile();
    }

    private void initTestDatabase() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // 创建users表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL,
                    email TEXT NOT NULL,
                    role TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1
                );
            """);

            // 插入测试数据
            String insertSql = "INSERT INTO users (username, password, email, role, active) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, "testuser1");
                ps.setString(2, "password1");
                ps.setString(3, "test1@example.com");
                ps.setString(4, "BUYER");
                ps.setInt(5, 1);
                ps.executeUpdate();

                ps.setString(1, "testuser2");
                ps.setString(2, "password2");
                ps.setString(3, "test2@example.com");
                ps.setString(4, "SELLER");
                ps.setInt(5, 1);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanUpOutputFile() {
        File outputFile = new File(TEST_OUTPUT_FILE);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    @Test
    void testExportUsers() {
        // 执行导出操作
        ExportUsers.main(new String[0]);

        // 验证文件是否创建
        File outputFile = new File(TEST_OUTPUT_FILE);
        assertTrue(outputFile.exists(), "导出文件未创建");
        assertTrue(outputFile.length() > 0, "导出文件为空");

        // 验证文件内容
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_OUTPUT_FILE)));
            
            // 检查标题行
            assertTrue(content.contains("ID | USERNAME        | ROLE      | EMAIL                     | ACTIVE   "), "标题行格式不正确");
            
            // 检查分隔线
            assertTrue(content.contains("------------------------------------------------------------------------------"), "分隔线缺失");
            
            // 检查测试数据
            assertTrue(content.contains("testuser1"), "测试用户1数据缺失");
            assertTrue(content.contains("testuser2"), "测试用户2数据缺失");
            assertTrue(content.contains("BUYER"), "BUYER角色数据缺失");
            assertTrue(content.contains("SELLER"), "SELLER角色数据缺失");
            assertTrue(content.contains("Yes"), "活跃状态数据缺失");
            
        } catch (IOException e) {
            fail("读取导出文件失败: " + e.getMessage());
        }
    }
}
