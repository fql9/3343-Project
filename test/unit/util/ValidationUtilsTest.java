package unit.util;

import org.junit.jupiter.api.Test;
import util.ValidationUtils;
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

    @Test
    void testIsValidUsername_EdgeCases() {
        // Test with numbers only
        assertTrue(ValidationUtils.isValidUsername("123"));
        assertTrue(ValidationUtils.isValidUsername("12345678901234567890")); // exactly 20 chars
        
        // Test with underscores
        assertTrue(ValidationUtils.isValidUsername("___"));
        assertTrue(ValidationUtils.isValidUsername("_user_"));
        
        // Test with mixed case
        assertTrue(ValidationUtils.isValidUsername("UserNAME"));
        
        // Invalid: special characters
        assertFalse(ValidationUtils.isValidUsername("user-name")); // hyphen
        assertFalse(ValidationUtils.isValidUsername("user.name")); // dot
        assertFalse(ValidationUtils.isValidUsername("user!name")); // exclamation
    }

    @Test
    void testIsValidPassword_EdgeCases() {
        // Exactly 6 characters - should pass
        assertTrue(ValidationUtils.isValidPassword("123456"));
        
        // Very long password - should pass
        assertTrue(ValidationUtils.isValidPassword("a".repeat(100)));
        
        // Password with special characters - should pass
        assertTrue(ValidationUtils.isValidPassword("p@ss!#"));
        
        // Password with spaces - should pass (no restriction on content)
        assertTrue(ValidationUtils.isValidPassword("pass  word"));
        
        // 5 characters - should fail
        assertFalse(ValidationUtils.isValidPassword("12345"));
    }

    @Test
    void testIsValidEmail_EdgeCases() {
        // Valid: various formats
        assertTrue(ValidationUtils.isValidEmail("a@b.co"));
        assertTrue(ValidationUtils.isValidEmail("user123@domain123.com"));
        assertTrue(ValidationUtils.isValidEmail("user_name@example.org"));
        assertTrue(ValidationUtils.isValidEmail("user@a.b.c.d.com"));
        
        // Valid: with plus sign
        assertTrue(ValidationUtils.isValidEmail("user+filter@gmail.com"));
        
        // Invalid: missing parts
        assertFalse(ValidationUtils.isValidEmail("userexample.com")); // missing @
        assertFalse(ValidationUtils.isValidEmail("user@")); // missing domain
        assertFalse(ValidationUtils.isValidEmail("@example.com")); // missing local part
        
        // Invalid: special cases
        assertFalse(ValidationUtils.isValidEmail("user@@example.com")); // double @
        assertFalse(ValidationUtils.isValidEmail("user@-example.com")); // domain starts with hyphen
        
        // Whitespace only should be treated as empty (optional)
        assertTrue(ValidationUtils.isValidEmail("   ")); // whitespace is trimmed, treated as empty
    }

    @Test
    void testIsValidPrice_EdgeCases() {
        // Very small positive price
        assertTrue(ValidationUtils.isValidPrice(0.001));
        assertTrue(ValidationUtils.isValidPrice(Double.MIN_VALUE));
        
        // Very large price
        assertTrue(ValidationUtils.isValidPrice(999999.99));
        assertTrue(ValidationUtils.isValidPrice(Double.MAX_VALUE));
        
        // Exactly zero
        assertFalse(ValidationUtils.isValidPrice(0.0));
        
        // Negative prices
        assertFalse(ValidationUtils.isValidPrice(-0.01));
        assertFalse(ValidationUtils.isValidPrice(-999999.99));
    }

    @Test
    void testIsNotEmpty_EdgeCases() {
        // Single character
        assertTrue(ValidationUtils.isNotEmpty("a"));
        
        // String with leading/trailing spaces but content
        assertTrue(ValidationUtils.isNotEmpty("  hello  "));
        
        // Tab and newline characters only
        assertFalse(ValidationUtils.isNotEmpty("\t"));
        assertFalse(ValidationUtils.isNotEmpty("\n"));
        assertFalse(ValidationUtils.isNotEmpty("\t\n  "));
    }
}
