package com.globus.biometric_auth_service.service.impl;

import com.globus.biometric_auth_service.dto.*;
import com.globus.biometric_auth_service.exception.IllegalAuthStateException;
import com.globus.biometric_auth_service.exception.UserNotFoundException;
import com.globus.biometric_auth_service.mapper.BiometricSettingsMapper;
import com.globus.biometric_auth_service.model.BiometricSettings;
import com.globus.biometric_auth_service.model.Device;
import com.globus.biometric_auth_service.repository.BiometricSettingsRepository;
import com.globus.biometric_auth_service.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BiometricAuthServiceImpl implements BiometricAuthService {
    private final BiometricSettingsRepository settingsRepository;
    private final LoginManager loginManager;
    private final DeviceService deviceService;
    private final OtpService otpService;
    private final SmsService smsService;

    @Override
    public BiometricSettingsResponse enableBiometricAuth(BiometricRegisterRequest request) {
        if (!otpService.validateOtp(request.phoneNumber(), request.otp())) {
            throw new IllegalAuthStateException("Invalid OTP");
        }

        Optional<BiometricSettings> settingsOPtional = findByUserId(request.userId());
        BiometricSettings settings = null;
        settings = settingsOPtional.orElseGet(() -> BiometricSettings.builder()
                .userId(request.userId())
                .devices(new ArrayList<>())
                .build());
        Device device = Device.builder()
                .account(settings)
                .deviceInfo(request.deviceInfo())
                .biometricEnabled(true)
                .build();
        settings.getDevices().add(device);
        return BiometricSettingsMapper.getSettingsDto(settingsRepository.save(settings));
    }

    @Override
    public BiometricSettingsResponse getBiometricAuthStatus(Integer userId) {
        BiometricSettings settings = findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with Id " + userId + " not found"));
        return BiometricSettingsMapper.getSettingsDto(settings);
    }

    @Override
    @Transactional
    public UserDetails biometricAuthLogin(BiometricAuthRequest request) {
        BiometricSettings settings = findByUserId(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User with Id " + request.userId() + " not found"));
        checkDevice(settings, request.deviceInfo());
        checkFailedAttempts(request);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                String.valueOf(request.userId()), "null", getAuthority(List.of("ROLE_USER")));
        settings.setLastUsed(null);
        settingsRepository.save(settings);
        return userDetails;
    }

    @Override
    public Optional<BiometricSettings> findByUserId(Integer userId) {
        return settingsRepository.findByUserId(userId);
    }

    @Override
    public String requestBiometricAuth(BiometricRegisterRequest request) {
        String otp = otpService.generateOtp(request.phoneNumber());
        smsService.sendSms(request.phoneNumber(), "Your OTP is: " + otp);
        return "OTP sent successfully: " + otp;
    }

    @Override
    @Transactional
    public DeviceDto changeDeviceEnableStatus (DeviceStatusChangeRequest request) {
        Integer id = settingsRepository.findIdByUserId(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User with Id " + request.userId() + " not found"));
        Device device = deviceService.changeDeviceEnableStatus(id, request.deviceInfo(), request.enabled());
        return BiometricSettingsMapper.getDeviceDto(device);
    }

    private void checkDevice(BiometricSettings settings, String deviceInfo) {
        for (Device device : settings.getDevices()) {
            if (device.getDeviceInfo().equals(deviceInfo)) {
                if (!device.getBiometricEnabled()) {
                    throw new IllegalAuthStateException("У данного устройства выключена аутентификации с помощью биометрии.");
                }
                return;
            }
        }
        throw new IllegalAuthStateException("Данное устройство не зарегистрировано в системе аутентификации с помощью биометрии.");
    }

    private void checkFailedAttempts(BiometricAuthRequest request) {
        if (loginManager.isBlocked(request.userId())) {
            throw new IllegalAuthStateException("Too many invalid requests. Try again later.");
        }

        if (!request.authenticated()) {
            loginManager.incrementAttempts(request.userId());
            throw new IllegalAuthStateException("Failed login. Try again.");
        }

        loginManager.resetAttempts(request.userId());
    }

    private Collection<? extends GrantedAuthority> getAuthority(List<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
