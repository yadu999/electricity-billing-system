package com.ebilling.backend.model;

import java.io.Serializable;

public class Settings implements Serializable {
    public double ratePerUnit = 6.5;   // currency per unit
    public double fixedCharge = 50.0;  // currency
    public double taxPercent = 10.0;   // %

    public Settings() {}
    public Settings(Settings other) {
        if (other != null) {
            this.ratePerUnit = other.ratePerUnit;
            this.fixedCharge = other.fixedCharge;
            this.taxPercent = other.taxPercent;
        }
    }
}