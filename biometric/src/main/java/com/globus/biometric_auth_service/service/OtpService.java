package com.globus.biometric_auth_service.service;

import com.globus.biometric_auth_service.util.Base64Service;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private final StringRedisTemplate redisTemplate;

    public String generateOtp(String phoneNumber) {
        String otp = Base64Service.encode(RandomStringUtils.randomNumeric(OTP_LENGTH));
        redisTemplate.opsForValue().set(
                phoneNumber,
                otp,
                Duration.ofMinutes(OTP_EXPIRY_MINUTES)
        );
        return otp;
    }

    public boolean validateOtp(String phoneNumber, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(phoneNumber);
        return otp != null && otp.equals(Base64Service.decode(storedOtp));
    }
}
