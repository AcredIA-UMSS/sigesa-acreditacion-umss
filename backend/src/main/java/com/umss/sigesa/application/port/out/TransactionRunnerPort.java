package com.umss.sigesa.application.port.out;

import java.util.function.Supplier;

/**
 * Delimita transacciones cortas desde la capa de aplicación sin anotar casos de uso
 * con {@code @Transactional} (hexagonal). El adaptador infra usa {@code TransactionTemplate}.
 */
public interface TransactionRunnerPort {

    <T> T required(Supplier<T> action);

    void required(Runnable action);

    <T> T readOnly(Supplier<T> action);
}
