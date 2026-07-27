package com.umss.sigesa.domain.exception;

public class ReportAccessDeniedException extends RuntimeException {

    public ReportAccessDeniedException() {
        super("Access denied to report job");
    }
}
