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
        NEW("NEW"),
        DOCUMENTATION("DOCUMENTATION"),
        CONSTRUCTION("CONSTRUCTION"),
        COMPLETION("COMPLETION"),
        CLOSED("CLOSED");

        private final String value;

        StatusName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static StatusName fromValue(String value) {
            for (StatusName status : values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Неизвестный статус: " + value);
        }

        // Добавьте этот метод!
        @Override
        public String toString() {
            return this.value;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private StatusName name;

    @Column(name = "description")
    private String description;

    // Relations
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatus> orderStatuses = new ArrayList<>();
}