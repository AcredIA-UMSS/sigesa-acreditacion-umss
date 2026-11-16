package com.umss.sigesa.adapter.in.web.advice;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/api/v1/_error-probe")
public class ErrorContractProbeController {

    public record ProbeRequest(@NotBlank(message = "El nombre es obligatorio") String name) {
    }

    @PostMapping("/echo")
    public Map<String, String> echo(@Valid @RequestBody ProbeRequest body) {
        return Map.of("name", body.name());
    }

    @GetMapping("/boom")
    public void boom() {
        throw new RuntimeException("SELECT password FROM users WHERE secret='leak'");
    }

    @GetMapping("/typed")
    public Map<String, String> typed(@RequestParam UUID id) {
        return Map.of("id", id.toString());
    }
}
