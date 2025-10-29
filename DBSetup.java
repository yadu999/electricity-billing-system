package com.ebilling.app;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class DBSetup {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: DBSetup <jdbc-admin-url> <admin-user> <admin-pass> <sql-file>");
            System.exit(1);
        }
        String url = args[0];
        String user = args[1];
        String pass = args[2];
        String sqlFile = args[3];

        System.out.println("Connecting to: " + url + " as " + user);

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected. Reading SQL file: " + sqlFile);
            String sql = new String(Files.readAllBytes(Paths.get(sqlFile)), java.nio.charset.StandardCharsets.UTF_8);
            // Remove comments that start with -- and split by semicolon
            StringBuilder cleaned = new StringBuilder();
            BufferedReader br = new BufferedReader(new StringReader(sql));
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (t.startsWith("--") || t.isEmpty()) continue;
                cleaned.append(line).append('\n');
            }
            String[] parts = cleaned.toString().split(";");
            try (Statement st = c.createStatement()) {
                for (String p : parts) {
                    String s = p.trim();
                    if (s.isEmpty()) continue;
                    System.out.println("Executing: " + s.replaceAll("\n", " ").substring(0, Math.min(120, s.length())) + "...");
                    st.execute(s);
                }
            }
            System.out.println("SQL executed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
