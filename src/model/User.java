package model;

public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private UserRole role;
    private boolean active; // 是否被管理员封禁/禁用
    // getter/setter
}