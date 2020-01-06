package com.synopsys.integration.stepworkflow;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;

public class StepWorkflowTest {
    public static final Integer DATA = 1;

    @Test
    public void testSuccessfulSingleStepDataStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(SubStep.ofSupplier(this::successfulDataSupplier))
                                                     .run();

        Assert.assertTrue(response.wasSuccessful());
        Assert.assertEquals(DATA, response.getData());
        Assert.assertNull(response.getException());
    }

    @Test
    public void testSuccessfulSingleStepDatalessStepWorkflow() {
        StepWorkflowResponse response = StepWorkflow.just(SubStep.ofExecutor(this::successfulExecutor))
                                            .run();

        Assert.assertTrue(response.wasSuccessful());
        Assert.assertNull(response.getData());
        Assert.assertNull(response.getException());
    }

    @Test
    public void testUnsuccessfulSingleStepStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(SubStep.ofSupplier(this::unsuccessfulDataSupplier))
                                                     .run();

        Assert.assertFalse(response.wasSuccessful());
        Assert.assertNull(response.getData());
        Assert.assertNotNull(response.getException());
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
