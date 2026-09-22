package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.CareerScopeMismatchException;
import com.umss.sigesa.domain.exception.IndicatorHasEvidenceException;
import com.umss.sigesa.domain.exception.IndicatorIncompleteException;
import com.umss.sigesa.domain.exception.CcAlreadyAssignedToProcessException;
import com.umss.sigesa.domain.exception.InvalidResponsibleUserException;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.ProcessHasEvidenceException;
import com.umss.sigesa.domain.exception.ProcessNotDeletableException;
import com.umss.sigesa.domain.exception.ProcessNotEditableException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProcessStructureOrderConflictException;
import com.umss.sigesa.domain.exception.ProgramNotFoundException;
import com.umss.sigesa.domain.exception.InvalidLevel1StateException;
import com.umss.sigesa.domain.exception.InvalidStageStateException;
import com.umss.sigesa.domain.exception.Level1ClosureBlockedException;
import com.umss.sigesa.domain.exception.StageGateBlockedException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.PendingIndicator;
import com.umss.sigesa.domain.exception.TemplateIndicatorIncompleteException;
import com.umss.sigesa.domain.exception.TemplateInUseException;
import com.umss.sigesa.domain.exception.TemplateNotEditableException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotPublishedException;
import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.adapter.in.web.dto.PendingIndicatorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.umss.sigesa.adapter.in.web")
public class ProcessExceptionHandler {

    @ExceptionHandler(ProcessAlreadyActiveException.class)
    public ResponseEntity<Map<String, String>> handleProcessAlreadyActive(ProcessAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "PROCESS_ALREADY_ACTIVE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ProgramNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProgramNotFound(ProgramNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "PROGRAM_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTemplateNotFound(TemplateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "TEMPLATE_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ProcessNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProcessNotFound(ProcessNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "PROCESS_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ProcessHasEvidenceException.class)
    public ResponseEntity<Map<String, String>> handleProcessHasEvidence(ProcessHasEvidenceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "PROCESS_HAS_EVIDENCE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ProcessNotDeletableException.class)
    public ResponseEntity<Map<String, String>> handleProcessNotDeletable(ProcessNotDeletableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "PROCESS_NOT_DELETABLE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateNotPublishedException.class)
    public ResponseEntity<Map<String, String>> handleTemplateNotPublished(TemplateNotPublishedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "TEMPLATE_NOT_PUBLISHED",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateStructureIncompleteException.class)
    public ResponseEntity<Map<String, String>> handleTemplateStructureIncomplete(
            TemplateStructureIncompleteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "TEMPLATE_STRUCTURE_INCOMPLETE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateOrderConflictException.class)
    public ResponseEntity<Map<String, String>> handleTemplateOrderConflict(TemplateOrderConflictException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "TEMPLATE_ORDER_CONFLICT",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateInUseException.class)
    public ResponseEntity<Map<String, String>> handleTemplateInUse(TemplateInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "TEMPLATE_IN_USE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateNotEditableException.class)
    public ResponseEntity<Map<String, String>> handleTemplateNotEditable(TemplateNotEditableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "TEMPLATE_NOT_EDITABLE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateIndicatorIncompleteException.class)
    public ResponseEntity<Map<String, String>> handleTemplateIndicatorIncomplete(
            TemplateIndicatorIncompleteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "TEMPLATE_INDICATOR_INCOMPLETE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ProcessNotEditableException.class)
    public ResponseEntity<Map<String, String>> handleProcessNotEditable(ProcessNotEditableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "PROCESS_NOT_EDITABLE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(IndicatorHasEvidenceException.class)
    public ResponseEntity<Map<String, String>> handleIndicatorHasEvidence(IndicatorHasEvidenceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "INDICATOR_HAS_EVIDENCE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(IndicatorIncompleteException.class)
    public ResponseEntity<Map<String, String>> handleIndicatorIncomplete(IndicatorIncompleteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "INDICATOR_INCOMPLETE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ProcessStructureOrderConflictException.class)
    public ResponseEntity<Map<String, String>> handleProcessStructureOrderConflict(
            ProcessStructureOrderConflictException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "PROCESS_STRUCTURE_ORDER_CONFLICT",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(CcAlreadyAssignedToProcessException.class)
    public ResponseEntity<Map<String, String>> handleCcAlreadyAssigned(CcAlreadyAssignedToProcessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "CC_ALREADY_ASSIGNED_TO_PROCESS",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(CareerScopeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleCareerScopeMismatch(CareerScopeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "CAREER_SCOPE_MISMATCH",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(InvalidLevel1StateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidLevel1State(InvalidLevel1StateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "INVALID_STATE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(InvalidStageStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStageState(InvalidStageStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "INVALID_STATE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(StageNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleStageNotFound(StageNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "STAGE_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(StageGateBlockedException.class)
    public ResponseEntity<Map<String, Object>> handleStageGateBlocked(StageGateBlockedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "STAGE_GATE_BLOCKED");
        body.put("message", ex.getMessage());
        body.put("failedRules", ex.getFailedRules());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Level1ClosureBlockedException.class)
    public ResponseEntity<Map<String, Object>> handleLevel1ClosureBlocked(Level1ClosureBlockedException ex) {
        List<PendingIndicatorResponseDto> pending = ex.getPendingIndicators().stream()
                .map(ProcessExceptionHandler::toPendingIndicatorDto)
                .toList();
        Map<String, Object> body = new HashMap<>();
        body.put("error", "NIVEL1_CIERRE_BLOQUEADO");
        body.put("message", ex.getMessage());
        body.put("pendingIndicators", pending);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidResponsibleUserException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResponsibleUser(InvalidResponsibleUserException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "INVALID_RESPONSIBLE_USER",
                        "message", ex.getMessage()
                ));
    }

    private static PendingIndicatorResponseDto toPendingIndicatorDto(PendingIndicator pending) {
        PendingIndicatorResponseDto dto = new PendingIndicatorResponseDto();
        dto.setIndicatorId(pending.indicatorId());
        dto.setCode(pending.code());
        dto.setName(pending.name());
        dto.setStatus(pending.status().name());
        dto.setOrder(pending.order());
        return dto;
    }
}
