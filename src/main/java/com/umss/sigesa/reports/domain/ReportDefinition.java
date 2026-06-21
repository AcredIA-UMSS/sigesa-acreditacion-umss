package com.umss.sigesa.reports.domain;

import jakarta.persistence.*;
import lombok.*;
import com.umss.sigesa.reports.config.MapToJsonConverter;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.Convert;

@Entity
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

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "text")
    private Map<String, Object> audiences;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "filters_allowed", columnDefinition = "text")
    private Map<String, Object> filtersAllowed;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "text")
    private Map<String, Object> metrics;

    private Integer version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
