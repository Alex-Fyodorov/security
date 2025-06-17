package com.globus.modul26.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "security_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_int")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_type", nullable = false, length = 45)
    private String eventType;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;



    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "biometry_used")
    private Boolean biometryUsed;

    //  Добавленное поле
    @Column(name = "is_suspicious", nullable = false)
    private Boolean isSuspicious = false;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isSuspicious == null) {
            this.isSuspicious = false;
        }
        // проверка айпи адреса. если неваоидный пишем  UNKNOWN
        if (this.ipAddress == null || this.ipAddress.trim().isEmpty() ||
                this.ipAddress.equalsIgnoreCase("null") ||
                this.ipAddress.equals("0:0:0:0:0:0:0:1") ||
                this.ipAddress.equals("127.0.0.1")) {
            this.ipAddress = "UNKNOWN";
        }

    }
}