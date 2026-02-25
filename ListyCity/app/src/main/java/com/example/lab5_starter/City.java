package com.example.lab5_starter;

import java.io.Serializable;

public class City implements Serializable {
    private String name;
    private String province;

    // 1. ADD THIS REQUIRED FIRESTORE CONSTRUCTOR
    public City() {
    }

    // Existing constructor
    public City(String name, String province) {
        this.name = name;
        this.province = province;
    }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}