package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.function.ThrowingConsumer;
import com.synopsys.integration.function.ThrowingFunction;

public class StepWorkflowResponseTest {

    private final String data = "Test Data";
    private final Exception exception = new RuntimeException("Test exception");

    @Test
    public void testStepWorkflowResponseTrue() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);

        assertEquals(true, stepWorkflowResponse.wasSuccessful());
        assertEquals(data, stepWorkflowResponse.getData());
        assertEquals(data, stepWorkflowResponse.getDataOrThrowException());
        assertEquals(exception, stepWorkflowResponse.getException());
    }

    @Test
    public void testStepWorkflowResponseFalse() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(false, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);

        assertEquals(false, stepWorkflowResponse.wasSuccessful());
        assertEquals(data, stepWorkflowResponse.getData());
        assertEquals(exception, stepWorkflowResponse.getException());
        assertThrows(RuntimeException.class, () -> { stepWorkflowResponse.getDataOrThrowException(); });
    }

    @Test
    public void testStepWorkflowResponseHandleResponse() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);
        ThrowingFunction throwingFunctionImp = new ThrowingFunctionImp();

        assertEquals(data, stepWorkflowResponse.handleResponse(throwingFunctionImp));
    }

    @Test
    public void testStepWorkflowResponseConsumeResponse() throws Throwable {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);
        StepWorkflowResponse<String> stepWorkflowResponse = new StepWorkflowResponse<>(subStepResponse);
        ThrowingConsumer throwingConsumerMock = Mockito.mock(ThrowingConsumer.class);
        stepWorkflowResponse.consumeResponse(throwingConsumerMock);

        Mockito.verify(throwingConsumerMock, times(1)).accept(stepWorkflowResponse);
    }

    public class ThrowingFunctionImp implements ThrowingFunction<StepWorkflowResponse, String, Exception> {

        @Override
        public String apply(StepWorkflowResponse stepWorkflowResponse) throws Exception {
            return (String) stepWorkflowResponse.getData();
        }
    }
}
