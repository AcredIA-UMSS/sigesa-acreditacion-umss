package com.umss.sigesa.reports.domain;

import jakarta.persistence.*;
import lombok.*;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@TypeDef(name = "json", typeClass = JsonType.class)
@Table(name = "report_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "owner_role", length = 50)
    private String ownerRole;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> audiences;

    @Type(type = "json")
    @Column(name = "filters_allowed", columnDefinition = "jsonb")
    private Map<String, Object> filtersAllowed;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metrics;

    private Integer version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
