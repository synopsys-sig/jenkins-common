package com.synopsys.integration.stepworkflow;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;

public class StepWorkflowTest {
    public static final Integer DATA = 1;

    @Test
    public void testSuccessfulSingleStepDataStepWorkflow() {
        final StepWorkflow<Integer> stepWorkflow = new StepWorkflow<>(new StepWorkflowController<Object, Integer>(SubStep.ofSupplier(this::successfulDataSupplier)));
        final StepWorkflowResponse<Integer> response = stepWorkflow.run();

        Assert.assertTrue(response.wasSuccessful());
        Assert.assertEquals(DATA, response.getData());
        Assert.assertNull(response.getException());
    }

    @Test
    public void testSuccessfulSingleStepDatalessStepWorkflow() {
        final StepWorkflow<Object> stepWorkflow = new StepWorkflow<>(new StepWorkflowController<Object, Object>(SubStep.ofExecutor(this::successfulExecutor)));
        final StepWorkflowResponse response = stepWorkflow.run();

        Assert.assertTrue(response.wasSuccessful());
        Assert.assertNull(response.getData());
        Assert.assertNull(response.getException());
    }

    @Test
    public void testUnsuccessfulSingleStepStepWorkflow() {
        final StepWorkflow<Integer> stepWorkflow = new StepWorkflow<>(new StepWorkflowController<Object, Integer>(SubStep.ofSupplier(this::unsuccessfulDataSupplier)));
        final StepWorkflowResponse<Integer> response = stepWorkflow.run();

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
