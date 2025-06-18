package com.globus.biometric_auth_service.service;

import com.globus.biometric_auth_service.exception.ResourceNotFoundException;
import com.globus.biometric_auth_service.model.Device;
import com.globus.biometric_auth_service.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    @Transactional
    public Device changeDeviceEnableStatus(Integer accountId, String deviceInfo, boolean status) {
        Device device = deviceRepository.findByAccountIdAndDeviceInfo(accountId, deviceInfo)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Device not found. Account id: %d, device info: %s", accountId, deviceInfo)));
        device.setBiometricEnabled(status);
        return deviceRepository.save(device);
    }
}
