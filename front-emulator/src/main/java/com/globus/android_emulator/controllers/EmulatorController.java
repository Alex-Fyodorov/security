package com.globus.android_emulator.controllers;

import com.globus.android_emulator.dto.*;
import com.globus.android_emulator.services.EmulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmulatorController {
    private final EmulatorService emulatorService;

    @PostMapping("/registration")
    public OtpResponse registration(@RequestBody BiometricRegisterRequest request) {
        return emulatorService.registration(request);
    }

    @PostMapping("/sms")
    public BiometricSettingsResponse smsCode(@RequestBody BiometricRegisterRequest request) {

        return emulatorService.sms(request);
    }

    @PostMapping("/auth")
    public JwtResponse auth(@RequestBody BiometricAuthRequest request) {
        return emulatorService.auth(request);
    }
    
    @GetMapping("/check/{userId}")
    public BiometricSettingsResponse check(@PathVariable int userId) {
        return emulatorService.check(userId);
    }

    @PostMapping("/device_status")
    public DeviceDto changeDeviceStatus(@RequestBody DeviceStatusChangeRequest request) {
        return emulatorService.changeStatus(request);
    }
}
