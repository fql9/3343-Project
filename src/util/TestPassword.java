package util;

public class TestPassword {
    public static void main(String[] args) {
        String[] passwords = {"123456", "password", "admin", "alice", "bob", "charlie", "david", "admin123", "password123", "test", "user", "qwerty"};
        
        System.out.println("===== 密码哈希对照表 =====");
        for (String pwd : passwords) {
            System.out.println(pwd + " -> " + PasswordUtils.hashPassword(pwd));
        }
    }
}

