package com.globus.modul26.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "banned_countries")
public class BannedCountry {

    @Id
    private String country;

    public BannedCountry() { }

    public BannedCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}