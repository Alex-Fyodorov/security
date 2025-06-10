package com.globus.android_emulator.services;

import com.globus.android_emulator.dto.*;
import com.globus.android_emulator.integrations.BiometricServiceIntegrations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmulatorService {
    public final BiometricServiceIntegrations biometricServiceIntegrations;

    public OtpResponse registration(BiometricRegisterRequest request) {
        return biometricServiceIntegrations.registration(request);
    }

    public BiometricSettingsResponse sms(BiometricRegisterRequest request) {
        return biometricServiceIntegrations.sms(request);
    }

    public JwtResponse auth(BiometricAuthRequest request) {
        return biometricServiceIntegrations.auth(request);
    }

    public BiometricSettingsResponse check(int userId) {
        return biometricServiceIntegrations.check(userId);
    }

    public DeviceDto changeStatus(DeviceStatusChangeRequest request) {
        return  biometricServiceIntegrations.changeStatus(request);
    }
}
