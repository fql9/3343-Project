package service;

import dao.UserDao;
import dao.impl.UserDaoImpl;
import model.User;
import model.UserRole;
import util.PasswordUtils;
import util.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * User service class - handles user-related business logic
 */
public class UserService {

    private final UserDao userDao;
    private final OrderService orderService;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Currently logged in user
    private static User currentUser;
    
    public UserService() {
        this.userDao = new UserDaoImpl();
        this.orderService = new OrderService();
    }
    
    /**
     * User registration
     * @param username Username
     * @param password Password
     * @param email Email
     * @param role Role
     * @return Registration result message
     */
    public String register(String username, String password, String email, UserRole role) {
        // Validate username
        if (!ValidationUtils.isValidUsername(username)) {
            return ValidationUtils.getUsernameValidationMessage();
        }
        
        // Validate password
        if (!ValidationUtils.isValidPassword(password)) {
            return ValidationUtils.getPasswordValidationMessage();
        }
        
        // Validate email
        if (!ValidationUtils.isValidEmail(email)) {
            return "Invalid email format";
        }
        
        // Check if username already exists
        if (userDao.findByUsername(username) != null) {
            return "Username already exists";
        }
        
        // Create user
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtils.hashPassword(password));
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        
        userDao.save(user);
        return null; // Success returns null
    }
    
    /**
     * User login
     * @param username Username
     * @param password Password
     * @return Login result message (success returns null)
     */
    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        
        if (user == null) {
            return "User does not exist";
        }
        
        if (!user.isActive()) {
            return "Account has been banned";
        }
        
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            return "Incorrect password";
        }
        
        currentUser = user;
        return null; // Success returns null
    }
    
    /**
     * User logout
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Get currently logged in user
     * @return Current user
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in
     * @return Whether logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user is admin
     * @return Whether is admin
     */
    public static boolean isAdmin() {
        return currentUser != null && UserRole.ADMIN.name().equals(currentUser.getRole());
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User object
     */
    public User getUserById(Long userId) {
        return userDao.findById(userId);
    }
    
    /**
     * Get all users (admin function)
     * @return User list
     */
    public List<User> getAllUsers() {
        return userDao.findAll();
    }
    
    /**
     * Ban/Unban user
     * @param userId User ID
     * @param active Whether active
     */
    public void setUserActive(Long userId, boolean active) {
        User user = userDao.findById(userId);
        if (user != null) {
            user.setActive(active);
            userDao.update(user);
        }
    }
    
    /**
     * Delete user
     * @param userId User ID
     */
    public void deleteUser(Long userId) {
        userDao.delete(userId);
    }
    
    /**
     * Change password
     * @param oldPassword Old password
     * @param newPassword New password
     * @return Change result message
     */
    public String changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            return "Not logged in";
        }
        
        if (!PasswordUtils.verifyPassword(oldPassword, currentUser.getPasswordHash())) {
            return "Incorrect old password";
        }
        
        if (!ValidationUtils.isValidPassword(newPassword)) {
            return ValidationUtils.getPasswordValidationMessage();
        }
        
        currentUser.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        userDao.update(currentUser);
        return null;
    }
    
    /**
     * Update user profile (email, bio, avatar)
     * @param email New email
     * @param bio New bio
     * @param avatarUrl New avatar URL
     * @return Update result message
     */
    public String updateProfile(String email, String bio, String avatarUrl) {
        if (currentUser == null) {
            return "Not logged in";
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            return "Invalid email format";
        }
        
        currentUser.setEmail(email);
        currentUser.setBio(bio);
        currentUser.setAvatarUrl(avatarUrl);
        
        userDao.update(currentUser);
        return null;
    }
    
    /**
     * Get total sales amount for current user
     */
    public double getTotalSalesAmount() {
        if (currentUser == null) return 0.0;
        return orderService.getOrdersBySeller(currentUser.getId()).stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()) || "SHIPPED".equals(o.getStatus()) || "PAID".equals(o.getStatus()))
                .mapToDouble(model.Order::getAmount)
                .sum();
    }

    /**
     * Get total purchase amount for current user
     */
    public double getTotalPurchaseAmount() {
        if (currentUser == null) return 0.0;
        return orderService.getOrdersByBuyer(currentUser.getId()).stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()) || "SHIPPED".equals(o.getStatus()) || "PAID".equals(o.getStatus()))
                .mapToDouble(model.Order::getAmount)
                .sum();
    }
}
