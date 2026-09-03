package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** Compatibility alias for process definitions that still use {@code cancelOrderDelegate}. */
@Component("cancelOrderDelegate")
public class CancleOrderDelegate implements JavaDelegate {

    private final SubOrderCanceledDelegate delegate;

    public CancleOrderDelegate(SubOrderCanceledDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(DelegateExecution execution) {
        delegate.execute(execution);
    }
}
