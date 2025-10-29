package com.ebilling.backend.storage;

import com.ebilling.backend.model.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Storage {
    // Settings
    Settings getSettingsCopy();
    void updateSettings(double rate, double fixed, double taxPct);

    // Customers
    List<Customer> getCustomersCopy();
    Optional<Customer> getCustomer(UUID id);
    void addCustomer(Customer c);
    void updateCustomer(Customer c);
    boolean deleteCustomer(UUID id, boolean removeBills);

    // Bills
    List<Bill> getBillsCopy();
    List<Bill> getBillsByCustomer(UUID customerId);
    Optional<Bill> findBill(UUID customerId, YearMonth month);
    void addOrReplaceBill(Bill bill);
    boolean markBillPaid(UUID billId);
    boolean deleteBill(UUID billId);
}
