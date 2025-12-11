package ru.hackathon.mos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "web_camera")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebCamera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", nullable = false, length = 45) // IPv6 может быть до 45 символов
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(name = "stream_url", length = 512)
    private String streamUrl; // RTSP или HTTP поток

    @Column(name = "login", length = 64)
    private String login;

    @Column(name = "password", length = 128)
    private String password;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "location", length = 256)
    private String location; // "Вид на фасад", "Вид на задний двор" и т.д.

    @Column(name = "last_check")
    private java.time.LocalDateTime lastCheck;

    @Column(name = "status", length = 20)
    private String status; // "online", "offline", "error"
}