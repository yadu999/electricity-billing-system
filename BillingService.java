package com.ebilling.backend.service;

import com.ebilling.backend.model.*;
import com.ebilling.backend.storage.Storage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class BillingService {
    private final Storage store;

    public BillingService(Storage store) {
        this.store = store;
    }

    // Settings
    public Settings getSettings() { return store.getSettingsCopy(); }
    public void updateSettings(double rate, double fixed, double taxPct) {
        if (rate < 0 || fixed < 0 || taxPct < 0) throw new IllegalArgumentException("Values must be >= 0");
        store.updateSettings(rate, fixed, taxPct);
    }

    // Customers
    public List<Customer> getCustomers() { return store.getCustomersCopy(); }
    public void addCustomer(String name, String meter, String address) {
        Objects.requireNonNull(name); Objects.requireNonNull(meter);
        Customer c = new Customer();
        c.id = UUID.randomUUID();
        c.name = name.trim();
        c.meterNumber = meter.trim();
        c.address = address == null ? "" : address.trim();
        c.createdAt = LocalDate.now();
        store.addCustomer(c);
    }
    public void updateCustomer(UUID id, String name, String meter, String address) {
        Customer c = new Customer();
        c.id = id; c.name = name.trim(); c.meterNumber = meter.trim(); c.address = address == null ? "" : address.trim();
        c.createdAt = LocalDate.now();
        store.updateCustomer(c);
    }
    public boolean deleteCustomer(UUID id, boolean removeBills) { return store.deleteCustomer(id, removeBills); }

    // Bills
    public List<Bill> getBills() { return store.getBillsCopy(); }
    public List<Bill> getBillsByCustomer(UUID customerId) { return store.getBillsByCustomer(customerId); }

    public Bill generateOrUpdateBill(UUID customerId, YearMonth month, int units) {
        if (units < 0) throw new IllegalArgumentException("Units cannot be negative");
        Customer c = store.getCustomer(customerId).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Settings s = store.getSettingsCopy();

        double subtotal = s.fixedCharge + (units * s.ratePerUnit);
        double tax = subtotal * (s.taxPercent / 100.0);
        double total = subtotal + tax;
        subtotal = round2(subtotal);
        tax = round2(tax);
        total = round2(total);

        Optional<Bill> existing = store.findBill(customerId, month);
        Bill b = existing.orElseGet(() -> {
            Bill nb = new Bill();
            nb.id = UUID.randomUUID();
            nb.customerId = c.id;
            nb.customerSnapshot = c.name;
            nb.meterSnapshot = c.meterNumber;
            nb.month = month;
            nb.status = BillStatus.PENDING;
            nb.issueDate = LocalDate.now();
            nb.dueDate = nb.issueDate.plusDays(15);
            return nb;
        });

        b.units = units;
        b.ratePerUnit = s.ratePerUnit;
        b.fixedCharge = s.fixedCharge;
        b.taxPercent = s.taxPercent;
        b.subtotal = subtotal;
        b.tax = tax;
        b.total = total;
        if (existing.isPresent() && b.status == BillStatus.PENDING) {
            b.issueDate = LocalDate.now();
            b.dueDate = b.issueDate.plusDays(15);
        }

        store.addOrReplaceBill(b);
        return b;
    }

    public boolean markBillPaid(UUID billId) { return store.markBillPaid(billId); }
    public boolean deleteBill(UUID billId) { return store.deleteBill(billId); }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}