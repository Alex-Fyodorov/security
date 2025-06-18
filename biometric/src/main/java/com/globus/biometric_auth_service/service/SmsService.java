package com.globus.biometric_auth_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    public void sendSms(String to, String message) {
       log.info("Sending SMS with message {} to {}", message, to);
    }
}
