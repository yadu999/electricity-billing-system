package com.ebilling.frontend.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Enumeration;

public class UIUtils {

    public static void installNiceLookAndFeel() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
        // Tweak Nimbus colors a bit
        UIManager.put("control", new Color(245, 247, 250));
        UIManager.put("nimbusLightBackground", new Color(252, 253, 255));
        UIManager.put("nimbusFocus", new Color(80, 140, 255));
        UIManager.put("nimbusSelectionBackground", new Color(80, 140, 255));
        UIManager.put("text", new Color(30, 35, 40));
        setGlobalFont(new Font("SansSerif", Font.PLAIN, 14));
    }

    public static void setGlobalFont(Font f) {
        FontUIResource res = new FontUIResource(f);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof FontUIResource) {
                UIManager.put(key, res);
            }
        }
    }

    public static JPanel header(String emoji, String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(235, 243, 255));
        p.setBorder(new EmptyBorder(16,16,16,16));

        JLabel icon = new JLabel(emoji);
        icon.setFont(icon.getFont().deriveFont(Font.PLAIN, 32f));
        icon.setBorder(new EmptyBorder(0,0,0,10));
        p.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 20f));
        JLabel s = new JLabel(subtitle);
        s.setForeground(new Color(90, 100, 110));
        text.add(t); text.add(s);
        p.add(text, BorderLayout.CENTER);

        return p;
    }

    public static void makeTablePretty(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoCreateRowSorter(true);
        table.setSelectionBackground(new Color(60, 120, 245));
        table.setSelectionForeground(Color.WHITE);
        // Zebra stripes
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private final Color stripe = new Color(248, 250, 252);
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                if (!isSelected) c.setBackground((row % 2 == 0) ? Color.WHITE : stripe);
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    public static JPanel statusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(6, 12, 6, 12));
        p.setBackground(new Color(245, 247, 250));
        return p;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(64, 120, 255));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }

    public static JButton lightButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(240, 243, 247));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(6,12,6,12)
        ));
        return b;
    }
}