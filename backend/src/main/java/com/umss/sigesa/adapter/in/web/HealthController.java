package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Liveness del proceso Spring (público, sin JWT)")
public class HealthController {

    @GetMapping
    @Operation(summary = "Liveness: HTTP 200 con {\"status\":\"UP\"} sin autenticación")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }
}
