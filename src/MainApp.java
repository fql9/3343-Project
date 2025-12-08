import config.DatabaseConfig;
import config.DemoDataInitializer;
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application entry class
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting Second-Hand Trading Platform...");
            System.out.println("Working directory: " + System.getProperty("user.dir"));
            System.out.println("User home: " + System.getProperty("user.home"));
            
            // Initialize database structure
            System.out.println("Initializing database...");
            DatabaseConfig.initDatabase();
            
            // Initialize demo data if database is empty
            System.out.println("Checking demo data...");
            DemoDataInitializer.initializeIfNeeded();
            
            // Set window properties
            primaryStage.setTitle("Second-hand Trading Platform");
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            
            // Show login view
            LoginController loginController = new LoginController(primaryStage);
            loginController.showLoginView();
            
            System.out.println("Application started successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Application startup failed: " + e.getMessage());
            
            // Show error dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to initialize application");
            alert.setContentText("Error: " + e.getMessage() + "\n\nPlease check the console for details.");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
