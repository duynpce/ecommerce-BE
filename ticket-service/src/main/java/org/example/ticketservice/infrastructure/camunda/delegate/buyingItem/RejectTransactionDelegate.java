package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** Compatibility alias for the earlier reject-transaction BPMN expression. */
@Component("rejectTransactionDelegate")
public class RejectTransactionDelegate implements JavaDelegate {

    private final TransactionRejectedDelegate delegate;

    public RejectTransactionDelegate(TransactionRejectedDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(DelegateExecution execution) {
        delegate.execute(execution);
    }
}
