package com.ebilling.frontend.ui;

import com.ebilling.backend.model.Bill;
import com.ebilling.backend.model.Settings;
import com.ebilling.backend.model.Customer;
import com.ebilling.backend.service.BillingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class BillingPanel extends JPanel {
    private final BillingService service;
    private final Runnable onChange;

    private final JComboBox<Customer> cmbCustomer = new JComboBox<>();
    private final JSpinner spUnits = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
    private final JSpinner spMonth;

    private final JTextArea txtResult = new JTextArea(6, 30);
    private final JCheckBox chkOnlySelected = new JCheckBox("Show only selected customer");
    private final JTextField txtSearch = new JTextField(20);

    private final JTable tblBills = new JTable();
    private final DefaultTableModel billsModel = new DefaultTableModel(new String[]{
            "ID", "Customer", "Month", "Units", "Subtotal", "Tax", "Total", "Status", "Due", "Paid"
    }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private TableRowSorter<DefaultTableModel> sorter;

    private final DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");
    private final NumberFormat money = new DecimalFormat("#,##0.00");

    public BillingPanel(BillingService service, Runnable onChange) {
        this.service = service;
        this.onChange = onChange;
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(10,10,10,10));

        // Month spinner
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.MONTH);
        spMonth = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spMonth, "yyyy-MM");
        spMonth.setEditor(editor);

        add(buildForm(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottomActions(), BorderLayout.SOUTH);

        reloadEverything();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Generate / Update Bill"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gc.gridx=0; gc.gridy=row; form.add(new JLabel("Customer"), gc);
        gc.gridx=1; gc.gridy=row; form.add(cmbCustomer, gc);

        row++;
        gc.gridx=0; gc.gridy=row; form.add(new JLabel("Month (YYYY-MM)"), gc);
        gc.gridx=1; gc.gridy=row; form.add(spMonth, gc);

        row++;
        gc.gridx=0; gc.gridy=row; form.add(new JLabel("Units Consumed"), gc);
        gc.gridx=1; gc.gridy=row; form.add(spUnits, gc);

        JButton btnGenerate = UIUtils.primaryButton("⚙ Generate / Update");
        row++;
        gc.gridx=1; gc.gridy=row; form.add(btnGenerate, gc);

        btnGenerate.addActionListener(e -> onGenerate());

        return form;
    }

    private JComponent buildCenter() {
        txtResult.setEditable(false);
        txtResult.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtResult.setBorder(BorderFactory.createTitledBorder("Bill Summary"));

        JPanel left = new JPanel(new BorderLayout(5,5));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Search:"));
        txtSearch.setToolTipText("Filter bills (customer, month, status, etc.)");
        filters.add(txtSearch);
        filters.add(chkOnlySelected);
        JButton btnRefresh = UIUtils.lightButton("🔄 Refresh");
        filters.add(btnRefresh);
        left.add(filters, BorderLayout.NORTH);
        left.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        // Bills table
        tblBills.setModel(billsModel);
        // Hide ID
        tblBills.getColumnModel().getColumn(0).setMinWidth(0);
        tblBills.getColumnModel().getColumn(0).setMaxWidth(0);
        UIUtils.makeTablePretty(tblBills);

        sorter = new TableRowSorter<>(billsModel);
        tblBills.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void ref() {
                String q = txtSearch.getText().trim();
                if (q.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { ref(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { ref(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { ref(); }
        });

        btnRefresh.addActionListener(e -> reloadBills());
        chkOnlySelected.addActionListener(e -> reloadBills());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                left, new JScrollPane(tblBills));
        split.setResizeWeight(0.35);
        return split;
    }

    private JPanel buildBottomActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDelete = UIUtils.lightButton("🗑 Delete Bill");
        JButton btnPaid = UIUtils.primaryButton("✅ Mark as Paid");
        p.add(btnDelete); p.add(btnPaid);

        btnPaid.addActionListener(e -> onMarkPaid());
        btnDelete.addActionListener(e -> onDeleteBill());
        return p;
    }

    public void reloadEverything() {
        reloadCustomers();
        reloadBills();
    }

    public void reloadCustomers() {
        cmbCustomer.removeAllItems();
        List<Customer> list = service.getCustomers();
        for (Customer c : list) cmbCustomer.addItem(c);
    }

    public void reloadBills() {
        billsModel.setRowCount(0);
        List<Bill> bills;
        if (chkOnlySelected.isSelected() && cmbCustomer.getSelectedItem() instanceof Customer) {
            bills = service.getBillsByCustomer(((Customer) cmbCustomer.getSelectedItem()).id);
        } else bills = service.getBills();

        bills.sort(Comparator.comparing((Bill b) -> b.month).reversed()
                .thenComparing(b -> b.customerSnapshot.toLowerCase(Locale.ROOT)));

        for (Bill b : bills) {
            billsModel.addRow(new Object[]{
                    b.id.toString(),
                    b.customerSnapshot + " (" + b.meterSnapshot + ")",
                    b.month.format(ymFmt),
                    b.units,
                    money.format(b.subtotal),
                    money.format(b.tax),
                    money.format(b.total),
                    b.status.name(),
                    b.dueDate == null ? "" : b.dueDate.toString(),
                    b.paidDate == null ? "" : b.paidDate.toString()
            });
        }
    }

    private void onGenerate() {
        Customer c = (Customer) cmbCustomer.getSelectedItem();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Please add/select a customer first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int units = (int) spUnits.getValue();
        Date d = (Date) spMonth.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        YearMonth ym = YearMonth.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

        Bill bill = service.generateOrUpdateBill(c.id, ym, units);
        Settings s = service.getSettings();

        StringBuilder sb = new StringBuilder();
        sb.append("Bill for ").append(bill.customerSnapshot).append(" (").append(bill.meterSnapshot).append(")\n");
        sb.append("Month: ").append(bill.month.format(ymFmt)).append("\n");
        sb.append("Units: ").append(bill.units).append("\n");
        sb.append("--------------------------------\n");
        sb.append(String.format("Rate per unit  : %s\n", money.format(bill.ratePerUnit)));
        sb.append(String.format("Fixed charge   : %s\n", money.format(bill.fixedCharge)));
        sb.append(String.format("Subtotal       : %s\n", money.format(bill.subtotal)));
        sb.append(String.format("Tax (%.2f%%)    : %s\n", bill.taxPercent, money.format(bill.tax)));
        sb.append(String.format("Total          : %s\n", money.format(bill.total)));
        sb.append("--------------------------------\n");
        sb.append("Status: ").append(bill.status).append("\n");
        sb.append("Due by: ").append(bill.dueDate).append("\n");

        txtResult.setText(sb.toString());
        reloadBills();
        onChange.run();
    }

    private void onMarkPaid() {
        int row = tblBills.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill to mark as paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = tblBills.convertRowIndexToModel(row);
        java.util.UUID id = java.util.UUID.fromString(String.valueOf(billsModel.getValueAt(modelRow, 0)));
        if (service.markBillPaid(id)) {
            JOptionPane.showMessageDialog(this, "Bill marked as PAID.");
            reloadBills();
            onChange.run();
        }
    }

    private void onDeleteBill() {
        int row = tblBills.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = tblBills.convertRowIndexToModel(row);
        java.util.UUID id = java.util.UUID.fromString(String.valueOf(billsModel.getValueAt(modelRow, 0)));
        int res = JOptionPane.showConfirmDialog(this, "Delete selected bill?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            if (service.deleteBill(id)) {
                JOptionPane.showMessageDialog(this, "Bill deleted.");
                reloadBills();
                onChange.run();
            }
        }
    }
}