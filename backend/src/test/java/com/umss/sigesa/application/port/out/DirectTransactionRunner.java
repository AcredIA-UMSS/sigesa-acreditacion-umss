package com.umss.sigesa.application.port.out;

import java.util.function.Supplier;

/** Ejecuta el bloque en el hilo actual sin TX real (unit tests). */
public final class DirectTransactionRunner implements TransactionRunnerPort {

    @Override
    public <T> T required(Supplier<T> action) {
        return action.get();
    }

    @Override
    public void required(Runnable action) {
        action.run();
    }

    @Override
    public <T> T readOnly(Supplier<T> action) {
        return action.get();
    }
}
