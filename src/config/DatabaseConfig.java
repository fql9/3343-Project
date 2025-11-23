package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:secondhand.db";

    // 初始化数据库（程序启动时执行）
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ...existing code...
            
            // ----- favorites -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    user_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    created_time TEXT,
                    PRIMARY KEY(user_id, item_id),
                    FOREIGN KEY(user_id) REFERENCES users(id),
                    FOREIGN KEY(item_id) REFERENCES items(id)
                );
            """);

            // ----- messages -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender_id INTEGER NOT NULL,
                    receiver_id INTEGER NOT NULL,
                    item_id INTEGER,
                    content TEXT NOT NULL,
                    created_time TEXT NOT NULL,
                    FOREIGN KEY(sender_id) REFERENCES users(id),
                    FOREIGN KEY(receiver_id) REFERENCES users(id),
                    FOREIGN KEY(item_id) REFERENCES items(id)
                );
            """);
            
            // 修复数据：将不合法的 'USER' 角色更新为 'BUYER'
            stmt.executeUpdate("UPDATE users SET role = 'BUYER' WHERE role = 'USER'");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 单例 Connection
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect database.", e);
        }
    }
}
