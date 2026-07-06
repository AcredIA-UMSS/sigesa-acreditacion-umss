package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "template")
@Getter
@Setter
@NoArgsConstructor
public class TemplateEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private boolean validated;

    @Column(name = "taxonomy_version", nullable = false, length = 50)
    private String taxonomyVersion;
}
