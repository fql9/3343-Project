package config;

import model.UserRole;
import service.*;
import java.sql.*;

/**
 * Demo data initializer - automatically populates database with sample data if empty
 */
public class DemoDataInitializer {
    
    /**
     * Check if database needs demo data and initialize if empty
     */
    public static void initializeIfNeeded() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if users exist
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Database already has " + rs.getInt(1) + " users, skipping demo data initialization.");
                return;
            }
            
            System.out.println("=".repeat(80));
            System.out.println("Initializing demo data...");
            System.out.println("Database path: " + DatabaseConfig.getDatabaseUrl());
            System.out.println("=".repeat(80));
            
            initializeUsers();
            System.out.println("  - Users created");
            
            initializeItems();
            System.out.println("  - Items created");
            
            initializeMessages();
            System.out.println("  - Messages created");
            
            initializeOrders();
            System.out.println("  - Orders created");
            
            initializeFavorites();
            System.out.println("  - Favorites created");
            
            System.out.println("=".repeat(80));
            System.out.println("Demo data initialized successfully!");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("=".repeat(80));
            System.err.println("ERROR: Failed to initialize demo data!");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Database URL: " + DatabaseConfig.getDatabaseUrl());
            e.printStackTrace();
            System.err.println("=".repeat(80));
            // Re-throw to make the error visible
            throw new RuntimeException("Failed to initialize demo data", e);
        }
    }
    
    private static void initializeUsers() {
        UserService userService = new UserService();
        
        // Create admin user
        registerWithRetry(userService, "admin", "password123", "admin@secondhand.com", UserRole.ADMIN);
        
        // Create seller accounts
        String[][] sellers = {
            {"alice", "alice@email.com"},
            {"charlie", "charlie@email.com"},
            {"evan", "evan@email.com"},
            {"grace", "grace@email.com"},
            {"iris", "iris@email.com"},
            {"kevin", "kevin@email.com"},
            {"mike", "mike@email.com"}
        };
        
        for (String[] seller : sellers) {
            registerWithRetry(userService, seller[0], "password123", seller[1], UserRole.SELLER);
            try { Thread.sleep(300); } catch (InterruptedException e) { /* ignore */ }
        }
        
        // Create buyer accounts
        String[][] buyers = {
            {"bob", "bob@email.com"},
            {"diana", "diana@email.com"},
            {"frank", "frank@email.com"},
            {"henry", "henry@email.com"},
            {"julia", "julia@email.com"},
            {"laura", "laura@email.com"},
            {"nathan", "nathan@email.com"}
        };
        
        for (String[] buyer : buyers) {
            registerWithRetry(userService, buyer[0], "password123", buyer[1], UserRole.BUYER);
            try { Thread.sleep(300); } catch (InterruptedException e) { /* ignore */ }
        }
    }
    
    private static void registerWithRetry(UserService service, String username, String password, 
                                         String email, UserRole role) {
        for (int i = 0; i < 10; i++) {
            try {
                service.register(username, password, email, role);
                return;
            } catch (Exception e) {
                if (i == 9) {
                    System.err.println("Failed to register user " + username + ": " + e.getMessage());
                    throw new RuntimeException("Failed after 10 retries", e);
                } else {
                    try { Thread.sleep(200); } catch (InterruptedException ie) {}
                }
            }
        }
    }
    
    private static void initializeItems() throws SQLException {
        ItemService itemService = new ItemService();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE role = 'SELLER'");
            java.util.List<Long> sellerIds = new java.util.ArrayList<>();
            while (rs.next()) {
                sellerIds.add(rs.getLong("id"));
            }
            
            String[][] itemData = {
                {"MacBook Pro 13-inch", "2022 MacBook Pro, 512GB SSD, 8GB RAM, excellent condition", "4500", "Electronics", "item_images/macbook.jpg"},
                {"iPhone 14", "iPhone 14, 64GB, Black, 95% new, includes case", "4000", "Electronics", "item_images/iphone.jpg"},
                {"Wireless Headphones", "Sony WH-1000XM4 noise-cancelling headphones", "1200", "Electronics", "item_images/headphones.jpg"},
                {"4K Monitor", "LG 27-inch 4K monitor, perfect for gaming", "2800", "Electronics", "item_images/monitor.jpg"},
                {"DSLR Camera", "Canon EOS R6 mirrorless camera, includes lens kit", "8500", "Electronics", "item_images/camera.jpg"},
                {"Gaming Laptop", "ROG Gaming Laptop, RTX 3060, 16GB RAM", "6500", "Electronics", "item_images/laptop.jpg"},
                {"iPad Pro 11-inch", "2021 iPad Pro, 256GB, includes Apple Pencil", "3200", "Electronics", "item_images/ipad.jpg"},
                {"Samsung Galaxy Tab", "Samsung Galaxy Tab S8, with keyboard case", "2500", "Electronics", "item_images/tablet.jpg"},
                {"Mechanical Keyboard", "Corsair K70 RGB, Cherry MX Brown switches", "800", "Electronics", "item_images/keyboard.jpg"},
                {"Wireless Mouse", "Logitech MX Master 3, ergonomic design", "450", "Electronics", "item_images/mouse.jpg"},
                {"Nintendo Switch", "Nintendo Switch with 5 game cartridges", "1800", "Gaming", "item_images/switch.jpg"},
                {"PlayStation 5", "PS5 Digital Edition, includes 2 controllers", "4200", "Gaming", "item_images/ps5.jpg"},
                {"Xbox Series X", "Xbox Series X console, like new", "3800", "Gaming", "item_images/xbox.jpg"},
                {"Office Desk", "Modern solid wood desk, includes ergonomic chair", "800", "Furniture", "item_images/desk.jpg"},
                {"Bookshelf", "5-tier wooden bookshelf, holds 100+ books", "350", "Furniture", "item_images/bookshelf.jpg"},
                {"Office Chair", "Herman Miller Aeron chair, perfect condition", "1500", "Furniture", "item_images/chair.jpg"},
                {"Programming Books", "10 programming books: Java, Python, JavaScript", "200", "Books", "item_images/books.jpg"},
                {"Design Books", "Complete UX/UI design book set", "150", "Books", "item_images/design.jpg"},
                {"Road Bike", "Trek road bike, 21-speed, carbon frame", "2200", "Sports", "item_images/bike.jpg"},
                {"Gym Equipment", "Home gym set: dumbbells, resistance bands, yoga mat", "600", "Sports", "item_images/gym.jpg"}
            };
            
            for (int j = 0; j < itemData.length && !sellerIds.isEmpty(); j++) {
                String[] data = itemData[j];
                Long sellerId = sellerIds.get(j % sellerIds.size());
                itemService.publishItem(sellerId, data[0], data[1], Double.parseDouble(data[2]), 
                                      data[3], data[4].isEmpty() ? null : data[4]);
            }
        }
    }
    
    private static void initializeMessages() throws SQLException {
        MessageService messageService = new MessageService();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT id FROM users LIMIT 8");
            java.util.List<Long> userIds = new java.util.ArrayList<>();
            while (rs.next()) {
                userIds.add(rs.getLong("id"));
            }
            
            if (userIds.size() >= 8) {
                messageService.sendMessage(userIds.get(1), userIds.get(2), "Hi! Is the MacBook Pro still available?");
                messageService.sendMessage(userIds.get(2), userIds.get(1), "Yes, it's still available!");
                messageService.sendMessage(userIds.get(3), userIds.get(4), "Is the iPhone 14 still for sale?");
                messageService.sendMessage(userIds.get(4), userIds.get(3), "Yes! Just checked at Apple Store.");
                messageService.sendMessage(userIds.get(5), userIds.get(6), "Interested in the gaming laptop!");
                messageService.sendMessage(userIds.get(6), userIds.get(5), "Great! It runs all games smoothly.");
            }
        }
    }
    
    private static void initializeOrders() throws SQLException {
        OrderService orderService = new OrderService();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE role = 'BUYER' LIMIT 4");
            java.util.List<Long> buyerIds = new java.util.ArrayList<>();
            while (rs.next()) {
                buyerIds.add(rs.getLong("id"));
            }
            
            rs = stmt.executeQuery("SELECT id FROM items LIMIT 4");
            java.util.List<Long> itemIds = new java.util.ArrayList<>();
            while (rs.next()) {
                itemIds.add(rs.getLong("id"));
            }
            
            String[] addresses = {
                "123 Main Street, New York, NY 10001",
                "456 Oak Avenue, Los Angeles, CA 90001",
                "789 Pine Road, Chicago, IL 60601",
                "321 Elm Street, Houston, TX 77001"
            };
            
            if (!buyerIds.isEmpty() && !itemIds.isEmpty()) {
                for (int i = 0; i < Math.min(4, Math.min(itemIds.size(), buyerIds.size())); i++) {
                    orderService.createOrder(buyerIds.get(i), itemIds.get(i), addresses[i]);
                }
            }
        }
    }
    
    private static void initializeFavorites() throws SQLException {
        FavoriteService favoriteService = new FavoriteService();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet userRs = stmt.executeQuery("SELECT id FROM users WHERE role = 'BUYER'");
            java.util.List<Long> userIds = new java.util.ArrayList<>();
            while (userRs.next()) {
                userIds.add(userRs.getLong("id"));
            }
            
            ResultSet itemRs = stmt.executeQuery("SELECT id FROM items LIMIT 10");
            java.util.List<Long> itemIds = new java.util.ArrayList<>();
            while (itemRs.next()) {
                itemIds.add(itemRs.getLong("id"));
            }
            
            // Each buyer favorites 3-4 items
            for (int i = 0; i < userIds.size() && i < 5; i++) {
                Long userId = userIds.get(i);
                for (int j = 0; j < 3 && j < itemIds.size(); j++) {
                    int itemIndex = (i * 2 + j) % itemIds.size();
                    favoriteService.addFavorite(userId, itemIds.get(itemIndex));
                }
            }
        }
    }
}
