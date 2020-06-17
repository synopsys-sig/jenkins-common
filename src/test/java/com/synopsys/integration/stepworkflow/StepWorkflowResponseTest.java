package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StepWorkflowResponseTest {

    private final String data = "Test Data";
    private final Exception exception = new RuntimeException("Test exception");

    @Test
    public void testStepWorkflowResponseTrue() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);

        stepWorkflowResponseValidation(stepWorkflowResponse);
    }

    @Test
    public void testStepWorkflowResponseFalse() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(false, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);

        assertFalse(stepWorkflowResponse.wasSuccessful());
        assertEquals(data, stepWorkflowResponse.getData());
        assertThrows(RuntimeException.class, stepWorkflowResponse::getDataOrThrowException);
        assertEquals(exception, stepWorkflowResponse.getException());
    }

    @Test
    public void testStepWorkflowResponseHandleResponse() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);

        assertEquals(data, stepWorkflowResponse.handleResponse(StepWorkflowResponse::getData));
    }

    @Test
    public void testStepWorkflowResponseConsumeResponse() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);

        stepWorkflowResponse.consumeResponse(this::stepWorkflowResponseValidation);
    }

    private void stepWorkflowResponseValidation(StepWorkflowResponse<String> stepWorkflowResponse) throws Exception {
        assertTrue(stepWorkflowResponse.wasSuccessful());
        assertEquals(data, stepWorkflowResponse.getData());
        assertEquals(data, stepWorkflowResponse.getDataOrThrowException());
        assertEquals(exception, stepWorkflowResponse.getException());
    }

}
