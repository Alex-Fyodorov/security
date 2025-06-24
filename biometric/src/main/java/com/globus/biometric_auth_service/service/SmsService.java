package com.globus.biometric_auth_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private final Logger log = LoggerFactory.getLogger(SmsService.class);

    public void sendSms(String to, String message) {
       log.info("Sending SMS with message {} to {}", message, to);
    }
}
