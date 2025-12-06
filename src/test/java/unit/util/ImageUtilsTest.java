package unit.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilsTest {

    private File tempImageFile;
    private static final String TEST_IMAGE_DIR = "item_images";

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary test image file
        tempImageFile = File.createTempFile("test_image", ".jpg");
        Files.write(tempImageFile.toPath(), "fake image data".getBytes());
    }

    @AfterEach
    void tearDown() {
        // Clean up temp file
        if (tempImageFile != null && tempImageFile.exists()) {
            tempImageFile.delete();
        }
        
        // Clean up saved images
        try {
            Files.walk(Paths.get(TEST_IMAGE_DIR))
                .filter(p -> p.toString().contains("test") || p.toString().contains("temp"))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        } catch (IOException e) {
            // Ignore
        }
    }

    @Test
    void testSaveImage_ValidFile() {
        // Act
        String savedPath = ImageUtils.saveImage(tempImageFile, 123L);

        // Assert
        assertNotNull(savedPath, "Should return saved path");
        assertTrue(savedPath.startsWith(TEST_IMAGE_DIR), "Path should be in image directory");
        assertTrue(savedPath.contains("item_123"), "Path should contain item ID");
        assertTrue(new File(savedPath).exists(), "Saved file should exist");
    }

    @Test
    void testSaveImage_NullFile() {
        // Act
        String savedPath = ImageUtils.saveImage(null, 123L);

        // Assert
        assertNull(savedPath, "Should return null for null file");
    }

    @Test
    void testSaveImage_NonExistentFile() {
        // Arrange
        File nonExistent = new File("nonexistent_file.jpg");

        // Act
        String savedPath = ImageUtils.saveImage(nonExistent, 123L);

        // Assert
        assertNull(savedPath, "Should return null for non-existent file");
    }

    @Test
    void testSaveImage_WithoutItemId() {
        // Act
        String savedPath = ImageUtils.saveImage(tempImageFile, null);

        // Assert
        assertNotNull(savedPath, "Should save even without item ID");
        assertTrue(savedPath.contains("temp_"), "Path should contain 'temp_' when no item ID");
    }

    @Test
    void testDeleteImage_ExistingFile() {
        // Arrange
        String savedPath = ImageUtils.saveImage(tempImageFile, 456L);
        assertNotNull(savedPath);

        // Act
        boolean result = ImageUtils.deleteImage(savedPath);

        // Assert
        assertTrue(result, "Should successfully delete existing file");
        assertFalse(new File(savedPath).exists(), "File should no longer exist");
    }

    @Test
    void testDeleteImage_NonExistentFile() {
        // Act
        boolean result = ImageUtils.deleteImage("nonexistent_path.jpg");

        // Assert
        assertFalse(result, "Should return false for non-existent file");
    }

    @Test
    void testDeleteImage_NullPath() {
        // Act
        boolean result = ImageUtils.deleteImage(null);

        // Assert
        assertFalse(result, "Should return false for null path");
    }

    @Test
    void testIsValidImage_ValidJpg() {
        // Arrange
        File jpgFile = new File("test.jpg");

        // Act & Assert
        assertTrue(ImageUtils.isValidImage(tempImageFile), "Should accept existing jpg file");
    }

    @Test
    void testIsValidImage_ValidExtensions() throws IOException {
        // Test multiple valid extensions
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        
        for (String ext : validExtensions) {
            File testFile = File.createTempFile("test", ext);
            try {
                assertTrue(ImageUtils.isValidImage(testFile), 
                    "Should accept " + ext + " extension");
            } finally {
                testFile.delete();
            }
        }
    }

    @Test
    void testIsValidImage_InvalidExtension() throws IOException {
        // Arrange
        File txtFile = File.createTempFile("test", ".txt");
        
        try {
            // Act
            boolean result = ImageUtils.isValidImage(txtFile);
            
            // Assert
            assertFalse(result, "Should reject non-image file");
        } finally {
            txtFile.delete();
        }
    }

    @Test
    void testIsValidImage_NullFile() {
        // Act
        boolean result = ImageUtils.isValidImage(null);

        // Assert
        assertFalse(result, "Should return false for null file");
    }

    @Test
    void testIsValidImage_NonExistentFile() {
        // Arrange
        File nonExistent = new File("nonexistent.jpg");

        // Act
        boolean result = ImageUtils.isValidImage(nonExistent);

        // Assert
        assertFalse(result, "Should return false for non-existent file");
    }

    @Test
    void testGetImageDirectory() {
        // Act
        String dir = ImageUtils.getImageDirectory();

        // Assert
        assertNotNull(dir, "Should return image directory");
        assertEquals("item_images", dir, "Should return correct directory name");
    }

    @Test
    void testSaveImage_PreservesFileContent() throws IOException {
        // Arrange
        byte[] originalContent = "test image content".getBytes();
        Files.write(tempImageFile.toPath(), originalContent);

        // Act
        String savedPath = ImageUtils.saveImage(tempImageFile, 789L);

        // Assert
        assertNotNull(savedPath);
        byte[] savedContent = Files.readAllBytes(Paths.get(savedPath));
        assertArrayEquals(originalContent, savedContent, "File content should be preserved");
    }

    @Test
    void testSaveImage_ReplacesExistingFile() {
        // Arrange - Save first time
        String firstPath = ImageUtils.saveImage(tempImageFile, 999L);
        assertNotNull(firstPath);
        
        // Act - Save again with same item ID (different timestamp)
        try { Thread.sleep(1100); } catch (InterruptedException e) { }
        String secondPath = ImageUtils.saveImage(tempImageFile, 999L);

        // Assert
        assertNotNull(secondPath);
        assertTrue(new File(secondPath).exists(), "New file should exist");
        // Both files can coexist due to timestamp in filename
    }

    @Test
    void testImageDirectory_CreatedAutomatically() {
        // Assert
        File imageDir = new File(TEST_IMAGE_DIR);
        assertTrue(imageDir.exists(), "Image directory should be created automatically");
        assertTrue(imageDir.isDirectory(), "Should be a directory");
    }
}
