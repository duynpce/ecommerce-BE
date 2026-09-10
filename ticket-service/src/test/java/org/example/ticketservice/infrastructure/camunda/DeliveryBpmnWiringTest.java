package org.example.ticketservice.infrastructure.camunda;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryBpmnWiringTest {

    @Test
    void buyingAndDeliveryModelsAreValidAndFullyWired() throws IOException {
        String buying = readResource("bpmn/buying-items-procedure.bpmn");
        String delivery = readResource("bpmn/delivery-process.bpmn");
        String returns = readResource("bpmn/returning-items.bpmn");
        String shipperApplication = readResource("bpmn/shipper-application-procedure.bpmn");
        String processes = readResource("META-INF/processes.xml");

        assertValidModel(buying);
        assertValidModel(delivery);
        assertValidModel(returns);
        assertValidModel(shipperApplication);

        assertTrue(buying.contains("calledElement=\"delivery-process\""));
        assertTrue(delivery.contains("<bpmn:process id=\"delivery-process\""));
        assertTrue(buying.contains("camunda:candidateGroups=\"SHIPPER\""));
        assertTrue(buying.contains(
                "id=\"reject-cancel\" name=\"reject sub-order\" "
                        + "camunda:delegateExpression=\"${subOrderRejectedDelegate}\""));
        assertTrue(buying.contains(
                "id=\"Flow_0x50fh5\" name=\"order rejected\" "
                        + "sourceRef=\"Gateway_034q4rj\" targetRef=\"reject-cancel\""));
        assertTrue(buying.contains(
                "id=\"cancel-sub-order\" name=\"cancel sub-order\" "
                        + "camunda:delegateExpression=\"${subOrderCanceledDelegate}\""));
        assertTrue(buying.contains(
                "id=\"transaction-cancel\" name=\"Cancel Transaction\" "
                        + "camunda:delegateExpression=\"${TransactionCanceledDelegate}\""));
        assertTrue(buying.contains(
                "id=\"reject-order\" name=\"Reject Transaction\" "
                        + "camunda:delegateExpression=\"${transactionRejectedDelegate}\""));
        assertTrue(buying.contains("name=\"All Sub-orders Accepted?\""));
        assertTrue(buying.contains("name=\"Run Snapshot Delivery\""));
        assertTrue(buying.contains("<camunda:in variables=\"all\" />"));
        assertTrue(buying.contains(
                "<camunda:in sourceExpression=\"${false}\" target=\"returnProcess\" />"));
        assertTrue(buying.contains("<camunda:out variables=\"all\" />"));
        assertTrue(delivery.contains("camunda:assignee=\"${shipperId}\""));
        assertTrue(returns.contains("camunda:assignee=\"${contributorId}\""));
        assertTrue(returns.contains("name=\"Accept Return for Pickup\" camunda:candidateGroups=\"SHIPPER\""));
        assertTrue(returns.contains("name=\"Run Return Delivery\" calledElement=\"delivery-process\""));
        assertTrue(returns.contains(
                "<camunda:in sourceExpression=\"${true}\" target=\"returnProcess\" />"));
        assertTrue(returns.contains("deliveryOutcome == \"RETRY\" &amp;&amp; retry &lt; 3"));
        assertTrue(returns.contains("deliveryOutcome == \"DELIVERY_FAILED\""));
        assertTrue(processes.contains("<resource>bpmn/delivery-process.bpmn</resource>"));
        assertTrue(shipperApplication.contains("name=\"Review Shipper Application\""));
        assertTrue(shipperApplication.contains("<bpmn:documentation>"));
        assertFalse(buying.contains("delegateExpression=\"\""));
        assertFalse(delivery.contains("delegateExpression=\"\""));
        assertFalse(buying.contains("calledElement=\"\""));
        assertFalse(returns.contains("calledElement=\"\""));
    }

    private void assertValidModel(String xml) {
        BpmnModelInstance model = Bpmn.readModelFromStream(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertDoesNotThrow(() -> Bpmn.validateModel(model));
    }

    private String readResource(String path) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Missing classpath resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
