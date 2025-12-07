package util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Image utility class for handling item images
 */
public class ImageUtils {
    
    private static final String IMAGE_DIR_NAME = "item_images";
    private static final String IMAGE_DIR;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    static {
        // Determine the image directory path
        IMAGE_DIR = getAppDataPath(IMAGE_DIR_NAME);
        
        // Create image directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(IMAGE_DIR));
            System.out.println("Image directory: " + IMAGE_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create image directory: " + e.getMessage());
        }
    }
    
    /**
     * Get the application data directory path.
     * Priority: 1) JAR directory, 2) User home directory
     */
    private static String getAppDataPath(String subdir) {
        try {
            // Try to get the directory where the JAR/class is located
            String jarPath = ImageUtils.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            
            // On Windows, remove leading slash from path like /C:/...
            if (jarPath.matches("^/[A-Za-z]:.*")) {
                jarPath = jarPath.substring(1);
            }
            
            File jarFile = new File(jarPath);
            File appDir;
            
            if (jarFile.isFile()) {
                // Running from JAR
                appDir = jarFile.getParentFile();
            } else {
                // Running from classes directory (IDE)
                appDir = new File(System.getProperty("user.dir"));
            }
            
            // Check if the directory is writable
            File testFile = new File(appDir, ".write_test");
            try {
                if (testFile.createNewFile()) {
                    testFile.delete();
                    return new File(appDir, subdir).getAbsolutePath();
                }
            } catch (Exception e) {
                // Directory is not writable, fall through to user home
            }
            
            // Fallback to user home directory
            String userHome = System.getProperty("user.home");
            File userDataDir = new File(userHome, ".secondhand-trading");
            return new File(userDataDir, subdir).getAbsolutePath();
            
        } catch (Exception e) {
            // Ultimate fallback to current directory
            System.err.println("Warning: Could not determine app directory, using current directory");
            return subdir;
        }
    }
    
    /**
     * Save uploaded image file
     * @param sourceFile Source file
     * @param itemId Item ID (optional, can be null for new items)
     * @return Saved file path, or null if failed
     */
    public static String saveImage(File sourceFile, Long itemId) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        
        try {
            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String extension = getFileExtension(sourceFile.getName());
            String filename = (itemId != null ? "item_" + itemId + "_" : "temp_") + 
                            timestamp + extension;
            
            Path targetPath = Paths.get(IMAGE_DIR, filename);
            
            // Copy file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return targetPath.toString();
        } catch (IOException e) {
            System.err.println("Failed to save image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Delete image file
     * @param imagePath Image path
     * @return true if successful
     */
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(imagePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if file is a valid image
     * @param file File to check
     * @return true if valid image
     */
    public static boolean isValidImage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif") ||
               name.endsWith(".bmp");
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return ".jpg";
    }
    
    /**
     * Get image directory path
     */
    public static String getImageDirectory() {
        return IMAGE_DIR;
    }
}
