package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.synopsys.integration.exception.IntegrationException;

public class StepWorkflowTest {
    public static final Integer DATA_ONE = 1;
    public static final Integer DATA_TWO = 2;
    public static final Integer DATA_THREE = 3;

    private static final IntegrationException integrationException = new IntegrationException("Exception #1");

    private static final SubStep<Object, Integer> subStepSupplierDataOne = SubStep.ofSupplier(() -> DATA_ONE);
    private static final SubStep<Object, Integer> subStepSupplierDataTwo = SubStep.ofSupplier(() -> DATA_TWO);
    private static final SubStep<Object, Integer> subStepSupplierDataThree = SubStep.ofSupplier(() -> DATA_THREE);
    private static final SubStep<Object, Object> subStepExecutorDataless = SubStep.ofExecutor(() -> {});
    private static final SubStep<Object, Integer> subStepSupplierException = SubStep.ofSupplier(() -> {throw integrationException;});

    private StepWorkflow.FlowController<Object, Integer> flowControllerDataOne;
    private StepWorkflow.Builder<Integer> successfulBuilderOfSupplierDataOne;
    private StepWorkflow.Builder<Integer> unsuccessfulBuilderOfSupplierException;

    @BeforeEach
    public void setup() {
        flowControllerDataOne = new StepWorkflow.FlowController<>(subStepSupplierDataOne);
        assertEquals(subStepSupplierDataOne, flowControllerDataOne.step);
        assertNull(flowControllerDataOne.next);
        assertNull(flowControllerDataOne.getResponse());

        successfulBuilderOfSupplierDataOne = StepWorkflow.first(subStepSupplierDataOne);
        assertEquals(subStepSupplierDataOne, successfulBuilderOfSupplierDataOne.start.step);
        assertEquals(subStepSupplierDataOne, successfulBuilderOfSupplierDataOne.end.step);
        assertNull(successfulBuilderOfSupplierDataOne.start.next);
        assertNull(successfulBuilderOfSupplierDataOne.end.next);

        unsuccessfulBuilderOfSupplierException = StepWorkflow.first(subStepSupplierException);
        assertEquals(subStepSupplierException, unsuccessfulBuilderOfSupplierException.start.step);
        assertEquals(subStepSupplierException, unsuccessfulBuilderOfSupplierException.end.step);
        assertNull(unsuccessfulBuilderOfSupplierException.start.next);
        assertNull(unsuccessfulBuilderOfSupplierException.end.next);
    }

    @Test
    public void testSuccessfulSingleStepDataStepWorkflow() throws Exception {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(subStepSupplierDataOne)
                                                     .run();

        assertTrue(response.wasSuccessful());
        assertEquals(DATA_ONE, response.getData());
        assertNull(response.getException());
        assertEquals(DATA_ONE, response.getDataOrThrowException());
    }

    @Test
    public void testSuccessfulSingleStepDatalessStepWorkflow() throws Exception {
        StepWorkflowResponse<Object> response = StepWorkflow.just(subStepExecutorDataless)
                                                    .run();

        assertTrue(response.wasSuccessful());
        assertNull(response.getData());
        assertNull(response.getException());
        assertNull(response.getDataOrThrowException());
    }

    @Test
    public void testUnsuccessfulSingleStepStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(subStepSupplierException)
                                                     .run();

        assertFalse(response.wasSuccessful());
        assertNull(response.getData());
        assertEquals(integrationException, response.getException());
        assertThrows(IntegrationException.class, response::getDataOrThrowException);
    }

    @Test
    public void testFlowControllerAppendAndConstructor() {
        StepWorkflow.FlowController<?, Integer> appendFlowController = flowControllerDataOne.append(subStepSupplierDataTwo);

        assertEquals(subStepSupplierDataOne, flowControllerDataOne.step);
        assertEquals(appendFlowController, flowControllerDataOne.next);
        assertNull(flowControllerDataOne.getResponse());
        assertEquals(subStepSupplierDataTwo, appendFlowController.step);
        assertEquals(subStepSupplierDataTwo, flowControllerDataOne.next.step);
        assertNull(appendFlowController.next);
        assertNull(appendFlowController.getResponse());
    }

    @Test
    public void testFlowControllerRunStepSuccess() {
        flowControllerDataOne.runStep(SubStepResponse.SUCCESS());
        SubStepResponse<Integer> response = flowControllerDataOne.getResponse();

        // Ensure that response is the only field that was changed as a result of runStep() being called
        assertEquals(subStepSupplierDataOne, flowControllerDataOne.step);
        assertNull(flowControllerDataOne.next);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.hasData());
        assertEquals(DATA_ONE, response.getData());
        assertFalse(response.hasException());
        assertNull(response.getException());
    }

    @Test
    public void testFlowControllerRunStepFailure() {
        flowControllerDataOne.runStep(SubStepResponse.FAILURE(integrationException));
        SubStepResponse<Integer> response = flowControllerDataOne.getResponse();

        // Ensure that response is the only field that was changed as a result of runStep() being called
        assertEquals(subStepSupplierDataOne, flowControllerDataOne.step);
        assertNull(flowControllerDataOne.next);
        assertNotNull(response);
        assertTrue(response.isFailure());
        assertFalse(response.hasData());
        assertTrue(response.hasException());
        assertEquals(integrationException, response.getException());
    }

    @Test
    public void testBuilderAndSometimes() {
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = successfulBuilderOfSupplierDataOne.andSometimes(subStepSupplierDataThree);

        assertEquals(subStepSupplierDataThree, andSometimesConditional.conditionalStepWorkflowBuilder.start.step);
        assertNull(andSometimesConditional.conditionalStepWorkflowBuilder.start.next);
        assertEquals(subStepSupplierDataOne, andSometimesConditional.parentBuilder.start.step);
        assertNull(andSometimesConditional.parentBuilder.start.next);
    }

    @Test
    public void testBuilderRun() {
        StepWorkflowResponse<Integer> response = successfulBuilderOfSupplierDataOne.run();

        assertTrue(response.wasSuccessful());
        assertEquals(DATA_ONE, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testConditionalConstructorAndThen() {
        StepWorkflow.ConditionalBuilder<Integer, Integer> conditionalBuilder = new StepWorkflow.ConditionalBuilder<>(successfulBuilderOfSupplierDataOne, subStepSupplierDataTwo);

        // Ensure that both start.next and end.next are both null for conditionalBuilder.conditionalStepWorkflowBuilder
        assertNull(conditionalBuilder.conditionalStepWorkflowBuilder.start.next);
        assertNull(conditionalBuilder.conditionalStepWorkflowBuilder.end.next);

        // Validate conditionalBuilder
        assertEquals(subStepSupplierDataTwo, conditionalBuilder.conditionalStepWorkflowBuilder.start.step);
        assertEquals(subStepSupplierDataOne, conditionalBuilder.parentBuilder.start.step);
        assertEquals(successfulBuilderOfSupplierDataOne, conditionalBuilder.parentBuilder);
        assertNull(conditionalBuilder.parentBuilder.start.next);
        assertNull(conditionalBuilder.parentBuilder.end.next);

        //conditionalBuilder.then(subStepSupplierDataSix);
        StepWorkflow.ConditionalBuilder<Integer, Integer> thenConditionalBuilder = conditionalBuilder.then(subStepSupplierDataThree);

        // Ensure that conditionalStepWorkflowBuilder.conditionalBuilder has been changed and both
        // start.next.step and end.next.step are both now equal to subStepFunctionDataFour after running then() for conditionalStepWorkflowBuilder
        assertEquals(subStepSupplierDataTwo, conditionalBuilder.conditionalStepWorkflowBuilder.start.step);
        assertEquals(subStepSupplierDataThree, conditionalBuilder.conditionalStepWorkflowBuilder.start.next.step);
        assertEquals(subStepSupplierDataTwo, conditionalBuilder.conditionalStepWorkflowBuilder.end.step);
        assertEquals(subStepSupplierDataThree, conditionalBuilder.conditionalStepWorkflowBuilder.end.next.step);

        // Ensure conditionalBuilder.parentBuilder has not changed since running then()
        assertEquals(subStepSupplierDataOne, conditionalBuilder.parentBuilder.start.step);
        assertEquals(successfulBuilderOfSupplierDataOne, conditionalBuilder.parentBuilder);
        assertNull(conditionalBuilder.parentBuilder.start.next);
        assertNull(conditionalBuilder.parentBuilder.end.next);

        // Validate thenConditionalBuilder
        assertEquals(successfulBuilderOfSupplierDataOne, thenConditionalBuilder.parentBuilder);
        assertEquals(conditionalBuilder.parentBuilder, thenConditionalBuilder.parentBuilder);
        assertEquals(subStepSupplierDataTwo, thenConditionalBuilder.conditionalStepWorkflowBuilder.start.step);
        assertEquals(subStepSupplierDataThree, thenConditionalBuilder.conditionalStepWorkflowBuilder.start.next.step);
        assertEquals(subStepSupplierDataThree, thenConditionalBuilder.conditionalStepWorkflowBuilder.end.step);
        assertEquals(subStepSupplierDataOne, thenConditionalBuilder.parentBuilder.start.step);
        assertNull(thenConditionalBuilder.parentBuilder.start.next);
        assertNull(thenConditionalBuilder.conditionalStepWorkflowBuilder.end.next);
    }

    @Test
    public void testConditionalButOnlyIf() {
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = successfulBuilderOfSupplierDataOne.andSometimes(subStepSupplierDataTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditional.butOnlyIf(new Object(), ignored -> true);

        assertEquals(subStepSupplierDataOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);
        assertEquals(butOnlyIfBuilder.start.next.step, butOnlyIfBuilder.end.step);
        assertNull(butOnlyIfBuilder.end.next);
    }

    @Test
    public void testRunConditionalWorkflowFailedPreviousResponse() {
        // Create and validate butOnlyIf() Builder
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = unsuccessfulBuilderOfSupplierException.andSometimes(subStepSupplierDataTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditional.butOnlyIf(new Object(), ignored -> true);
        assertEquals(subStepSupplierException, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);
        assertEquals(butOnlyIfBuilder.start.next.step, butOnlyIfBuilder.end.step);
        assertNull(butOnlyIfBuilder.end.next);

        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = butOnlyIfBuilder.then(subStepSupplierDataThree);
        assertEquals(subStepSupplierException, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(subStepSupplierDataThree, thenBuilder.end.step);
        assertEquals(subStepSupplierException, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next.step);
        assertNull(thenBuilder.end.next);

        // Run and validate build()
        StepWorkflow<Integer> buildStepWorkflow = thenBuilder.build();
        assertEquals(thenBuilder.start, buildStepWorkflow.start);
        assertEquals(thenBuilder.end, buildStepWorkflow.end);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = buildStepWorkflow.run();
        assertFalse(response.wasSuccessful());
        assertNull(response.getData());
        assertEquals(integrationException, response.getException());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testRunConditionalWorkflowSuccessPreviousResponse(Boolean predicateTest) {
        // Create and validate butOnlyIf() Builder
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = successfulBuilderOfSupplierDataOne.andSometimes(subStepSupplierDataTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditional.butOnlyIf(new Object(), ignored -> predicateTest);
        assertEquals(subStepSupplierDataOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);
        assertEquals(butOnlyIfBuilder.start.next.step, butOnlyIfBuilder.end.step);
        assertNull(butOnlyIfBuilder.end.next);

        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = butOnlyIfBuilder.then(subStepSupplierDataThree);
        assertEquals(subStepSupplierDataOne, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(subStepSupplierDataOne, thenBuilder.start.step);
        assertEquals(subStepSupplierDataThree, thenBuilder.end.step);
        assertNotNull(thenBuilder.start.next.step);
        assertNull(thenBuilder.end.next);

        // Run and validate build()
        StepWorkflow<Integer> buildStepWorkflow = thenBuilder.build();
        assertEquals(thenBuilder.start, buildStepWorkflow.start);
        assertEquals(thenBuilder.end, buildStepWorkflow.end);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = buildStepWorkflow.run();
        assertTrue(response.wasSuccessful());
        assertEquals(DATA_THREE, response.getData());
        assertNull(response.getException());
    }

}
