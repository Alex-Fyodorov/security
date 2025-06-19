package com.globus.biometric_auth_service.service.impl;

import com.globus.biometric_auth_service.dto.*;
import com.globus.biometric_auth_service.exception.IllegalAuthStateException;
import com.globus.biometric_auth_service.exception.UserNotFoundException;
import com.globus.biometric_auth_service.integration.SessionTracingIntegrations;
import com.globus.biometric_auth_service.mapper.BiometricSettingsMapper;
import com.globus.biometric_auth_service.model.BiometricSettings;
import com.globus.biometric_auth_service.model.BiometryType;
import com.globus.biometric_auth_service.model.Device;
import com.globus.biometric_auth_service.repository.BiometricSettingsRepository;
import com.globus.biometric_auth_service.service.*;
import com.globus.biometric_auth_service.util.Base64Service;
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
    private final SessionTracingIntegrations sessionTracingIntegrations;

    /**
     * Enables biometric authentication for a user after OTP validation.
     * Registers a new device if none exists for the user.
     *
     * @param request Registration request containing user ID, phone number, OTP, and device info
     * @return Biometric settings response with updated device configuration
     * @throws IllegalAuthStateException if OTP validation fails
     */
    @Override
    public BiometricSettingsResponse enableBiometricAuth(BiometricRegisterRequest request) {
        if (!otpService.validateOtp(request.phoneNumber(), request.otp())) {
            throw new IllegalAuthStateException("Invalid OTP");
        }

        BiometricSettings settings = findByUserId(request.userId()).orElseGet(() -> BiometricSettings.builder()
                .userId(request.userId())
                .devices(new ArrayList<>())
                .build());
        Device device = Device.builder()
                .account(settings)
                .deviceInfo(request.deviceInfo())
                .biometricEnabled(true)
                .biometryType(BiometryType.valueOf(request.biometricType()))
                .build();
        settings.getDevices().add(device);
        return BiometricSettingsMapper.getSettingsDto(settingsRepository.save(settings));
    }

    /**
     * Retrieves the current biometric authentication status for a user.
     *
     * @param userId ID of the user to check
     * @return Current biometric settings configuration
     * @throws UserNotFoundException if no settings exist for the user
     */
    @Override
    public BiometricSettingsResponse getBiometricAuthStatus(Integer userId) {
        BiometricSettings settings = findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with Id " + userId + " not found"));
        return BiometricSettingsMapper.getSettingsDto(settings);
    }

    /**
     * Authenticates a user via biometric credentials. Resets failed attempt counter on success.
     *
     * @param request Authentication request containing user ID and device info
     * @return Spring Security UserDetails object for authenticated user
     * @throws UserNotFoundException if user settings are not found
     * @throws IllegalAuthStateException for device/authentication failures or blocked user
     */
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
        sessionTracingIntegrations.login(request.userId(), request.deviceInfo(), "biometric", "111.222.333.444");
        return userDetails;
    }

    @Override
    public Optional<BiometricSettings> findByUserId(Integer userId) {
        return settingsRepository.findByUserId(userId);
    }

    /**
     * Initiates biometric registration by generating and sending OTP via SMS.
     *
     * @param request Registration request containing phone number
     * @return Success message with OTP generation status
     */
    @Override
    public String requestBiometricAuth(BiometricRegisterRequest request) {
        String otp = otpService.generateOtp(request.phoneNumber());
        smsService.sendSms(request.phoneNumber(), "Your OTP is: " + Base64Service.decode(otp) );
        return "OTP sent successfully: " + Base64Service.decode(otp);
    }

    /**
     * Changes biometric authentication enablement status for a specific device.
     *
     * @param request Device status change request containing user ID, device info, and enable flag
     * @return DTO with updated device information
     * @throws UserNotFoundException if user settings are not found
     */
    @Override
    @Transactional
    public DeviceDto changeDeviceEnableStatus (DeviceStatusChangeRequest request) {
        Integer id = settingsRepository.findIdByUserId(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User with Id " + request.userId() + " not found"));
        Device device = deviceService.changeDeviceEnableStatus(id, request.deviceInfo(), request.enabled());
        return BiometricSettingsMapper.getDeviceDto(device);
    }

    /**
     * Validates a device's registration and biometric enablement status.
     *
     * @param settings User's biometric settings
     * @param deviceInfo Target device identifier to validate
     * @throws IllegalAuthStateException for unregistered or disabled devices
     */
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

    /**
     * Checks failed login attempts and blocks users exceeding threshold.
     *
     * @param request Authentication request containing user ID
     * @throws IllegalAuthStateException for blocked users or failed authentication
     */
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

    /**
     * Converts role strings to Spring Security authorities.
     *
     * @param roles List of role strings (e.g., "ROLE_USER")
     * @return Collection of GrantedAuthority objects
     */
    private Collection<? extends GrantedAuthority> getAuthority(List<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
