package com.ebilling.frontend.ui;

import com.ebilling.backend.service.BillingService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final BillingService service;
    private CustomerPanel customerPanel;
    private BillingPanel billingPanel;
    private SettingsPanel settingsPanel;

    private final JLabel statusLabel = new JLabel("Ready");

    public MainFrame(BillingService service) {
        super("Electric Billing System ⚡");
        this.service = service;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(UIUtils.header("💡", "Electric Billing System", "Manage customers, generate bills, track payments"), BorderLayout.NORTH);

        // Shared refresh action: refresh data across panels and status
        Runnable refreshAll = () -> {
            // reload panels if they are initialized
            if (customerPanel != null) {
                customerPanel.reloadTable();
            }
            if (billingPanel != null) {
                billingPanel.reloadEverything();
            }
            refreshStatus();
        };

        JTabbedPane tabs = new JTabbedPane();
        customerPanel = new CustomerPanel(service, refreshAll);
        billingPanel = new BillingPanel(service, refreshAll);
        settingsPanel = new SettingsPanel(service, refreshAll);

        tabs.addTab("👤 Customers", customerPanel);
        tabs.addTab("🧾 Billing", billingPanel);
        tabs.addTab("⚙ Settings", settingsPanel);

        add(tabs, BorderLayout.CENTER);

        JPanel status = UIUtils.statusBar();
        status.add(statusLabel, BorderLayout.WEST);
        add(status, BorderLayout.SOUTH);

        setJMenuBar(buildMenu());
        refreshStatus();
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        file.add(exit);

        JMenu view = new JMenu("View");
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e -> { customerPanel.reloadTable(); billingPanel.reloadEverything(); refreshStatus(); });
        view.add(refresh);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Electric Billing System\nSwing frontend + pure Java backend\nData: " +
                new java.io.File(System.getProperty("user.home"), ".ebilling_data_v2.ser"),
                "About", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);

        bar.add(file); bar.add(view); bar.add(help);
        return bar;
    }

    public void refreshStatus() {
        int customers = service.getCustomers().size();
        long pending = service.getBills().stream().filter(b -> b.status.name().equals("PENDING")).count();
        long paid = service.getBills().stream().filter(b -> b.status.name().equals("PAID")).count();
        statusLabel.setText(String.format("👤 %d customers | 🧾 %d bills pending, ✅ %d paid", customers, pending, paid));
    }
}