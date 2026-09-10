package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** Compatibility alias for the earlier lower-camel-case BPMN expression. */
@Component("cancelTransactionDelegate")
public class CancelTransactionDelegate implements JavaDelegate {

    private final TransactionCanceledDelegate delegate;

    public CancelTransactionDelegate(TransactionCanceledDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(DelegateExecution execution) {
        delegate.execute(execution);
    }
}
