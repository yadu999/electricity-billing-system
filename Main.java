package com.ebilling.app;

import com.ebilling.backend.service.BillingService;
import com.ebilling.backend.storage.MySQLDataStore;
import com.ebilling.backend.storage.Storage;
import com.ebilling.frontend.ui.MainFrame;
import com.ebilling.frontend.ui.UIUtils;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIUtils.installNiceLookAndFeel();
            Storage store = null;
            String dbUrl = System.getProperty("db.url");
            if (dbUrl == null || dbUrl.isBlank()) {
                JOptionPane.showMessageDialog(null, "Database URL is required. Start the app with -Ddb.url=jdbc:mysql://host:port/dbname", "Missing DB URL", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            try {
                String dbUser = System.getProperty("db.user", "root");
                String dbPass = System.getProperty("db.pass", "");
                store = new MySQLDataStore(dbUrl, dbUser, dbPass);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to connect to the database: " + e.getMessage(), "DB Connection Failed", JOptionPane.ERROR_MESSAGE);
                System.exit(2);
            }
            BillingService service = new BillingService(store);
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}