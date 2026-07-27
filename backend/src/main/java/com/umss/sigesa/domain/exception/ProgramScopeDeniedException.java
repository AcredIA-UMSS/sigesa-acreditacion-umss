package com.umss.sigesa.domain.exception;

public class ProgramScopeDeniedException extends RuntimeException {

    public ProgramScopeDeniedException() {
        super("Program scope denied for indicator");
    }
}
