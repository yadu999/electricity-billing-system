package com.ebilling.backend.storage;

import com.ebilling.backend.model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore implements Storage {
    private final File file;
    private DataModel model;

    public DataStore() {
        this.file = new File(System.getProperty("user.home"), ".ebilling_data_v2.ser");
        load();
    }

    private synchronized void load() {
        if (!file.exists()) {
            model = new DataModel();
            save();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object o = ois.readObject();
            model = (o instanceof DataModel) ? (DataModel) o : new DataModel();
        } catch (Exception e) {
            e.printStackTrace();
            model = new DataModel();
        }
    }

    private synchronized void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Settings
    public synchronized Settings getSettingsCopy() {
        return new Settings(model.settings);
    }
    public synchronized void updateSettings(double rate, double fixed, double taxPct) {
        model.settings.ratePerUnit = rate;
        model.settings.fixedCharge = fixed;
        model.settings.taxPercent = taxPct;
        save();
    }

    // Customers
    public synchronized List<Customer> getCustomersCopy() {
        return new ArrayList<>(model.customers);
    }
    public synchronized Optional<Customer> getCustomer(UUID id) {
        return model.customers.stream().filter(c -> c.id.equals(id)).findFirst();
    }
    public synchronized void addCustomer(Customer c) {
        model.customers.add(c);
        save();
    }
    public synchronized void updateCustomer(Customer c) {
        for (int i = 0; i < model.customers.size(); i++) {
            if (model.customers.get(i).id.equals(c.id)) {
                model.customers.set(i, c);
                save();
                return;
            }
        }
    }
    public synchronized boolean deleteCustomer(UUID id, boolean removeBills) {
        boolean removed = model.customers.removeIf(c -> c.id.equals(id));
        if (removed && removeBills) {
            model.bills.removeIf(b -> b.customerId.equals(id));
        }
        save();
        return removed;
    }

    // Bills
    public synchronized List<Bill> getBillsCopy() {
        return new ArrayList<>(model.bills);
    }
    public synchronized List<Bill> getBillsByCustomer(UUID customerId) {
        return model.bills.stream().filter(b -> b.customerId.equals(customerId)).collect(Collectors.toList());
    }
    public synchronized Optional<Bill> findBill(UUID customerId, YearMonth month) {
        return model.bills.stream().filter(b -> b.customerId.equals(customerId) && b.month.equals(month)).findFirst();
    }
    public synchronized void addOrReplaceBill(Bill bill) {
        for (int i = 0; i < model.bills.size(); i++) {
            if (model.bills.get(i).id.equals(bill.id)) {
                model.bills.set(i, bill);
                save(); return;
            }
        }
        model.bills.add(bill);
        save();
    }
    public synchronized boolean markBillPaid(UUID billId) {
        for (Bill b : model.bills) {
            if (b.id.equals(billId)) {
                b.status = BillStatus.PAID;
                b.paidDate = LocalDate.now();
                save();
                return true;
            }
        }
        return false;
    }
    public synchronized boolean deleteBill(UUID billId) {
        boolean ok = model.bills.removeIf(b -> b.id.equals(billId));
        if (ok) save();
        return ok;
    }

    public File getFile() { return file; }
}