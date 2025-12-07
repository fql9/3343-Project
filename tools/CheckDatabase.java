import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:secondhand.db");
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM users");
            rs.next();
            System.out.println("Users: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM items");
            rs.next();
            System.out.println("Items: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM orders");
            rs.next();
            System.out.println("Orders: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM messages");
            rs.next();
            System.out.println("Messages: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM favorites");
            rs.next();
            System.out.println("Favorites: " + rs.getInt(1));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
