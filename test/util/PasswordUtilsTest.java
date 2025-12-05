package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void testHashPassword() {
        String password = "testPassword123";
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertEquals(PasswordUtils.hashPassword(password), hashedPassword); // Same password should produce same hash
    }

    @Test
    void testVerifyPassword() {
        String password = "testPassword123";
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        assertTrue(PasswordUtils.verifyPassword(password, hashedPassword));
        assertFalse(PasswordUtils.verifyPassword("wrongPassword", hashedPassword));
    }

    @Test
    void testGenerateRandomPassword() {
        int length = 10;
        String randomPassword = PasswordUtils.generateRandomPassword(length);
        
        assertNotNull(randomPassword);
        assertEquals(length, randomPassword.length());
        
        // Test that different calls generate different passwords
        String anotherRandomPassword = PasswordUtils.generateRandomPassword(length);
        assertNotEquals(randomPassword, anotherRandomPassword);
    }
}
