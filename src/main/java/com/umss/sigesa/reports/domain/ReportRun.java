package com.umss.sigesa.reports.domain;

import jakarta.persistence.*;
import lombok.*;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.TypeDef;

import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@TypeDef(name = "json", typeClass = JsonType.class)
@Entity
@Table(name = "report_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_definition_id")
    private Long reportDefinitionId;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> params;

    @Column(name = "status")
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Type(type = "json")
    @Column(name = "result_json", columnDefinition = "jsonb")
    private Map<String, Object> resultJson;

    @Type(type = "json")
    @Column(name = "result_metadata", columnDefinition = "jsonb")
    private Map<String, Object> resultMetadata;

    @Column(name = "download_url", length = 1000)
    private String downloadUrl;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
