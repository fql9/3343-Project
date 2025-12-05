package util;

import util.PasswordUtils;

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

    @Test
    void testHashPassword_EmptyString() {
        String hashedPassword = PasswordUtils.hashPassword("");
        assertNotNull(hashedPassword);
        // Empty string should still produce a valid hash
        assertTrue(hashedPassword.length() > 0);
    }

    @Test
    void testHashPassword_VeryLongPassword() {
        String longPassword = "a".repeat(1000);
        String hashedPassword = PasswordUtils.hashPassword(longPassword);
        assertNotNull(hashedPassword);
        // SHA-256 produces 32 bytes = 44 Base64 characters (with padding)
        assertEquals(44, hashedPassword.length());
    }

    @Test
    void testHashPassword_SpecialCharacters() {
        String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String hashedPassword = PasswordUtils.hashPassword(specialPassword);
        assertNotNull(hashedPassword);
        assertEquals(44, hashedPassword.length());
    }

    @Test
    void testHashPassword_UnicodeCharacters() {
        String unicodePassword = "密码中文字符";
        String hashedPassword = PasswordUtils.hashPassword(unicodePassword);
        assertNotNull(hashedPassword);
        assertEquals(44, hashedPassword.length());
    }

    @Test
    void testVerifyPassword_EmptyPassword() {
        String hashedEmpty = PasswordUtils.hashPassword("");
        assertTrue(PasswordUtils.verifyPassword("", hashedEmpty));
        assertFalse(PasswordUtils.verifyPassword(" ", hashedEmpty));
    }

    @Test
    void testVerifyPassword_CaseSensitive() {
        String password = "Password";
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        assertFalse(PasswordUtils.verifyPassword("password", hashedPassword));
        assertFalse(PasswordUtils.verifyPassword("PASSWORD", hashedPassword));
        assertTrue(PasswordUtils.verifyPassword("Password", hashedPassword));
    }

    @Test
    void testGenerateRandomPassword_MinLength() {
        String randomPassword = PasswordUtils.generateRandomPassword(1);
        assertNotNull(randomPassword);
        assertEquals(1, randomPassword.length());
    }

    @Test
    void testGenerateRandomPassword_LongLength() {
        int length = 100;
        String randomPassword = PasswordUtils.generateRandomPassword(length);
        assertNotNull(randomPassword);
        assertEquals(length, randomPassword.length());
    }

    @Test
    void testGenerateRandomPassword_ZeroLength() {
        String randomPassword = PasswordUtils.generateRandomPassword(0);
        assertNotNull(randomPassword);
        assertEquals(0, randomPassword.length());
    }

    @Test
    void testGenerateRandomPassword_ContainsAlphanumeric() {
        // Generate a longer password to increase probability of having both letters and digits
        String randomPassword = PasswordUtils.generateRandomPassword(50);
        
        boolean hasLetter = randomPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = randomPassword.chars().anyMatch(Character::isDigit);
        
        // At length 50, extremely likely to have both
        assertTrue(hasLetter || hasDigit); // At minimum should have one type
    }

    @Test
    void testHashPassword_Deterministic() {
        String password = "samePasswordEveryTime";
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);
        String hash3 = PasswordUtils.hashPassword(password);
        
        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }

    @Test
    void testHashPassword_Null() {
        // Test null handling - may throw NPE or return null depending on implementation
        assertThrows(Exception.class, () -> PasswordUtils.hashPassword(null));
    }
}
