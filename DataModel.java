package com.ebilling.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataModel implements Serializable {
    public List<Customer> customers = new ArrayList<>();
    public List<Bill> bills = new ArrayList<>();
    public Settings settings = new Settings();
}