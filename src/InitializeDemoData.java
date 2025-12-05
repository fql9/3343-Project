import config.DatabaseConfig;
import config.DemoDataInitializer;

/**
 * Standalone class to initialize demo data
 */
public class InitializeDemoData {
    public static void main(String[] args) {
        try {
            System.out.println("=".repeat(80));
            System.out.println("Starting Demo Data Initialization...");
            System.out.println("=".repeat(80));
            
            // Initialize database structure
            DatabaseConfig.initDatabase();
            System.out.println("✓ Database structure initialized");
            
            // Wait for database to be fully released
            Thread.sleep(200);
            
            // Initialize demo data
            System.out.println("=".repeat(80));
            System.out.println("Initializing demo data...");
            System.out.println("=".repeat(80));
            DemoDataInitializer.initializeIfNeeded();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("✓ Demo Data Initialization Complete!");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
