package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;

public class StepWorkflowTest {
    public static final Integer DATA = 1;

    @Test
    public void testSuccessfulSingleStepDataStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(SubStep.ofSupplier(this::successfulDataSupplier))
                                                     .run();

        assertTrue(response.wasSuccessful());
        assertEquals(DATA, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testSuccessfulSingleStepDatalessStepWorkflow() {
        StepWorkflowResponse<Object> response = StepWorkflow.just(SubStep.ofExecutor(this::successfulExecutor))
                                                    .run();

        assertTrue(response.wasSuccessful());
        assertNull(response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testUnsuccessfulSingleStepStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(SubStep.ofSupplier(this::unsuccessfulDataSupplier))
                                                     .run();

        assertFalse(response.wasSuccessful());
        assertNull(response.getData());
        assertNotNull(response.getException());
    }

    private void successfulExecutor() {
        // No body, should always succeed.
    }

    private Integer successfulDataSupplier() {
        return DATA;
    }

    private Integer unsuccessfulDataSupplier() throws IntegrationException {
        throw new IntegrationException();
    }

}
