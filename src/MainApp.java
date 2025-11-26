import config.DatabaseConfig;
import view.controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application entry class
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseConfig.initDatabase();
            
            // Set window properties
            primaryStage.setTitle("Second-hand Trading Platform");
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            
            // Show login view
            LoginController loginController = new LoginController(primaryStage);
            loginController.showLoginView();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Application startup failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
