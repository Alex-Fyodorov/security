package com.globus.session_tracing.utils;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class Base64Service {

    public static String encode(String plainText) {
        return Base64.getEncoder().encodeToString(plainText.getBytes());
    }

    public static String decode(String encodedText) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedText);
        return new String(decodedBytes);
    }
}