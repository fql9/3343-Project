package util;

import config.DatabaseConfig;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ExportUsers {

    private static String truncateOrPad(String str, int length) {
        if (str == null) str = "";
        if (str.length() > length) {
            return str.substring(0, length);
        }
        return str;
    }

    public static void main(String[] args) {
        String outputFile = "users_export.txt";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users");
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("ID | USERNAME        | ROLE      | EMAIL                     | ACTIVE   \n");
            writer.write("------------------------------------------------------------------------------\n");

            int count = 0;
            while (rs.next()) {
                String line = String.format("%-2d | %-15s | %-9s | %-25s | %-8s",
                        rs.getInt("id"),
                        truncateOrPad(rs.getString("username"), 15),
                        truncateOrPad(rs.getString("role"), 9),
                        truncateOrPad(rs.getString("email"), 25),
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