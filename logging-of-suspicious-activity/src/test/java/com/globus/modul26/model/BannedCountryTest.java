package com.globus.modul26.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BannedCountryTest {

    private BannedCountry country;

    @BeforeEach
    void setUp() {
        country = new BannedCountry();
    }

    @Test
    void getCountry() {
        country.setCountry("USA");
        assertEquals("USA", country.getCountry());
    }

    @Test
    void setCountry() {
        country.setCountry("UKR");
        assertEquals("UKR", country.getCountry());
    }

    @Test
    void testConstructorWithCountry() {
        BannedCountry bc = new BannedCountry("POL");
        assertEquals("POL", bc.getCountry());
    }
}