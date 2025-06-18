package com.globus.biometric_auth_service.impl;

import com.globus.biometric_auth_service.dto.*;
import com.globus.biometric_auth_service.exception.IllegalAuthStateException;
import com.globus.biometric_auth_service.exception.UserNotFoundException;
import com.globus.biometric_auth_service.model.BiometricSettings;
import com.globus.biometric_auth_service.model.Device;
import com.globus.biometric_auth_service.repository.BiometricSettingsRepository;
import com.globus.biometric_auth_service.service.DeviceService;
import com.globus.biometric_auth_service.service.LoginManager;
import com.globus.biometric_auth_service.service.OtpService;
import com.globus.biometric_auth_service.service.SmsService;
import com.globus.biometric_auth_service.service.impl.BiometricAuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BiometricAuthServiceImplTest {
    @Mock
    private BiometricSettingsRepository settingsRepository;
    @Mock
    private LoginManager loginManager;
    @Mock
    private DeviceService deviceService;
    @Mock
    private OtpService otpService;
    @Mock
    private SmsService smsService;
    @InjectMocks
    private BiometricAuthServiceImpl biometricAuthService;

    @Test
    void enableBiometricAuth_InvalidOtp_ThrowsException() {
        BiometricRegisterRequest request = new BiometricRegisterRequest(
                "deviceInfo", 1,"1234567890","invalidOtp"
        );
        when(otpService.validateOtp(request.phoneNumber(), request.otp())).thenReturn(false);

        assertThrows(IllegalAuthStateException.class,
                () -> biometricAuthService.enableBiometricAuth(request));
        verify(otpService).validateOtp(request.phoneNumber(), request.otp());
    }

    @Test
    void enableBiometricAuth_NewUser_CreatesSettings() {
        BiometricRegisterRequest request = new BiometricRegisterRequest(
                "deviceInfo", 1,"1234567890","validOtp"
        );
        when(otpService.validateOtp(anyString(), anyString())).thenReturn(true);
        when(settingsRepository.findByUserId(request.userId())).thenReturn(Optional.empty());

        BiometricSettings savedSettings = new BiometricSettings();
        savedSettings.setUserId(request.userId());
        savedSettings.setDevices(new ArrayList<>());
        when(settingsRepository.save(any())).thenReturn(savedSettings);

        BiometricSettingsResponse response = biometricAuthService.enableBiometricAuth(request);

        assertNotNull(response);
        verify(settingsRepository).save(any());
        verify(settingsRepository).findByUserId(request.userId());
    }

    @Test
    void getBiometricAuthStatus_UserNotFound_ThrowsException() {
        int userId = 1;
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> biometricAuthService.getBiometricAuthStatus(userId));
    }

    @Test
    void getBiometricAuthStatus_ValidUser_ReturnsSettings() {
        Integer userId = 1;
        BiometricSettings settings = new BiometricSettings();
        Device device = new Device();
        settings.setDevices(Collections.singletonList(device));
        device.setAccount(settings);
        settings.setUserId(userId);
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

        BiometricSettingsResponse response = biometricAuthService.getBiometricAuthStatus(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
    }

    @Test
    @Transactional
    void biometricAuthLogin_InvalidDevice_ThrowsException() {
        BiometricAuthRequest request = new BiometricAuthRequest("unknownDevice",1 , true);
        BiometricSettings settings = new BiometricSettings();
        settings.setUserId(request.userId());
        settings.setDevices(Collections.emptyList());

        when(settingsRepository.findByUserId(request.userId())).thenReturn(Optional.of(settings));

        assertThrows(IllegalAuthStateException.class,
                () -> biometricAuthService.biometricAuthLogin(request));
    }

    @Test
    @Transactional
    void biometricAuthLogin_BlockedUser_ThrowsException() {
        BiometricAuthRequest request = new BiometricAuthRequest("validDevice",1, true);
        BiometricSettings settings = new BiometricSettings();
        settings.setUserId(request.userId());
        Device device = new Device();
        device.setDeviceInfo("validDevice");
        device.setBiometricEnabled(true);
        settings.setDevices(List.of(device));

        when(settingsRepository.findByUserId(request.userId())).thenReturn(Optional.of(settings));
        when(loginManager.isBlocked(request.userId())).thenReturn(true);

        assertThrows(IllegalAuthStateException.class,
                () -> biometricAuthService.biometricAuthLogin(request));
    }

    @Test
    @Transactional
    void biometricAuthLogin_Success_ReturnsUserDetails() {
        BiometricAuthRequest request = new BiometricAuthRequest("validDevice",1,true);
        BiometricSettings settings = new BiometricSettings();
        settings.setUserId(request.userId());
        settings.setLastUsed(null);
        Device device = new Device();
        device.setDeviceInfo("validDevice");
        device.setBiometricEnabled(true);
        settings.setDevices(List.of(device));

        when(settingsRepository.findByUserId(request.userId())).thenReturn(Optional.of(settings));
        when(loginManager.isBlocked(request.userId())).thenReturn(false);
        when(settingsRepository.save(settings)).thenReturn(settings);

        UserDetails userDetails = biometricAuthService.biometricAuthLogin(request);

        assertNotNull(userDetails);
        assertEquals(String.valueOf(request.userId()), userDetails.getUsername());
        verify(loginManager).resetAttempts(request.userId());
    }

    @Test
    void requestBiometricAuth_SendsOtp() {
        BiometricRegisterRequest request = new BiometricRegisterRequest(
                "deviceInfo", 1,"1234567890","validOtp"
        );
        String otp = "123456";
        when(otpService.generateOtp(request.phoneNumber())).thenReturn(otp);

        String result = biometricAuthService.requestBiometricAuth(request);
        System.out.println(result);
        assertEquals("OTP sent successfully: " + otp, result);
        verify(smsService).sendSms(request.phoneNumber(), "Your OTP is: " + otp);
    }

    @Test
    void changeDeviceEnableStatus_UserNotFound_ThrowsException() {
        DeviceStatusChangeRequest request = new DeviceStatusChangeRequest("deviceInfo",1,true);
        when(settingsRepository.findIdByUserId(request.userId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> biometricAuthService.changeDeviceEnableStatus(request));
    }

    @Test
    void changeDeviceEnableStatus_Success_ReturnsDeviceDto() {
        DeviceStatusChangeRequest request = new DeviceStatusChangeRequest("deviceInfo",1,true);
        Integer settingsId = 100;
        Device device = new Device();
        BiometricSettings settings = new BiometricSettings();
        device.setAccount(settings);
        when(settingsRepository.findIdByUserId(request.userId())).thenReturn(Optional.of(settingsId));
        when(deviceService.changeDeviceEnableStatus(
                settingsId, request.deviceInfo(), request.enabled()
        )).thenReturn(device);

        DeviceDto result = biometricAuthService.changeDeviceEnableStatus(request);

        assertNotNull(result);
        verify(deviceService).changeDeviceEnableStatus(
                settingsId, request.deviceInfo(), request.enabled()
        );
    }
}