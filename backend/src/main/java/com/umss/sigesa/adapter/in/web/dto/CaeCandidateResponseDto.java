package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "Usuario CAE_MEMBER candidato a delegación")
public class CaeCandidateResponseDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
}
