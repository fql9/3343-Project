package util;

import config.DatabaseConfig;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ExportUsers {

    public static void main(String[] args) {
        String outputFile = "users_export.txt";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users");
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write(String.format("%-5s | %-15s | %-10s | %-25s | %-10s\n", "ID", "USERNAME", "ROLE", "EMAIL", "ACTIVE"));
            writer.write("------------------------------------------------------------------------------\n");

            int count = 0;
            while (rs.next()) {
                String line = String.format("%-5d | %-15s | %-10s | %-25s | %-10s",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getInt("active") == 1 ? "Yes" : "No"
                );
                writer.write(line);
                writer.newLine();
                count++;
            }

            System.out.println("成功导出 " + count + " 位用户信息到文件: " + outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}