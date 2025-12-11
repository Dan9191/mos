package ru.hackathon.mos.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_stage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typeId", nullable = false)
    private OrderStageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stageChangedBy", nullable = false)
    private User changedBy;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "plannedEndDate")
    private LocalDateTime plannedEndDate;

    @Column(name = "completionDate")
    private LocalDateTime completionDate;

    @Column(name = "isCompleted", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "progress")
    private Integer progress = 0;

    @Column(name = "notes", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}