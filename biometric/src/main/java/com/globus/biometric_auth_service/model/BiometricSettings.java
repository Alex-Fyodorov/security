package com.globus.biometric_auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "biometric_settings")
public class BiometricSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Device> devices;

    @Column(name = "last_used")
    @UpdateTimestamp
    private LocalDateTime lastUsed;
}
