package util;

import java.util.regex.Pattern;

/**
 * Data validation utility class.
 * Provides common validation methods for user input.
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    
    /**
     * Validate username format
     * @param username Username
     * @return Whether valid
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validate password strength
     * @param password Password
     * @return Whether valid
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }
    
    /**
     * Validate email format
     * @param email Email
     * @return Whether valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email is optional
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate price
     * @param price Price
     * @return Whether valid
     */
    public static boolean isValidPrice(double price) {
        return price > 0;
    }
    
    /**
     * Validate string is not empty
     * @param str String
     * @return Whether not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Get username validation error message
     * @return Error message
     */
    public static String getUsernameValidationMessage() {
        return "Username must be 3-20 characters, containing only letters, numbers and underscores";
    }
    
    /**
     * Get password validation error message
     * @return Error message
     */
    public static String getPasswordValidationMessage() {
        return "Password must be at least 6 characters";
    }
}
