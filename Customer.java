package com.ebilling.backend.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Customer implements Serializable {
    public UUID id;
    public String name;
    public String meterNumber;
    public String address;
    public LocalDate createdAt;

    @Override
    public String toString() {
        return name + " (" + meterNumber + ")";
    }
}