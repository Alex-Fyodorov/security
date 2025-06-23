package com.globus.biometric_auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.globus.biometric_auth_service.dto.BiometricAuthRequest;
import com.globus.biometric_auth_service.dto.BiometricRegisterRequest;
import com.globus.biometric_auth_service.dto.BiometricSettingsResponse;
import com.globus.biometric_auth_service.dto.DeviceDto;
import com.globus.biometric_auth_service.dto.DeviceStatusChangeRequest;
import com.globus.biometric_auth_service.dto.JwtResponse;
import com.globus.biometric_auth_service.dto.OtpResponse;
import com.globus.biometric_auth_service.service.BiometricAuthService;
import com.globus.biometric_auth_service.util.JwtTokenUtil;
import com.globus.biometric_auth_service.validator.FieldsValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BiometricAuthControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FieldsValidator fieldsValidator;
    @Mock
    private BiometricAuthService biometricAuthService;
    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private BiometricAuthController biometricAuthController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(biometricAuthController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    void enableBiometricAuth_validRequest_returnsSettings() throws Exception {
        BiometricRegisterRequest request = new BiometricRegisterRequest("deviceInfo", 1, "1234567", "1234567", "FINGERPRINT");
        BiometricSettingsResponse response = new BiometricSettingsResponse();
        response.setId(1);
        response.setUserId(1);
        response.setLastUsed(LocalDateTime.of(2024, 7, 1, 12,1));
        response.setDevices(List.of(new DeviceDto()));
        doNothing().when(fieldsValidator).registerValidate(request);
        when(biometricAuthService.enableBiometricAuth(request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/biometric/enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request))
                        .header("Authorization", "Bearer token")
                        .header("X-Request-ID", "req123")
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    @Test
    void requestBiometricAuth_validRequest_returnsOtp() throws Exception {
        // Setup
        BiometricRegisterRequest request = new BiometricRegisterRequest("deviceInfo", 1, "1234567", "1234567", "FINGERPRINT");
        OtpResponse response = new OtpResponse("123456");
        String otp = "123456";

        doNothing().when(fieldsValidator).registerValidate(request);
        when(biometricAuthService.requestBiometricAuth(request)).thenReturn(otp);

        // Execute & Verify
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/biometric/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request))
                        .header("X-Request-ID", "req456")
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    @Test
    void biometricAuthLogin_validRequest_returnsJwt() throws Exception {
        // Setup
        BiometricAuthRequest request = new BiometricAuthRequest("deviceInfo", 1, true);
        UserDetails userDetails = mock(UserDetails.class);
        JwtResponse response = new JwtResponse("jwt.token.here");

        doNothing().when(fieldsValidator).authValidate(request);
        when(biometricAuthService.biometricAuthLogin(request)).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn(response.jwt());

        // Execute & Verify
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/biometric/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request))
                        .header("Authorization", "Basic auth")
                        .header("X-Request-ID", "req789"))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    @Test
    void getBiometricAuthStatus_validUserId_returnsStatus() throws Exception {
        // Setup
        Integer userId = 12345;
        BiometricSettingsResponse response = new BiometricSettingsResponse();

        when(biometricAuthService.getBiometricAuthStatus(userId)).thenReturn(response);

        // Execute & Verify
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/biometric/status")
                        .param("userId", userId.toString())
                        .header("Accept-Language", "en-US")
                        .header("X-Request-ID", "req101"))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    @Test
    void changeDeviceEnableStatus_validRequest_returnsDeviceDto() throws Exception {
        // Setup
        DeviceStatusChangeRequest request = new DeviceStatusChangeRequest("deviceInfo", 1, true);
        DeviceDto response = new DeviceDto(1, 1, "deviceInfo", true, "FINGERPRINT");

        when(biometricAuthService.changeDeviceEnableStatus(request)).thenReturn(response);

        // Execute & Verify
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/biometric/device_enabled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }
}