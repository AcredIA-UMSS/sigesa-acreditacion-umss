package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.ProcessNotEditableException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProcessStructureOrderConflictException;
import com.umss.sigesa.domain.exception.ProgramNotFoundException;
import com.umss.sigesa.domain.exception.SubphaseHasEvidenceException;
import com.umss.sigesa.domain.exception.SubphaseLinkRequiredException;
import com.umss.sigesa.domain.exception.TemplateInUseException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotPublishedException;
import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.exception.TemplateSubphaseLinkRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(TemplateNotPublishedException.class)
    public ResponseEntity<Map<String, String>> handleTemplateNotPublished(TemplateNotPublishedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "TEMPLATE_NOT_PUBLISHED",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(TemplateSubphaseLinkRequiredException.class)
    public ResponseEntity<Map<String, String>> handleTemplateSubphaseLinkRequired(
            TemplateSubphaseLinkRequiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "TEMPLATE_SUBPHASE_LINK_REQUIRED",
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

    @ExceptionHandler(ProcessNotEditableException.class)
    public ResponseEntity<Map<String, String>> handleProcessNotEditable(ProcessNotEditableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "PROCESS_NOT_EDITABLE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(SubphaseHasEvidenceException.class)
    public ResponseEntity<Map<String, String>> handleSubphaseHasEvidence(SubphaseHasEvidenceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "SUBPHASE_HAS_EVIDENCE",
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

    @ExceptionHandler(SubphaseLinkRequiredException.class)
    public ResponseEntity<Map<String, String>> handleSubphaseLinkRequired(SubphaseLinkRequiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "SUBPHASE_LINK_REQUIRED",
                        "message", ex.getMessage()
                ));
    }
}
