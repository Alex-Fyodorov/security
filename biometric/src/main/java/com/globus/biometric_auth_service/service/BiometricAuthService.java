package com.globus.biometric_auth_service.service;

import com.globus.biometric_auth_service.dto.*;
import com.globus.biometric_auth_service.model.BiometricSettings;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface BiometricAuthService {

    BiometricSettingsResponse enableBiometricAuth(BiometricRegisterRequest request);

    BiometricSettingsResponse getBiometricAuthStatus(Integer userId);

    UserDetails biometricAuthLogin(BiometricAuthRequest request);

    Optional<BiometricSettings> findByUserId(Integer userId);

    String requestBiometricAuth(BiometricRegisterRequest request);

    DeviceDto changeDeviceEnableStatus (DeviceStatusChangeRequest request);
}
