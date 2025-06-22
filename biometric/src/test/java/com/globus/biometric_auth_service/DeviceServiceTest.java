package com.globus.biometric_auth_service;

import com.globus.biometric_auth_service.exception.ResourceNotFoundException;
import com.globus.biometric_auth_service.model.Device;
import com.globus.biometric_auth_service.repository.DeviceRepository;
import com.globus.biometric_auth_service.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {
    @Mock
    private DeviceRepository deviceRepository;
    @InjectMocks
    private DeviceService deviceService;

    @Test
    void changeDeviceEnableStatus_deviceFound_updatesAndSavesDevice() {
        Integer accountId = 1;
        String deviceInfo = "iPhone12";
        boolean newStatus = true;
        Device mockDevice = new Device();
        mockDevice.setBiometricEnabled(!newStatus);

        when(deviceRepository.findByAccountIdAndDeviceInfo(accountId, deviceInfo))
                .thenReturn(Optional.of(mockDevice));
        when(deviceRepository.save(mockDevice)).thenReturn(mockDevice);

        Device result = deviceService.changeDeviceEnableStatus(accountId, deviceInfo, newStatus);

        assertSame(mockDevice, result, "Should return the saved device");
        assertTrue(mockDevice.getBiometricEnabled(), "Status should be updated to true");
        verify(deviceRepository).save(mockDevice);
    }

    @Test
    void changeDeviceEnableStatus_deviceNotFound_throwsException() {
        Integer accountId = 1;
        String deviceInfo = "UnknownDevice";
        boolean status = true;

        when(deviceRepository.findByAccountIdAndDeviceInfo(accountId, deviceInfo))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> deviceService.changeDeviceEnableStatus(accountId, deviceInfo, status)
        );

        String expectedMessage = String.format(
                "Device not found. Account id: %d, device info: %s",
                accountId, deviceInfo
        );
        assertEquals(expectedMessage, exception.getMessage());
        verify(deviceRepository, never()).save(any());
    }
}