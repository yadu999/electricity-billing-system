package com.ebilling.backend.storage;

import com.ebilling.backend.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class MySQLDataStore implements Storage {
    private final String url;
    private final String user;
    private final String pass;

    public MySQLDataStore(String url, String user, String pass) throws SQLException {
        this.url = url; this.user = user; this.pass = pass;
        try (Connection c = connect()) {
            initSchema(c);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }

    private void initSchema(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS settings (id INT PRIMARY KEY CHECK (id=1), rate DOUBLE, fixed_charge DOUBLE, tax_percent DOUBLE);");
            s.execute("INSERT IGNORE INTO settings (id, rate, fixed_charge, tax_percent) VALUES (1, 6.5, 50.0, 10.0);");

            s.execute("CREATE TABLE IF NOT EXISTS customers (id CHAR(36) PRIMARY KEY, name VARCHAR(255), meter VARCHAR(255), address TEXT, created_at DATE);");

            s.execute("CREATE TABLE IF NOT EXISTS bills (id CHAR(36) PRIMARY KEY, customer_id CHAR(36), customer_snapshot VARCHAR(255), meter_snapshot VARCHAR(255), year INT, month INT, units INT, rate DOUBLE, fixed_charge DOUBLE, tax_percent DOUBLE, subtotal DOUBLE, tax DOUBLE, total DOUBLE, status VARCHAR(20), issue_date DATE, due_date DATE, paid_date DATE, FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE);");
        }
    }

    // Settings
    @Override
    public synchronized Settings getSettingsCopy() {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT rate, fixed_charge, tax_percent FROM settings WHERE id=1")) {
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    Settings s = new Settings();
                    s.ratePerUnit = r.getDouble(1);
                    s.fixedCharge = r.getDouble(2);
                    s.taxPercent = r.getDouble(3);
                    return s;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new Settings();
    }

    @Override
    public synchronized void updateSettings(double rate, double fixed, double taxPct) {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("UPDATE settings SET rate=?, fixed_charge=?, tax_percent=? WHERE id=1")) {
            p.setDouble(1, rate); p.setDouble(2, fixed); p.setDouble(3, taxPct); p.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Customers
    @Override
    public synchronized List<Customer> getCustomersCopy() {
        List<Customer> out = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT id, name, meter, address, created_at FROM customers")) {
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Customer cst = new Customer();
                    cst.id = UUID.fromString(r.getString(1));
                    cst.name = r.getString(2);
                    cst.meterNumber = r.getString(3);
                    cst.address = r.getString(4);
                    java.sql.Date d = r.getDate(5); if (d != null) cst.createdAt = d.toLocalDate();
                    out.add(cst);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    @Override
    public synchronized Optional<Customer> getCustomer(UUID id) {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT id, name, meter, address, created_at FROM customers WHERE id=?")) {
            p.setString(1, id.toString());
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    Customer cst = new Customer();
                    cst.id = UUID.fromString(r.getString(1));
                    cst.name = r.getString(2);
                    cst.meterNumber = r.getString(3);
                    cst.address = r.getString(4);
                    java.sql.Date d = r.getDate(5); if (d != null) cst.createdAt = d.toLocalDate();
                    return Optional.of(cst);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public synchronized void addCustomer(Customer c) {
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement("INSERT INTO customers (id, name, meter, address, created_at) VALUES (?,?,?,?,?)")) {
            p.setString(1, c.id.toString()); p.setString(2, c.name); p.setString(3, c.meterNumber); p.setString(4, c.address); p.setDate(5, java.sql.Date.valueOf(c.createdAt));
            p.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public synchronized void updateCustomer(Customer c) {
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement("UPDATE customers SET name=?, meter=?, address=?, created_at=? WHERE id=?")) {
            p.setString(1, c.name); p.setString(2, c.meterNumber); p.setString(3, c.address); p.setDate(4, java.sql.Date.valueOf(c.createdAt)); p.setString(5, c.id.toString());
            p.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public synchronized boolean deleteCustomer(UUID id, boolean removeBills) {
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement("DELETE FROM customers WHERE id=?")) {
            p.setString(1, id.toString());
            int res = p.executeUpdate();
            if (removeBills) {
                try (PreparedStatement pb = conn.prepareStatement("DELETE FROM bills WHERE customer_id=?")) { pb.setString(1, id.toString()); pb.executeUpdate(); }
            }
            return res > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Bills
    @Override
    public synchronized List<Bill> getBillsCopy() {
        List<Bill> out = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT id, customer_id, customer_snapshot, meter_snapshot, year, month, units, rate, fixed_charge, tax_percent, subtotal, tax, total, status, issue_date, due_date, paid_date FROM bills")) {
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    out.add(mapBill(r));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    @Override
    public synchronized List<Bill> getBillsByCustomer(UUID customerId) {
        List<Bill> out = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT id, customer_id, customer_snapshot, meter_snapshot, year, month, units, rate, fixed_charge, tax_percent, subtotal, tax, total, status, issue_date, due_date, paid_date FROM bills WHERE customer_id=?")) {
            p.setString(1, customerId.toString());
            try (ResultSet r = p.executeQuery()) { while (r.next()) out.add(mapBill(r)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    @Override
    public synchronized Optional<Bill> findBill(UUID customerId, YearMonth month) {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT id, customer_id, customer_snapshot, meter_snapshot, year, month, units, rate, fixed_charge, tax_percent, subtotal, tax, total, status, issue_date, due_date, paid_date FROM bills WHERE customer_id=? AND year=? AND month=?")) {
            p.setString(1, customerId.toString()); p.setInt(2, month.getYear()); p.setInt(3, month.getMonthValue());
            try (ResultSet r = p.executeQuery()) { if (r.next()) return Optional.of(mapBill(r)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public synchronized void addOrReplaceBill(Bill bill) {
        try (Connection conn = connect()) {
            // upsert
            try (PreparedStatement p = conn.prepareStatement("REPLACE INTO bills (id, customer_id, customer_snapshot, meter_snapshot, year, month, units, rate, fixed_charge, tax_percent, subtotal, tax, total, status, issue_date, due_date, paid_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                p.setString(1, bill.id.toString());
                p.setString(2, bill.customerId.toString());
                p.setString(3, bill.customerSnapshot);
                p.setString(4, bill.meterSnapshot);
                p.setInt(5, bill.month.getYear());
                p.setInt(6, bill.month.getMonthValue());
                p.setInt(7, bill.units);
                p.setDouble(8, bill.ratePerUnit);
                p.setDouble(9, bill.fixedCharge);
                p.setDouble(10, bill.taxPercent);
                p.setDouble(11, bill.subtotal);
                p.setDouble(12, bill.tax);
                p.setDouble(13, bill.total);
                p.setString(14, bill.status == null ? "PENDING" : bill.status.name());
                p.setDate(15, bill.issueDate == null ? null : java.sql.Date.valueOf(bill.issueDate));
                p.setDate(16, bill.dueDate == null ? null : java.sql.Date.valueOf(bill.dueDate));
                p.setDate(17, bill.paidDate == null ? null : java.sql.Date.valueOf(bill.paidDate));
                p.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public synchronized boolean markBillPaid(UUID billId) {
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement("UPDATE bills SET status='PAID', paid_date=? WHERE id=?")) {
            p.setDate(1, java.sql.Date.valueOf(LocalDate.now())); p.setString(2, billId.toString());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public synchronized boolean deleteBill(UUID billId) {
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement("DELETE FROM bills WHERE id=?")) {
            p.setString(1, billId.toString());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Bill mapBill(ResultSet r) throws SQLException {
        Bill b = new Bill();
        b.id = UUID.fromString(r.getString(1));
        b.customerId = UUID.fromString(r.getString(2));
        b.customerSnapshot = r.getString(3);
        b.meterSnapshot = r.getString(4);
        int year = r.getInt(5); int month = r.getInt(6);
        b.month = YearMonth.of(year, month);
        b.units = r.getInt(7);
        b.ratePerUnit = r.getDouble(8);
        b.fixedCharge = r.getDouble(9);
        b.taxPercent = r.getDouble(10);
        b.subtotal = r.getDouble(11);
        b.tax = r.getDouble(12);
        b.total = r.getDouble(13);
        String st = r.getString(14); b.status = st == null ? BillStatus.PENDING : BillStatus.valueOf(st);
    java.sql.Date id = r.getDate(15); b.issueDate = id == null ? null : id.toLocalDate();
    java.sql.Date dd = r.getDate(16); b.dueDate = dd == null ? null : dd.toLocalDate();
    java.sql.Date pd = r.getDate(17); b.paidDate = pd == null ? null : pd.toLocalDate();
        return b;
    }
}
