package util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Image utility class for handling item images
 */
public class ImageUtils {
    
    private static final String IMAGE_DIR = "item_images";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    static {
        // Create image directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(IMAGE_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create image directory: " + e.getMessage());
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
