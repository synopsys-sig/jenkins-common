package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.synopsys.integration.exception.IntegrationException;

public class StepWorkflowTest {
    public static final Integer TEST_DATA_ONE = 1;
    public static final Integer TEST_DATA_TWO = 2;
    public static final Integer TEST_DATA_THREE = 3;
    public static final Integer TEST_DATA_FOUR = 4;
    public static final Integer TEST_DATA_FIVE = 5;

    private static final IntegrationException integrationException = new IntegrationException("Exception #1");

    private SubStep<Object, Integer> subStepSupplierOne;
    private SubStep<Object, Integer> subStepSupplierTwo;
    private SubStep<Object, Integer> subStepSupplierThree;
    private SubStep<Object, Object> subStepExecutorOne;
    private SubStep<Integer, Object> subStepFunctionOne;
    private SubStep<Integer, Object> subStepFunctionTwo;
    private StepWorkflow.FlowController<Object, Integer> flowControllerOne;
    private StepWorkflow.Builder<Integer> successfulBuilderOfSupplierOne;
    private StepWorkflow.Builder<Integer> builderThenSubStepSupplierTwo;

    private StepWorkflow.Builder<Integer> unsuccessfulBuilderOfSupplierOne;
    private SubStep<Object, Integer> unsuccessfulSubStepSupplierOne;

    @BeforeEach
    public void setup() {
        subStepSupplierOne = SubStep.ofSupplier(() -> TEST_DATA_ONE);
        subStepSupplierTwo = SubStep.ofSupplier(() -> TEST_DATA_TWO);
        subStepSupplierThree = SubStep.ofSupplier(() -> TEST_DATA_THREE);
        subStepExecutorOne = SubStep.ofExecutor(() -> {});
        subStepFunctionOne = SubStep.ofFunction((Integer integer) -> TEST_DATA_FOUR);
        subStepFunctionTwo = SubStep.ofFunction((Integer integer) -> TEST_DATA_FIVE);

        flowControllerOne = new StepWorkflow.FlowController<>(subStepSupplierOne);
        assertEquals(subStepSupplierOne, flowControllerOne.step);
        assertNull(flowControllerOne.next);
        assertNull(flowControllerOne.getResponse());

        successfulBuilderOfSupplierOne = StepWorkflow.first(subStepSupplierOne);
        assertEquals(subStepSupplierOne, successfulBuilderOfSupplierOne.start.step);
        assertEquals(subStepSupplierOne, successfulBuilderOfSupplierOne.end.step);
        assertNull(successfulBuilderOfSupplierOne.start.next);
        assertNull(successfulBuilderOfSupplierOne.end.next);

        builderThenSubStepSupplierTwo = successfulBuilderOfSupplierOne.then(subStepSupplierTwo);
        assertEquals(subStepSupplierOne, builderThenSubStepSupplierTwo.start.step);
        assertEquals(subStepSupplierTwo, builderThenSubStepSupplierTwo.end.step);
        assertEquals(subStepSupplierTwo, builderThenSubStepSupplierTwo.start.next.step);
        assertNull(builderThenSubStepSupplierTwo.end.next);

        unsuccessfulSubStepSupplierOne = SubStep.ofSupplier(() -> {throw integrationException;});
        unsuccessfulBuilderOfSupplierOne = StepWorkflow.first(unsuccessfulSubStepSupplierOne);
        assertEquals(unsuccessfulSubStepSupplierOne, unsuccessfulBuilderOfSupplierOne.start.step);
        assertEquals(unsuccessfulSubStepSupplierOne, unsuccessfulBuilderOfSupplierOne.end.step);
        assertNull(unsuccessfulBuilderOfSupplierOne.start.next);
        assertNull(unsuccessfulBuilderOfSupplierOne.end.next);
    }

    @Test
    public void testSuccessfulSingleStepDataStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(subStepSupplierOne)
                                                     .run();

        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_ONE, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testSuccessfulSingleStepDatalessStepWorkflow() {
        StepWorkflowResponse<Object> response = StepWorkflow.just(subStepExecutorOne)
                                                    .run();

        assertTrue(response.wasSuccessful());
        assertNull(response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testUnsuccessfulSingleStepStepWorkflow() {
        StepWorkflowResponse<Integer> response = StepWorkflow.just(unsuccessfulSubStepSupplierOne)
                                                     .run();

        assertFalse(response.wasSuccessful());
        assertNull(response.getData());
        assertNotNull(response.getException());
    }

    @Test
    public void testFlowControllerAppendAndConstructor() {
        StepWorkflow.FlowController<?, Integer> appendFlowController = flowControllerOne.append(subStepSupplierTwo);

        assertEquals(subStepSupplierOne, flowControllerOne.step);
        assertEquals(appendFlowController, flowControllerOne.next);
        assertNull(flowControllerOne.getResponse());
        assertEquals(subStepSupplierTwo, appendFlowController.step);
        assertEquals(subStepSupplierTwo, flowControllerOne.next.step);
        assertNull(appendFlowController.next);
        assertNull(appendFlowController.getResponse());
    }

    @Test
    public void testFlowControllerRunStepSuccess() {
        flowControllerOne.runStep(SubStepResponse.SUCCESS());
        SubStepResponse<Integer> response = flowControllerOne.getResponse();

        // Ensure that response is the only field that was changed as a result of runStep() being called
        assertEquals(subStepSupplierOne, flowControllerOne.step);
        assertNull(flowControllerOne.next);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.hasData());
        assertEquals(TEST_DATA_ONE, response.getData());
        assertFalse(response.hasException());
        assertNull(response.getException());
    }

    @Test
    public void testFlowControllerRunStepFailure() {
        flowControllerOne.runStep(SubStepResponse.FAILURE(integrationException));
        SubStepResponse<Integer> response = flowControllerOne.getResponse();

        // Ensure that response is the only field that was changed as a result of runStep() being called
        assertEquals(subStepSupplierOne, flowControllerOne.step);
        assertNull(flowControllerOne.next);
        assertNotNull(response);
        assertTrue(response.isFailure());
        assertFalse(response.hasData());
        assertTrue(response.hasException());
        assertEquals(integrationException, response.getException());
    }

    @Test
    public void testBuilderAndSometimes() {
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = successfulBuilderOfSupplierOne.andSometimes(subStepSupplierTwo);
        StepWorkflow.Builder<Object> buildBuilder = andSometimesConditional.build(subStepFunctionOne);

        assertEquals(subStepSupplierOne, buildBuilder.start.step);
        assertEquals(subStepFunctionOne, buildBuilder.start.next.step);
        assertNull(buildBuilder.start.next.next);
        assertEquals(subStepFunctionOne, buildBuilder.end.step);
        assertNull(buildBuilder.end.next);
    }

    @Test
    public void testBuilderRun() {
        StepWorkflowResponse<Integer> response = successfulBuilderOfSupplierOne.run();

        assertTrue(response.wasSuccessful());
        assertEquals(TEST_DATA_ONE, response.getData());
        assertNull(response.getException());
    }

    @Test
    public void testConditionalConstructorAndThen() {
        StepWorkflow.ConditionalBuilder<Integer, Integer> conditionalBuilder = new StepWorkflow.ConditionalBuilder(builderThenSubStepSupplierTwo, subStepSupplierThree);
        StepWorkflow.ConditionalBuilder<Integer, Object> thenConditionalBuilder = conditionalBuilder.then(subStepFunctionOne);
        StepWorkflow.Builder<Object> buildBuilder = thenConditionalBuilder.build(subStepFunctionTwo);

        assertEquals(subStepSupplierOne, buildBuilder.start.step);
        assertEquals(subStepSupplierTwo, buildBuilder.start.next.step);
        assertEquals(subStepFunctionTwo, buildBuilder.end.step);
    }

    @Test
    public void testConditionalButOnlyIf() {
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = successfulBuilderOfSupplierOne.andSometimes(subStepSupplierTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditional.butOnlyIf(new Object(), ignored -> true);

        assertEquals(subStepSupplierOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);
        assertEquals(butOnlyIfBuilder.start.next.step, butOnlyIfBuilder.end.step);
        assertNull(butOnlyIfBuilder.end.next);
    }

    @Test
    public void testRunConditionalWorkflowFailedPreviousResponse() {
        // Create and validate butOnlyIf() Builder
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = unsuccessfulBuilderOfSupplierOne.andSometimes(subStepSupplierTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditional.butOnlyIf(new Object(), ignored -> true);
        assertEquals(unsuccessfulSubStepSupplierOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);
        assertEquals(butOnlyIfBuilder.start.next.step, butOnlyIfBuilder.end.step);
        assertNull(butOnlyIfBuilder.end.next);

        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = butOnlyIfBuilder.then(subStepSupplierThree);
        assertEquals(unsuccessfulSubStepSupplierOne, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(subStepSupplierThree, thenBuilder.end.step);
        assertEquals(unsuccessfulSubStepSupplierOne, thenBuilder.start.step);
        assertEquals(subStepSupplierThree, thenBuilder.end.step);
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
        StepWorkflow.ConditionalBuilder<Integer, Integer> andSometimesConditional = successfulBuilderOfSupplierOne.andSometimes(subStepSupplierTwo);
        StepWorkflow.Builder<Object> butOnlyIfBuilder = andSometimesConditional.butOnlyIf(new Object(), ignored -> predicateTest);
        assertEquals(subStepSupplierOne, butOnlyIfBuilder.start.step);
        assertNotNull(butOnlyIfBuilder.start.next);
        assertEquals(butOnlyIfBuilder.start.next.step, butOnlyIfBuilder.end.step);
        assertNull(butOnlyIfBuilder.end.next);

        // Create and validate then() Builder
        StepWorkflow.Builder<Integer> thenBuilder = butOnlyIfBuilder.then(subStepSupplierThree);
        assertEquals(subStepSupplierOne, thenBuilder.start.step);
        assertNotNull(thenBuilder.start.next);
        assertEquals(subStepSupplierThree, thenBuilder.end.step);
        assertEquals(subStepSupplierOne, thenBuilder.start.step);
        assertEquals(subStepSupplierThree, thenBuilder.end.step);
        assertNotNull(thenBuilder.start.next.step);
        assertNull(thenBuilder.end.next);

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

}
