package com.umss.sigesa.domain.exception;

public class DelegationDeniedException extends RuntimeException {

    public DelegationDeniedException() {
        super("El indicador no está cubierto por una delegación CAE del usuario.");
    }
}
