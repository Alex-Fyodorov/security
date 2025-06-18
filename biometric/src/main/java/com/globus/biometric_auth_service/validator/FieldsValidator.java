package com.globus.biometric_auth_service.validator;

import com.globus.biometric_auth_service.dto.BiometricAuthRequest;
import com.globus.biometric_auth_service.dto.BiometricRegisterRequest;
import com.globus.biometric_auth_service.exception.ValidationException;
import com.globus.biometric_auth_service.model.BiometricSettings;
import com.globus.biometric_auth_service.model.Device;
import com.globus.biometric_auth_service.service.BiometricAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FieldsValidator {
    private final BiometricAuthService biometricAuthService;

    public void registerValidate(BiometricRegisterRequest request) {
        Optional<BiometricSettings> settings = biometricAuthService.findByUserId(request.userId());
        if (settings.isPresent()) {
            Optional<Device> device = settings.get().getDevices().stream().filter(d -> d.getDeviceInfo().equals(request.deviceInfo())).findFirst();
            if (device.isPresent()) {
                throw new ValidationException(String.format("Учётная запись для этого устройства и пользователя с id: %d уже существует.", request.userId()));
            }
        }
        if (request.userId() == null) {
            throw new ValidationException("Нет информации о userId.");
        }
        if (request.deviceInfo() == null || request.deviceInfo().isBlank()) {
            throw new ValidationException("Нет информации об устройстве.");
        }
    }

    public void authValidate(BiometricAuthRequest request) {
        if (request.userId() == null) {
            throw new ValidationException("Нет информации о userId.");
        }
        if (request.deviceInfo() == null || request.deviceInfo().isBlank()) {
            throw new ValidationException("Нет информации об устройстве.");
        }
    }
}
