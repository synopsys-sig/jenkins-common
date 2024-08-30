package com.blackduck.integration.stepworkflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testStepWorkflowResponseFalse() {
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

        stepWorkflowResponseValidation(stepWorkflowResponse);
    }

    @Test
    public void testStepWorkflowResponseConsumeResponse() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);

        stepWorkflowResponse.consumeResponse(this::stepWorkflowResponseValidation);

        stepWorkflowResponseValidation(stepWorkflowResponse);
    }

    private void stepWorkflowResponseValidation(StepWorkflowResponse<String> stepWorkflowResponse) throws Throwable {
        assertTrue(stepWorkflowResponse.wasSuccessful());
        assertEquals(data, stepWorkflowResponse.getData());
        assertEquals(data, stepWorkflowResponse.getDataOrThrowException());
        assertEquals(exception, stepWorkflowResponse.getException());
    }

}
