package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.ApproveIndicatorResponse;
import com.umss.sigesa.adapter.in.web.dto.RejectIndicatorRequest;
import com.umss.sigesa.adapter.in.web.dto.RejectIndicatorResponse;
import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators")
public class IndicatorWorkflowController {

    private final ApproveIndicatorUseCase approveIndicatorUseCase;
    private final RejectIndicatorUseCase rejectIndicatorUseCase;
    private final WebIdentityResolver identityResolver;

    public IndicatorWorkflowController(ApproveIndicatorUseCase approveIndicatorUseCase,
                                       RejectIndicatorUseCase rejectIndicatorUseCase,
                                       WebIdentityResolver identityResolver) {
        this.approveIndicatorUseCase = approveIndicatorUseCase;
        this.rejectIndicatorUseCase = rejectIndicatorUseCase;
        this.identityResolver = identityResolver;
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApproveIndicatorResponse> approve(@PathVariable UUID id) {
        ApproveIndicatorUseCase.ApproveIndicatorResult result =
                approveIndicatorUseCase.approve(id, identityResolver.requireIdentity());

        return ResponseEntity.ok(new ApproveIndicatorResponse(
                result.newState(),
                result.stateHistoryId(),
                result.event()
        ));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RejectIndicatorResponse> reject(@PathVariable UUID id,
                                                          @Valid @RequestBody RejectIndicatorRequest request) {
        RejectIndicatorUseCase.RejectIndicatorResult result =
                rejectIndicatorUseCase.reject(id, request.justification(), identityResolver.requireIdentity());

        return ResponseEntity.ok(new RejectIndicatorResponse(
                result.newState(),
                result.observationId(),
                result.stateHistoryId()
        ));
    }
}
