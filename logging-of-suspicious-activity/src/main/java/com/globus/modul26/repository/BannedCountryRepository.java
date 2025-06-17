package com.globus.modul26.repository;

import com.globus.modul26.model.BannedCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannedCountryRepository extends JpaRepository<BannedCountry, String> {

}