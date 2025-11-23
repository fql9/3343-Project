package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:secondhand.db";
    
    // 加载 SQLite JDBC 驱动
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
        }
    }

    // 初始化数据库（程序启动时执行）
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ----- users -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    email TEXT,
                    role TEXT NOT NULL,
                    active INTEGER NOT NULL
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
                    active INTEGER NOT NULL,
                    created_time TEXT NOT NULL,
                    FOREIGN KEY(seller_id) REFERENCES users(id)
                );
            """);

            // ----- favorites -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    created_time TEXT NOT NULL,
                    UNIQUE(user_id, item_id),
                    FOREIGN KEY(user_id) REFERENCES users(id),
                    FOREIGN KEY(item_id) REFERENCES items(id)
                );
            """);

            // ----- messages -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    from_user_id INTEGER NOT NULL,
                    to_user_id INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    created_time TEXT NOT NULL,
                    read INTEGER NOT NULL,
                    FOREIGN KEY(from_user_id) REFERENCES users(id),
                    FOREIGN KEY(to_user_id) REFERENCES users(id)
                );
            """);

            // ----- transactions -----
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id INTEGER NOT NULL,
                    buyer_id INTEGER NOT NULL,
                    seller_id INTEGER NOT NULL,
                    agreed_price REAL NOT NULL,
                    status TEXT NOT NULL,
                    delivery_method TEXT,
                    shipping_address TEXT,
                    tracking_number TEXT,
                    item_received INTEGER NOT NULL,
                    item_verified INTEGER NOT NULL,
                    funds_released INTEGER NOT NULL,
                    created_time TEXT NOT NULL,
                    updated_time TEXT NOT NULL,
                    FOREIGN KEY(item_id) REFERENCES items(id),
                    FOREIGN KEY(buyer_id) REFERENCES users(id),
                    FOREIGN KEY(seller_id) REFERENCES users(id)
                );
            """);

            System.out.println("Database initialized.");
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
