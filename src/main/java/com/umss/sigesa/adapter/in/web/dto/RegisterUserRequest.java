package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record RegisterUserRequest(String email, String role, UUID programId) {
}
