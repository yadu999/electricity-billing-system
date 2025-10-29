package com.ebilling.app;

import java.sql.*;

public class DBVerify {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: DBVerify <jdbc-url> <user> <pass>");
            System.exit(1);
        }
        String url = args[0];
        String user = args[1];
        String pass = args[2];

        System.out.println("Connecting to: " + url + " as " + user);
        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected OK. Listing tables:");
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("SHOW TABLES")) {
                    int i = 0;
                    while (rs.next()) {
                        i++;
                        System.out.println(" - " + rs.getString(1));
                    }
                    if (i == 0) System.out.println(" (no tables found)");
                }

                // Count settings rows if table exists
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM settings")) {
                    if (rs.next()) System.out.println("settings rows: " + rs.getInt(1));
                } catch (SQLException e) {
                    System.out.println("settings table not found or error: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}
