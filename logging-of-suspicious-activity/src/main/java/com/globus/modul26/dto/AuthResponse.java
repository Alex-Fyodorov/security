package com.globus.modul26.dto;

public class AuthResponse {
    private String token;
    private String email;             // email пользователя
    private boolean biometryUsed;     // добавили это поле!

    public AuthResponse(String token, String email, boolean biometryUsed) {
        this.token = token;
        this.email = email;
        this.biometryUsed = biometryUsed;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isBiometryUsed() {
        return biometryUsed;
    }
    public void setBiometryUsed(boolean biometryUsed) {
        this.biometryUsed = biometryUsed;
    }
}