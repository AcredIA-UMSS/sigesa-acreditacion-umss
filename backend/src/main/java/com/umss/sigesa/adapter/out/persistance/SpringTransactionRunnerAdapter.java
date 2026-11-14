package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class SpringTransactionRunnerAdapter implements TransactionRunnerPort {

    private final TransactionTemplate requiredTemplate;
    private final TransactionTemplate readOnlyTemplate;

    public SpringTransactionRunnerAdapter(PlatformTransactionManager transactionManager) {
        this.requiredTemplate = new TransactionTemplate(transactionManager);
        this.readOnlyTemplate = new TransactionTemplate(transactionManager);
        this.readOnlyTemplate.setReadOnly(true);
    }

    @Override
    public <T> T required(Supplier<T> action) {
        return requiredTemplate.execute(status -> action.get());
    }

    @Override
    public void required(Runnable action) {
        requiredTemplate.executeWithoutResult(status -> action.run());
    }

    @Override
    public <T> T readOnly(Supplier<T> action) {
        return readOnlyTemplate.execute(status -> action.get());
    }
}
