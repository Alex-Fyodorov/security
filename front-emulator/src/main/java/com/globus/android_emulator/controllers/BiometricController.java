package com.globus.android_emulator.controllers;

import com.globus.android_emulator.dto.*;
import com.globus.android_emulator.integrations.BiometricServiceIntegrations;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/biometric")
public class BiometricController {
    public final BiometricServiceIntegrations biometricServiceIntegrations;

    @PostMapping("/registration")
    public OtpResponse registration(@RequestBody BiometricRegisterRequest request) {
        return biometricServiceIntegrations.registration(request);
    }

    @PostMapping("/sms")
    public BiometricSettingsResponse smsCode(@RequestBody BiometricRegisterRequest request) {
        return biometricServiceIntegrations.sms(request);
    }

    @PostMapping("/auth")
    public JwtResponse auth(@RequestBody BiometricAuthRequest request) {
        return biometricServiceIntegrations.auth(request);
    }
    
    @GetMapping("/check/{userId}")
    public BiometricSettingsResponse check(@PathVariable int userId) {
        return biometricServiceIntegrations.check(userId);
    }

    @PostMapping("/device_status")
    public DeviceDto changeDeviceStatus(@RequestBody DeviceStatusChangeRequest request) {
        return biometricServiceIntegrations.changeStatus(request);
    }
}
