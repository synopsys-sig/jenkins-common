package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;

public class StepWorkflowTest {
    public static final Integer TEST_DATA_ONE = 1;
    public static final Integer TEST_DATA_TWO = 2;
    public static final Integer TEST_DATA_THREE = 3;
    public static final Integer TEST_DATA_FOUR = 4;
    public static final Integer TEST_DATA_FIVE = 5;

    private static final String exceptionMessage1 = "Exception #1";
    private static final IntegrationException exception = new IntegrationException(exceptionMessage1);

    private final SubStep<Object, Integer> successfulSupplierOne = SubStep.ofSupplier(this::successfulDataSupplierOne);
    private final SubStep<Object, Integer> successfulSupplierTwo = SubStep.ofSupplier(this::successfulDataSupplierTwo);
    private final SubStep<Object, Integer> successfulSupplierThree = SubStep.ofSupplier(this::successfulDataSupplierThree);
    private final SubStep<Object, Integer> unsuccessfulSupplierOne = SubStep.ofSupplier(this::unsuccessfulDataSupplier);

    private final SubStep<Object, Object> successfulExecutorOne = SubStep.ofExecutor(this::successfulExecutor);
    private final SubStep<Integer, Object> successfulFunctionOne = SubStep.ofFunction(this::successfulFunctionOne);
    private final SubStep<Integer, Object> successfulFunctionTwo = SubStep.ofFunction(this::successfulFunctionTwo);

    private final StepWorkflow.FlowController<Object, Integer> successfulFlowControllerOfSupplier = new StepWorkflow.FlowController<>(successfulSupplierOne);

    private final StepWorkflow.Builder<Integer> successfulBuilderOfSupplierOne = StepWorkflow.first(successfulSupplierOne);
    private final StepWorkflow.Builder<Integer> unsuccessfulBuilderOfSupplierOne = StepWorkflow.first(unsuccessfulSupplierOne);

    @Test
    public void testSuccessfulSingleStepDataStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(successfulSupplierOne)
                                                     .run();

        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_ONE, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testSuccessfulSingleStepDatalessStepWorkflow() {
        StepWorkflowResponse<Object> response = StepWorkflow.just(successfulExecutorOne)
                                                    .run();

        assertTrue(response.wasSuccessful());
        assertNull(response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testUnsuccessfulSingleStepStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(unsuccessfulSupplierOne)
                                                     .run();

        assertFalse(response.wasSuccessful());
        assertNull(response.getData());
        assertNotNull(response.getException());
    }

    @Test
    public void testSingleFlowControllerWithSuccessResponse() {
        // Validate FlowController before calling runStep()
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertNull(successfulFlowControllerOfSupplier.next);
        assertNull(successfulFlowControllerOfSupplier.response);
        assertNull(successfulFlowControllerOfSupplier.getResponse());

        // Call runStep() using SUCCESS SubStepResponse
        successfulFlowControllerOfSupplier.runStep(SubStepResponse.SUCCESS());

        // Validate FlowController after calling runStep()
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertNull(successfulFlowControllerOfSupplier.next);
        assertNotNull(successfulFlowControllerOfSupplier.response);
        assertNotNull(successfulFlowControllerOfSupplier.getResponse());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().isSuccess());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().hasData());
        assertEquals(TEST_DATA_ONE, successfulFlowControllerOfSupplier.getResponse().getData());
        assertFalse(successfulFlowControllerOfSupplier.getResponse().hasException());
        assertNull(successfulFlowControllerOfSupplier.getResponse().getException());
    }

    @Test
    public void testSingleFlowControllerWithFailedResponse() {
        // Validate FlowController before calling runStep()
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertNull(successfulFlowControllerOfSupplier.next);
        assertNull(successfulFlowControllerOfSupplier.response);
        assertNull(successfulFlowControllerOfSupplier.getResponse());

        // Call runStep() using FAILURE SubStepResponse
        successfulFlowControllerOfSupplier.runStep(SubStepResponse.FAILURE(exception));

        // Validate FlowController after calling runStep()
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertNull(successfulFlowControllerOfSupplier.next);
        assertNotNull(successfulFlowControllerOfSupplier.response);
        assertNotNull(successfulFlowControllerOfSupplier.getResponse());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().isFailure());
        assertFalse(successfulFlowControllerOfSupplier.getResponse().hasData());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().hasException());
        assertEquals(exception, successfulFlowControllerOfSupplier.getResponse().getException());
    }

    @Test
    public void testMultipleFlowControllerSuccessResponse() {
        // Append and validate second FlowController before calling runStep()
        StepWorkflow.FlowController<?, Integer> appendFlowController = successfulFlowControllerOfSupplier.append(successfulSupplierTwo);
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertEquals(appendFlowController, successfulFlowControllerOfSupplier.next);
        assertNull(successfulFlowControllerOfSupplier.response);
        assertNull(successfulFlowControllerOfSupplier.getResponse());
        assertEquals(successfulSupplierTwo, appendFlowController.step);
        assertNull(appendFlowController.next);
        assertNull(appendFlowController.response);
        assertNull(appendFlowController.getResponse());

        // Call runStep() using SUCCESS SubStepResponse
        successfulFlowControllerOfSupplier.runStep(SubStepResponse.SUCCESS());

        // Validate first FlowController after calling runStep()
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertEquals(appendFlowController, successfulFlowControllerOfSupplier.next);
        assertNotNull(successfulFlowControllerOfSupplier.response);
        assertNotNull(successfulFlowControllerOfSupplier.getResponse());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().isSuccess());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().hasData());
        assertEquals(TEST_DATA_ONE, successfulFlowControllerOfSupplier.getResponse().getData());
        assertFalse(successfulFlowControllerOfSupplier.getResponse().hasException());
        assertNull(successfulFlowControllerOfSupplier.getResponse().getException());

        // Validate second FlowController after calling runStep()
        assertEquals(successfulSupplierTwo, appendFlowController.step);
        assertNull(appendFlowController.next);
        assertNotNull(appendFlowController.response);
        assertNotNull(appendFlowController.getResponse());
        assertTrue(appendFlowController.getResponse().isSuccess());
        assertTrue(appendFlowController.getResponse().hasData());
        assertEquals(TEST_DATA_TWO, appendFlowController.getResponse().getData());
        assertFalse(appendFlowController.getResponse().hasException());
        assertNull(appendFlowController.getResponse().getException());
    }

    @Test
    public void testMultipleFlowControllerFailedResponse() {
        // Append and validate second FlowController before calling runStep()
        StepWorkflow.FlowController<?, Integer> appendFlowController = successfulFlowControllerOfSupplier.append(successfulSupplierTwo);
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertEquals(appendFlowController, successfulFlowControllerOfSupplier.next);
        assertNull(successfulFlowControllerOfSupplier.response);
        assertNull(successfulFlowControllerOfSupplier.getResponse());
        assertEquals(successfulSupplierTwo, appendFlowController.step);
        assertNull(appendFlowController.next);
        assertNull(appendFlowController.response);
        assertNull(appendFlowController.getResponse());

        // Call runStep() using FAILURE SubStepResponse
        successfulFlowControllerOfSupplier.runStep(SubStepResponse.FAILURE(exception));

        // Validate first FlowController after calling runStep()
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertEquals(appendFlowController, successfulFlowControllerOfSupplier.next);
        assertNotNull(successfulFlowControllerOfSupplier.response);
        assertNotNull(successfulFlowControllerOfSupplier.getResponse());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().isFailure());
        assertFalse(successfulFlowControllerOfSupplier.getResponse().hasData());
        assertTrue(successfulFlowControllerOfSupplier.getResponse().hasException());
        assertEquals(exception, successfulFlowControllerOfSupplier.getResponse().getException());

        // Validate second FlowController after calling runStep()
        assertEquals(successfulSupplierTwo, appendFlowController.step);
        assertNull(appendFlowController.next);
        assertNotNull(appendFlowController.response);
        assertNotNull(appendFlowController.getResponse());
        assertTrue(appendFlowController.getResponse().isFailure());
        assertFalse(appendFlowController.getResponse().hasData());
        assertTrue(appendFlowController.getResponse().hasException());
        assertEquals(exception, appendFlowController.getResponse().getException());
    }

    @Test
    public void testStepWorkflowStartAndEnd() {
        // Create and validate StepWorkflow
        StepWorkflow.FlowController<?, Integer> appendFlowController = successfulFlowControllerOfSupplier.append(successfulSupplierTwo);
        StepWorkflow<Integer> stepWorkflow = new StepWorkflow<>(successfulFlowControllerOfSupplier, appendFlowController);
        assertEquals(successfulSupplierOne, stepWorkflow.start.step);
        assertEquals(successfulSupplierOne, successfulFlowControllerOfSupplier.step);
        assertEquals(successfulSupplierTwo, stepWorkflow.end.step);
        assertEquals(successfulSupplierTwo, successfulFlowControllerOfSupplier.next.step);
        assertEquals(successfulSupplierTwo, appendFlowController.step);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = stepWorkflow.run();
        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_TWO, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testBuilderFirstStepOnly() {
        // Create and validate first() Builder
        assertEquals(successfulSupplierOne, successfulBuilderOfSupplierOne.start.step);
        assertEquals(successfulSupplierOne, successfulBuilderOfSupplierOne.end.step);
        assertNull(successfulBuilderOfSupplierOne.start.next);

        // Validate return from calling build()
        StepWorkflow<Integer> buildStepWorkflow = successfulBuilderOfSupplierOne.build();
        assertEquals(successfulSupplierOne, buildStepWorkflow.start.step);
        assertEquals(successfulSupplierOne, buildStepWorkflow.end.step);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = successfulBuilderOfSupplierOne.run();
        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_ONE, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testBuilderThenStep() {
        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenStepWorkflow = successfulBuilderOfSupplierOne.then(successfulSupplierTwo);
        assertEquals(successfulSupplierOne, successfulBuilderOfSupplierOne.start.step);
        assertEquals(successfulSupplierOne, successfulBuilderOfSupplierOne.end.step);
        assertEquals(successfulSupplierTwo, successfulBuilderOfSupplierOne.start.next.step);
        assertNull(successfulBuilderOfSupplierOne.end.next.next);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = thenStepWorkflow.run();
        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_TWO, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testBuilderAndSometimesStep() {
        // Create and validate andSometimes() Builder
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditionalBuilder = successfulBuilderOfSupplierOne.andSometimes(successfulSupplierTwo);
        StepWorkflow.Builder<Object> buildStepWorkflow = andSometimesConditionalBuilder.build(successfulFunctionOne);
        assertEquals(successfulSupplierOne, buildStepWorkflow.start.step);
        assertEquals(successfulFunctionOne, buildStepWorkflow.start.next.step);
        assertNull(buildStepWorkflow.start.next.next);
        assertEquals(successfulFunctionOne, buildStepWorkflow.end.step);
        assertNull(buildStepWorkflow.end.next);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Object> response = buildStepWorkflow.run();
        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_FOUR, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testConditionalButOnlyIfTrue() {
        // Create and validate butOnlyIf() Builder
        // butOnlyIf passed a "true" for execution
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditionalBuilder = successfulBuilderOfSupplierOne.andSometimes(successfulSupplierTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditionalBuilder.butOnlyIf(true, returnValue -> this.returnValue(returnValue));
        assertEquals(successfulSupplierOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);

        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = butOnlyIfBuilder.then(successfulSupplierThree);
        assertEquals(successfulSupplierOne, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(successfulSupplierThree, thenBuilder.end.step);

        // Run and validate build()
        StepWorkflow<Integer> buildStepWorkflow = thenBuilder.build();
        assertEquals(thenBuilder.start, buildStepWorkflow.start);
        assertEquals(thenBuilder.end, buildStepWorkflow.end);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = buildStepWorkflow.run();
        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_THREE, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testConditionalButOnlyIfFalse() {
        // Create and validate butOnlyIf() Builder
        // butOnlyIf passed a "false" for execution
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditionalBuilder = successfulBuilderOfSupplierOne.andSometimes(successfulSupplierTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditionalBuilder.butOnlyIf(false, returnValue -> this.returnValue(returnValue));
        assertEquals(successfulSupplierOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);

        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = butOnlyIfBuilder.then(successfulSupplierThree);
        assertEquals(successfulSupplierOne, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(successfulSupplierThree, thenBuilder.end.step);

        // Run and validate build()
        StepWorkflow<Integer> buildStepWorkflow = thenBuilder.build();
        assertEquals(thenBuilder.start, buildStepWorkflow.start);
        assertEquals(thenBuilder.end, buildStepWorkflow.end);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = buildStepWorkflow.run();
        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_THREE, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testConditionalFailedPreviousResponse() {
        // Create and validate first() Builder using unsuccessfulDataSupplier
        assertEquals(unsuccessfulSupplierOne, unsuccessfulBuilderOfSupplierOne.start.step);
        assertNull(unsuccessfulBuilderOfSupplierOne.start.next);
        assertEquals(unsuccessfulSupplierOne, unsuccessfulBuilderOfSupplierOne.end.step);

        // Create and validate butOnlyIf() Builder
        // butOnlyIf passed a "false" for execution
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditionalBuilder = unsuccessfulBuilderOfSupplierOne.andSometimes(successfulSupplierTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditionalBuilder.butOnlyIf(false, returnValue -> this.returnValue(returnValue));
        assertEquals(unsuccessfulSupplierOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);

        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = butOnlyIfBuilder.then(successfulSupplierThree);
        assertEquals(unsuccessfulSupplierOne, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(successfulSupplierThree, thenBuilder.end.step);

        // Run and validate build()
        StepWorkflow<Integer> buildStepWorkflow = thenBuilder.build();
        assertEquals(thenBuilder.start, buildStepWorkflow.start);
        assertEquals(thenBuilder.end, buildStepWorkflow.end);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Integer> response = buildStepWorkflow.run();
        assertFalse(response.wasSuccessful());
        assertNull(response.getData());
        assertEquals(exception, response.getException());
    }

    @Test
    public void testConditionalConstructorAndThen() {
        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = successfulBuilderOfSupplierOne.then(successfulSupplierTwo);
        assertEquals(successfulSupplierOne, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(successfulSupplierTwo, thenBuilder.end.step);

        // Create and validate build() Builder using conditionalBuilderOne.then() and ConditionalBuilder constructor
        StepWorkflow.ConditionalBuilder<Integer, Integer> conditionalBuilder = new StepWorkflow.ConditionalBuilder(thenBuilder, successfulSupplierThree);
        StepWorkflow.ConditionalBuilder<Integer, Object> thenConditionalBuilder = conditionalBuilder.then(successfulFunctionOne);
        StepWorkflow.Builder<Object> buildBuilder = thenConditionalBuilder.build(successfulFunctionTwo);
        assertEquals(successfulSupplierOne, buildBuilder.start.step);
        assertEquals(successfulSupplierTwo, buildBuilder.start.next.step);
        assertEquals(successfulFunctionTwo, buildBuilder.end.step);

        // Run and validate build()
        StepWorkflow<Object> buildStepWorkflow = buildBuilder.build();
        assertEquals(buildBuilder.start, buildStepWorkflow.start);
        assertEquals(buildBuilder.end, buildStepWorkflow.end);

        // Call run() and validate return StepWorkflowResponse
        StepWorkflowResponse<Object> response = buildStepWorkflow.run();
        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_FIVE, response.getData());
        assertNull(response.getException());
    }

    private boolean returnValue(boolean returnValue) {
        return returnValue;
    }

    private void successfulExecutor() {
        // No body, should always succeed.
    }

    private Integer successfulDataSupplierOne() {
        return TEST_DATA_ONE;
    }

    private Integer successfulDataSupplierTwo() {
        return TEST_DATA_TWO;
    }

    private Integer successfulDataSupplierThree() {
        return TEST_DATA_THREE;
    }

    private Integer successfulFunctionOne(Object object) {
        return TEST_DATA_FOUR;
    }

    private Integer successfulFunctionTwo(Object object) {
        return TEST_DATA_FIVE;
    }

    private Integer unsuccessfulDataSupplier() throws IntegrationException {
        throw exception;
    }

}
