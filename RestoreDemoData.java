import config.DatabaseConfig;
import config.DemoDataInitializer;

public class RestoreDemoData {
    public static void main(String[] args) {
        try {
            System.out.println("=".repeat(80));
            System.out.println("恢复演示数据...");
            System.out.println("=".repeat(80));
            
            // 确保不在测试模式
            DatabaseConfig.setTestMode(false);
            
            // 初始化数据库结构
            DatabaseConfig.initDatabase();
            System.out.println("✓ 数据库结构已初始化");
            
            // 初始化演示数据
            DemoDataInitializer.initializeIfNeeded();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("✓ 演示数据恢复完成!");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
