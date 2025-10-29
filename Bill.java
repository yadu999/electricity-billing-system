package com.ebilling.backend.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

public class Bill implements Serializable {
    public UUID id;
    public UUID customerId;
    public String customerSnapshot;
    public String meterSnapshot;

    public YearMonth month;
    public int units;

    public double ratePerUnit;
    public double fixedCharge;
    public double taxPercent;

    public double subtotal;
    public double tax;
    public double total;

    public BillStatus status;
    public LocalDate issueDate;
    public LocalDate dueDate;
    public LocalDate paidDate;
}