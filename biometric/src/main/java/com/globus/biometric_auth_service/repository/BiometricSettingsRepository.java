package com.globus.biometric_auth_service.repository;

import com.globus.biometric_auth_service.model.BiometricSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BiometricSettingsRepository extends JpaRepository<BiometricSettings, Integer> {

    @Query("select bs from BiometricSettings bs join fetch bs.devices where bs.userId = :userId")
    Optional<BiometricSettings> findByUserId(int userId);

    @Query("select bs.id from BiometricSettings bs where bs.userId = :userId")
    Optional<Integer> findIdByUserId(int userId);
}
