package com.umss.sigesa.domain.exception;

public class ReportNotReadyException extends RuntimeException {

    public ReportNotReadyException() {
        super("Report is not ready for download");
    }
}
