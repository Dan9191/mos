package ru.hackathon.mos.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_stage_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStageType {

    public enum StageName {
        FOUNDATION("foundation"),
        WALLS("walls"),
        ROOF("roof"),
        FINISHING("finishing");

        private final String value;

        StageName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        // Добавляем метод для преобразования строки в enum
        public static StageName fromValue(String value) {
            for (StageName stage : values()) {
                if (stage.value.equalsIgnoreCase(value)) {
                    return stage;
                }
            }
            throw new IllegalArgumentException("Неизвестный этап: " + value);
        }

        // Добавляем toString для возврата строкового значения
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
    private StageName name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Relations
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStage> orderStages = new ArrayList<>();
}