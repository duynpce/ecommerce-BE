package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.example.ticketservice.application.usecase.StartBuyingProcedureUseCase;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartBuyingProcedureService implements StartBuyingProcedureUseCase {

    /**
     * Process key defined in buying-items-procedure.bpmn
     * (bpmn:process id="Process_0fsfzgs")
     */
    private static final String PROCESS_KEY = "Process_0fsfzgs";

    private final RuntimeService runtimeService;

    @Override
    public void start(UUID transactionId, UUID contributorId, UUID customerId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("transactionId",  transactionId.toString());
        variables.put("contributorId",  contributorId.toString());
        variables.put("customerId",     customerId.toString());
        variables.put("retry",          0);

        // Use transactionId as the business key so tasks can be looked up by it
        runtimeService.startProcessInstanceByKey(PROCESS_KEY, transactionId.toString(), variables);

        log.info("[buying-items] Started Camunda process '{}' for transactionId={}", PROCESS_KEY, transactionId);
    }
}
