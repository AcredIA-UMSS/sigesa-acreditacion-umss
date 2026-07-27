package com.umss.sigesa.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProcessRequestDto {
    @NotNull
    @JsonProperty("career_id")
    private UUID careerId;

    @NotNull
    @JsonProperty("template_id")
    private UUID templateId;
}
