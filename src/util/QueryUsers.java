package util;

import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class QueryUsers {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, password, email, role FROM users")) {

            System.out.println("===== 用户列表 =====");
            System.out.printf("%-5s | %-15s | %-50s | %-25s | %-10s%n", "ID", "USERNAME", "PASSWORD_HASH", "EMAIL", "ROLE");
            System.out.println("-".repeat(120));

            while (rs.next()) {
                System.out.printf("%-5d | %-15s | %-50s | %-25s | %-10s%n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }

            // 测试常见密码
            System.out.println("\n===== 密码验证测试 =====");
            String[] testPasswords = {"123456", "password", "admin", "alice", "bob", "charlie", "david", "admin123", "password123"};
            
            rs.close();
            ResultSet rs2 = stmt.executeQuery("SELECT username, password FROM users");
            while (rs2.next()) {
                String username = rs2.getString("username");
                String storedHash = rs2.getString("password");
                
                for (String testPwd : testPasswords) {
                    if (PasswordUtils.verifyPassword(testPwd, storedHash)) {
                        System.out.println("用户 " + username + " 的密码是: " + testPwd);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

