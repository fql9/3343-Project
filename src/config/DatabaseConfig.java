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

            // ----- users -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    phone TEXT,
                    role TEXT DEFAULT 'BUYER',
                    created_time TEXT
                );
            """);

            // ----- items -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    price REAL NOT NULL,
                    category TEXT,
                    image_url TEXT,
                    active INTEGER DEFAULT 1,
                    created_time TEXT,
                    FOREIGN KEY(seller_id) REFERENCES users(id)
                );
            """);
            
            // 检查 items 表是否有 image_url 列，如果没有则添加（用于旧数据库升级）
            try {
                stmt.execute("ALTER TABLE items ADD COLUMN image_url TEXT");
            } catch (Exception e) {
                // 列已存在，忽略错误
            }

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

            // ----- orders -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_no TEXT NOT NULL UNIQUE,
                    buyer_id INTEGER NOT NULL,
                    seller_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    status TEXT NOT NULL,
                    shipping_address TEXT,
                    created_time TEXT,
                    FOREIGN KEY(buyer_id) REFERENCES users(id),
                    FOREIGN KEY(seller_id) REFERENCES users(id),
                    FOREIGN KEY(item_id) REFERENCES items(id)
                );
            """);
            
            // ----- reviews -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reviews (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    reviewer_id INTEGER NOT NULL,
                    reviewee_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    rating INTEGER NOT NULL,
                    comment TEXT,
                    created_time TEXT,
                    FOREIGN KEY(order_id) REFERENCES orders(id),
                    FOREIGN KEY(reviewer_id) REFERENCES users(id),
                    FOREIGN KEY(reviewee_id) REFERENCES users(id),
                    FOREIGN KEY(item_id) REFERENCES items(id)
                );
            """);

            // ----- notifications -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    is_read INTEGER DEFAULT 0,
                    created_time TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                );
            """);
            
            // 修复数据：将不合法的 'USER' 角色更新为 'BUYER'
            stmt.executeUpdate("UPDATE users SET role = 'BUYER' WHERE role = 'USER'");

            // 检查 users 表是否有 avatar_url 和 bio 列
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN avatar_url TEXT");
            } catch (Exception e) {
                // 列已存在，忽略错误
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN bio TEXT");
            } catch (Exception e) {
                // 列已存在，忽略错误
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN created_time TEXT");
            } catch (Exception e) {
                // 列已存在，忽略错误
            }

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
