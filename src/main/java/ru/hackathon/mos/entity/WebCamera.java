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
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress; // переименовано из "ip" в "ip_address" чтобы соответствовать БД

    @Column(name = "port")
    private Integer port;
}