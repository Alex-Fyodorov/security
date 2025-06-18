package com.globus.biometric_auth_service.mapper;

import com.globus.biometric_auth_service.dto.BiometricSettingsResponse;
import com.globus.biometric_auth_service.dto.DeviceDto;
import com.globus.biometric_auth_service.model.BiometricSettings;
import com.globus.biometric_auth_service.model.Device;

import java.util.ArrayList;

public class BiometricSettingsMapper {

    public static BiometricSettingsResponse getSettingsDto(BiometricSettings settings) {
        BiometricSettingsResponse response = new BiometricSettingsResponse();
        response.setId(settings.getId());
        response.setUserId(settings.getUserId());
        response.setLastUsed(settings.getLastUsed());
        response.setDevices(new ArrayList<>());
        for (Device device : settings.getDevices()) {
            response.getDevices().add(getDeviceDto(device));
        }
        return response;
    }

    public static DeviceDto getDeviceDto(Device device) {
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setId(device.getId());
        deviceDto.setAccountId(device.getAccount().getId());
        deviceDto.setDeviceInfo(device.getDeviceInfo());
        deviceDto.setBiometricEnabled(device.getBiometricEnabled());
        return deviceDto;
    }
}
