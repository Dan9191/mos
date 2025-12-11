package ru.hackathon.mos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

    public enum TypeName {
        CONTRACT("contract"),
        ESTIMATE("estimate"),
        SITE_PLAN("site_plan"),
        ACCEPTANCE_CERTIFICATE("acceptance_certificate"),
        PERMIT("permit"),
        TECHNICAL_DOCUMENTATION("technical_documentation");

        private final String value;

        TypeName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private TypeName name;

    @Column(name = "description")
    private String description;

    // Relations
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();
}