package com.ebilling.frontend.ui;

import com.ebilling.backend.model.Settings;
import com.ebilling.backend.service.BillingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final BillingService service;
    private final Runnable onChange;

    private final JTextField txtRate = new JTextField();
    private final JTextField txtFixed = new JTextField();
    private final JTextField txtTaxPct = new JTextField();

    public SettingsPanel(BillingService service, Runnable onChange) {
        this.service = service; this.onChange = onChange;
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(10,10,10,10));

        add(buildForm(), BorderLayout.NORTH);
        loadSettingsFields();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Tariff Settings"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,8,8,8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gc.gridx=0; gc.gridy=row; form.add(new JLabel("Rate per Unit"), gc);
        gc.gridx=1; gc.gridy=row; form.add(txtRate, gc);

        row++;
        gc.gridx=0; gc.gridy=row; form.add(new JLabel("Fixed Charge"), gc);
        gc.gridx=1; gc.gridy=row; form.add(txtFixed, gc);

        row++;
        gc.gridx=0; gc.gridy=row; form.add(new JLabel("Tax (%)"), gc);
        gc.gridx=1; gc.gridy=row; form.add(txtTaxPct, gc);

        JButton btnSave = UIUtils.primaryButton("💾 Save Settings");
        row++;
        gc.gridx=1; gc.gridy=row; form.add(btnSave, gc);

        btnSave.addActionListener(e -> onSave());
        return form;
    }

    public void loadSettingsFields() {
        Settings s = service.getSettings();
        txtRate.setText(String.valueOf(s.ratePerUnit));
        txtFixed.setText(String.valueOf(s.fixedCharge));
        txtTaxPct.setText(String.valueOf(s.taxPercent));
    }

    private void onSave() {
        try {
            double rate = Double.parseDouble(txtRate.getText().trim());
            double fixed = Double.parseDouble(txtFixed.getText().trim());
            double tax = Double.parseDouble(txtTaxPct.getText().trim());
            if (rate < 0 || fixed < 0 || tax < 0) {
                JOptionPane.showMessageDialog(this, "Values must be non-negative.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            service.updateSettings(rate, fixed, tax);
            JOptionPane.showMessageDialog(this, "Settings updated. New bills will use these values.");
            onChange.run();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }
}