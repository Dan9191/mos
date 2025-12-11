package ru.hackathon.mos.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_status_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusType {

    public enum StatusName {
        NEW("new"),
        DOCUMENTATION("documentation"),
        CONSTRUCTION("construction"),
        COMPLETION("completion"),
        CLOSED("closed");

        private final String value;

        StatusName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)  // Сохраняем как строку в БД
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private StatusName name;

    @Column(name = "description")
    private String description;

    // Relations
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatus> orderStatuses = new ArrayList<>();
}