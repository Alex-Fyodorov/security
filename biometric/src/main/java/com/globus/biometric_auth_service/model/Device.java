package com.globus.biometric_auth_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private BiometricSettings account;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "biometric_enabled")
    private Boolean biometricEnabled;

    @Column(name = "biometry_type")
    @Enumerated(EnumType.STRING)
    private BiometryType biometryType;
}
