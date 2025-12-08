package config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_NAME = "secondhand.db";
    private static final String TEST_DB_NAME = "test_secondhand.db";
    private static String DB_URL;
    private static boolean isTestMode = false;
    
    static {
        // Initialize database path at class load time
        DB_URL = "jdbc:sqlite:" + getAppDataPath(DB_NAME);
    }
    
    /**
     * Get the application data directory path for storing database.
     * Always uses user home directory for consistency between portable and installed versions.
     */
    private static String getAppDataPath(String filename) {
        try {
            // Always use user home directory for database
            // This ensures portable and installed versions share the same database
            String userHome = System.getProperty("user.home");
            File userDataDir = new File(userHome, ".secondhand-trading");
            if (!userDataDir.exists()) {
                userDataDir.mkdirs();
            }
            String dbPath = new File(userDataDir, filename).getAbsolutePath();
            System.out.println("Database path: " + dbPath);
            return dbPath;
            
        } catch (Exception e) {
            // Ultimate fallback to current directory
            System.err.println("Warning: Could not determine app directory, using current directory");
            return filename;
        }
    }
    
    /**
     * 设置为测试模式，使用独立的测试数据库
     * 这个方法会同步更新 isTestMode 标志和数据库 URL
     */
    public static synchronized void setTestMode(boolean testMode) {
        isTestMode = testMode;
        DB_URL = "jdbc:sqlite:" + getAppDataPath(testMode ? TEST_DB_NAME : DB_NAME);
        System.out.println("DatabaseConfig: testMode=" + testMode + ", DB_URL=" + DB_URL);
    }
    
    /**
     * 检查是否在测试模式
     */
    public static boolean isTestMode() {
        return isTestMode;
    }
    
    /**
     * 获取当前数据库URL
     */
    public static String getDatabaseUrl() {
        return DB_URL;
    }
    
    /**
     * 验证数据库状态一致性
     * 确保 isTestMode 和 DB_URL 匹配
     */
    private static void validateDatabaseState() {
        boolean urlIsTest = DB_URL.contains("test_");
        if (isTestMode != urlIsTest) {
            throw new IllegalStateException(
                String.format("❌ 数据库状态不一致! isTestMode=%s, DB_URL=%s", isTestMode, DB_URL)
            );
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
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    phone TEXT,
                    role TEXT DEFAULT 'BUYER',
                    active INTEGER DEFAULT 1,
                    avatar_url TEXT,
                    bio TEXT,
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
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    created_time TEXT,
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
                    read INTEGER DEFAULT 0,
                    FOREIGN KEY(from_user_id) REFERENCES users(id),
                    FOREIGN KEY(to_user_id) REFERENCES users(id)
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
                    title TEXT,
                    content TEXT,
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
        // 验证数据库状态一致性
        validateDatabaseState();
        
        // 防御:只允许 SQLite URL,避免意外的驱动或无效协议悄悄成功
        if (DB_URL == null || !DB_URL.startsWith("jdbc:sqlite:")) {
            throw new RuntimeException("Invalid database URL: " + DB_URL);
        }
        try {
            // Configure SQLite for better concurrency
            String url = DB_URL + "?journal_mode=WAL&busy_timeout=10000";
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect database.", e);
        }
    }

    /**
     * @deprecated 不要直接设置 URL，使用 setTestMode() 代替
     * 直接修改 URL 会导致状态不一致
     */
    @Deprecated
    public static synchronized void setDbUrlForTest(String url) {
        System.err.println("⚠️ WARNING: setDbUrlForTest is deprecated, use setTestMode() instead");
        DB_URL = url;
    }

    /**
     * @deprecated 不要直接重置 URL，使用 setTestMode(false) 代替
     * 直接修改 URL 会导致状态不一致
     */
    @Deprecated
    public static synchronized void resetDbUrl() {
        System.err.println("⚠️ WARNING: resetDbUrl is deprecated, use setTestMode(false) instead");
        DB_URL = "jdbc:sqlite:" + getAppDataPath(DB_NAME);
    }
}
