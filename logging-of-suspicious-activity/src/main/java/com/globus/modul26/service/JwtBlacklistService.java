package com.globus.modul26.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {
    // Потокобезопасный set для хранения jti заблокированных токенов
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public void blacklist(String jti) {
        blacklist.add(jti);
    }

    public boolean isBlacklisted(String jti) {
        return blacklist.contains(jti);
    }
}