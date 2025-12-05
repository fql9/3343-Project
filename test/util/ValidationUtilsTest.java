package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void testIsValidUsername() {
        // Valid usernames
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertTrue(ValidationUtils.isValidUsername("user_name"));
        assertTrue(ValidationUtils.isValidUsername("User_Name123"));
        assertTrue(ValidationUtils.isValidUsername("a".repeat(3)));
        assertTrue(ValidationUtils.isValidUsername("a".repeat(20)));
        
        // Invalid usernames
        assertFalse(ValidationUtils.isValidUsername(null));
        assertFalse(ValidationUtils.isValidUsername(""));
        assertFalse(ValidationUtils.isValidUsername("  "));
        assertFalse(ValidationUtils.isValidUsername("us")); // Too short
        assertFalse(ValidationUtils.isValidUsername("a".repeat(21))); // Too long
        assertFalse(ValidationUtils.isValidUsername("user name")); // Contains space
        assertFalse(ValidationUtils.isValidUsername("user@name")); // Contains special char
    }

    @Test
    void testIsValidPassword() {
        // Valid passwords
        assertTrue(ValidationUtils.isValidPassword("abc123")); // 6 characters
        assertTrue(ValidationUtils.isValidPassword("password123"));
        
        // Invalid passwords
        assertFalse(ValidationUtils.isValidPassword(null));
        assertFalse(ValidationUtils.isValidPassword(""));
        assertFalse(ValidationUtils.isValidPassword("abc12")); // Too short
    }

    @Test
    void testIsValidEmail() {
        // Valid emails
        assertTrue(ValidationUtils.isValidEmail(null)); // Email is optional
        assertTrue(ValidationUtils.isValidEmail("")); // Email is optional
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user+tag@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user@sub.example.com"));
        
        // Invalid emails
        assertFalse(ValidationUtils.isValidEmail("user")); // No domain
        assertFalse(ValidationUtils.isValidEmail("user@")); // No top-level domain
        assertFalse(ValidationUtils.isValidEmail("user@.com")); // Missing domain
        assertFalse(ValidationUtils.isValidEmail("@example.com")); // Missing username
        assertFalse(ValidationUtils.isValidEmail("user@example..com")); // Double dot
    }

    @Test
    void testIsValidPrice() {
        assertTrue(ValidationUtils.isValidPrice(10.99));
        assertTrue(ValidationUtils.isValidPrice(0.01));
        assertFalse(ValidationUtils.isValidPrice(0));
        assertFalse(ValidationUtils.isValidPrice(-5.0));
    }

    @Test
    void testIsNotEmpty() {
        assertTrue(ValidationUtils.isNotEmpty("hello"));
        assertFalse(ValidationUtils.isNotEmpty(null));
        assertFalse(ValidationUtils.isNotEmpty(""));
        assertFalse(ValidationUtils.isNotEmpty("  "));
    }

    @Test
    void testGetUsernameValidationMessage() {
        assertNotNull(ValidationUtils.getUsernameValidationMessage());
        assertTrue(ValidationUtils.getUsernameValidationMessage().contains("3-20 characters"));
    }

    @Test
    void testGetPasswordValidationMessage() {
        assertNotNull(ValidationUtils.getPasswordValidationMessage());
        assertTrue(ValidationUtils.getPasswordValidationMessage().contains("at least 6 characters"));
    }
}
