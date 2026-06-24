package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.application.port.in.CreateAccreditationProcessUseCase;
import com.umss.sigesa.adapter.in.web.dto.CreateProcessRequest;
import com.umss.sigesa.adapter.in.web.dto.ProcessResponse;
import com.umss.sigesa.domain.model.AccreditationProcess;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/processes")
public class AccreditationProcessController {

    private final CreateAccreditationProcessUseCase createProcessUseCase;

    public AccreditationProcessController(CreateAccreditationProcessUseCase createProcessUseCase) {
        this.createProcessUseCase = createProcessUseCase;
    }

    @PostMapping
    public ResponseEntity<ProcessResponse> createProcess(@RequestBody CreateProcessRequest request) {
        AccreditationProcess process = createProcessUseCase.create(
                request.templateId(),
                request.careerId(),
                request.period(),
                request.type()
        );

        // Mapeo manual (o mediante MapStruct) de Dominio a DTO para no exponer el modelo interno
        ProcessResponse response = new ProcessResponse(
                process.getId(),
                process.getStatus(),
                process.getTaxonomySnapshotVersion(),
                process.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
