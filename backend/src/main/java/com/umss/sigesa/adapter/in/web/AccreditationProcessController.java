package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.application.port.in.CreateAccreditationProcessUseCase;
import com.umss.sigesa.adapter.in.web.dto.CreateProcessRequest;
import com.umss.sigesa.adapter.in.web.dto.ProcessResponse;
import com.umss.sigesa.adapter.in.web.dto.ProcessSummaryResponse;
import com.umss.sigesa.application.port.in.GetProcessUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes")
public class AccreditationProcessController {

    private final CreateAccreditationProcessUseCase createProcessUseCase;
    private final ListProcessesUseCase listProcessesUseCase;
    private final GetProcessUseCase getProcessUseCase;

    public AccreditationProcessController(CreateAccreditationProcessUseCase createProcessUseCase,
                                          ListProcessesUseCase listProcessesUseCase,
                                          GetProcessUseCase getProcessUseCase) {
        this.createProcessUseCase = createProcessUseCase;
        this.listProcessesUseCase = listProcessesUseCase;
        this.getProcessUseCase = getProcessUseCase;
    }

    @GetMapping
    public List<ProcessSummaryResponse> list(
            @RequestParam(required = false) ProcessStatus status,
            @RequestParam(required = false) UUID careerId,
            @RequestParam(required = false) String period) {
        return listProcessesUseCase.list(status, careerId, period).stream()
                .map(summary -> new ProcessSummaryResponse(
                        summary.processId(),
                        summary.templateId(),
                        summary.careerId(),
                        summary.period(),
                        summary.type(),
                        summary.status(),
                        summary.taxonomySnapshotVersion(),
                        summary.createdAt()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessSummaryResponse> getById(@PathVariable UUID id) {
        return getProcessUseCase.getById(id)
                .map(detail -> new ProcessSummaryResponse(
                        detail.processId(),
                        detail.templateId(),
                        detail.careerId(),
                        detail.period(),
                        detail.type(),
                        detail.status(),
                        detail.taxonomySnapshotVersion(),
                        detail.createdAt()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProcessResponse> createProcess(@RequestBody CreateProcessRequest request) {
        AccreditationProcess process = createProcessUseCase.create(
                request.templateId(),
                request.careerId(),
                request.period(),
                request.type()
        );

        ProcessResponse response = new ProcessResponse(
                process.getId(),
                process.getStatus(),
                process.getTaxonomySnapshotVersion(),
                process.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
