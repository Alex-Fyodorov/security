package com.globus.biometric_auth_service.repository;

import com.globus.biometric_auth_service.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByAccountIdAndDeviceInfo(Integer accountId, String deviceInfo);
}
