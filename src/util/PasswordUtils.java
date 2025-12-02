package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password utility class - for password encryption and verification.
 * Uses SHA-256 algorithm for secure password hashing.
 */
public class PasswordUtils {

    private static final String ALGORITHM = "SHA-256";
    
    /**
     * Hash password using SHA-256
     * @param password Plain text password
     * @return Hashed password
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password encryption failed", e);
        }
    }
    
    /**
     * Verify if password matches
     * @param rawPassword Plain text password
     * @param hashedPassword Hashed password
     * @return Whether they match
     */
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        String hashed = hashPassword(rawPassword);
        return hashed.equals(hashedPassword);
    }
    
    /**
     * Generate random password
     * @param length Password length
     * @return Random password
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
}
