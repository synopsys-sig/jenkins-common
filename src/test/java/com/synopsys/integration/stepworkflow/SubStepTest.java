package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;

public class SubStepTest {
    private static final Integer testData = 1;
    private static final String exceptionMessage1 = "Exception #1";
    private static final String exceptionMessage2 = "Exception #2";
    private static final IntegrationException exception = new IntegrationException(exceptionMessage1);
    private static final IntegrationException implementationException = new IntegrationException(exceptionMessage2);
    private static final SubStepResponse<Integer> successfulPreviousResponse = new SubStepResponse<>(true, testData, null);
    private static final SubStepResponse<Integer> successfulPreviousResponseNoData = new SubStepResponse<>(true, null, null);
    private static final SubStepResponse<Integer> failedPreviousResponse = new SubStepResponse<>(false, testData, exception);

    @Test
    public void testFunctionSuccessPreviousResponse() {
        SubStep<Integer, Object> subStep = SubStep.ofFunction(this::successfulFunction);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponse);

        assertTrue(subStepResponse.isSuccess());
        assertTrue(subStepResponse.hasData());
        assertEquals(testData, subStepResponse.getData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testFunctionSuccessPreviousResponseNoData() {
        SubStep<Integer, Object> subStep = SubStep.ofFunction(this::successfulFunction);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponseNoData);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testFunctionFailedPreviousResponse() {
        SubStep<Integer, Object> subStep = SubStep.ofFunction(this::successfulFunction);
        SubStepResponse<Object> subStepResponse = subStep.run(failedPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(exception, subStepResponse.getException());
    }

    @Test
    public void testFunctionFailedFunction() {
        SubStep<Integer, Object> subStep = SubStep.ofFunction(this::unsuccessfulFunction);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(implementationException, subStepResponse.getException());
    }

    @Test
    public void testConsumerSuccPreviousResponse() {
        SubStep<Integer, Object> subStep = SubStep.ofConsumer(this::successfulConsumer);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponse);

        assertTrue(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testConsumerSuccPreviousResponseNoData() {
        SubStep<Integer, Object> subStep = SubStep.ofConsumer(this::successfulConsumer);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponseNoData);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testConsumerFailedPreviousResponse() {
        SubStep<Integer, Object> subStep = SubStep.ofConsumer(this::successfulConsumer);
        SubStepResponse<Object> subStepResponse = subStep.run(failedPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(exception, subStepResponse.getException());
    }

    @Test
    public void testConsumerFailedConsumer() {
        SubStep<Integer, Object> subStep = SubStep.ofConsumer(this::unsuccessfulConsumer);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(implementationException, subStepResponse.getException());
    }

    @Test
    public void testSupplierSuccessPreviousResponse() {
        SubStep<Object, Integer> subStep = SubStep.ofSupplier(this::successfulSupplier);
        SubStepResponse<Integer> subStepResponse = subStep.run(successfulPreviousResponse);

        assertTrue(subStepResponse.isSuccess());
        assertTrue(subStepResponse.hasData());
        assertEquals(testData, subStepResponse.getData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testSupplierSuccessPreviousResponseNoData() {
        SubStep<Object, Integer> subStep = SubStep.ofSupplier(this::successfulSupplier);
        SubStepResponse<Integer> subStepResponse = subStep.run(successfulPreviousResponseNoData);

        assertTrue(subStepResponse.isSuccess());
        assertTrue(subStepResponse.hasData());
        assertEquals(testData, subStepResponse.getData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testSupplierFailedPreviousResponse() {
        SubStep<Object, Integer> subStep = SubStep.ofSupplier(this::successfulSupplier);
        SubStepResponse<Integer> subStepResponse = subStep.run(failedPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(exception, subStepResponse.getException());
    }

    @Test
    public void testSupplierFailedSupplier() {
        SubStep<Object, Integer> subStep = SubStep.ofSupplier(this::unsuccessfulSupplier);
        SubStepResponse<Integer> subStepResponse = subStep.run(successfulPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(implementationException, subStepResponse.getException());
    }

    @Test
    public void testExecutorSuccessPreviousResponse() {
        SubStep<Object, Object> subStep = SubStep.ofExecutor(this::successfulExecutor);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponse);

        assertTrue(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testExecutorSuccessPreviousResponseNoData() {
        SubStep<Object, Object> subStep = SubStep.ofExecutor(this::successfulExecutor);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponseNoData);

        assertTrue(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertFalse(subStepResponse.hasException());
    }

    @Test
    public void testExecutorFailedPreviousResponse() {
        SubStep<Object, Object> subStep = SubStep.ofExecutor(this::successfulExecutor);
        SubStepResponse<Object> subStepResponse = subStep.run(failedPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(exception, subStepResponse.getException());
    }

    @Test
    public void testExecutorFailedExecutor() {
        SubStep<Object, Object> subStep = SubStep.ofExecutor(this::unsuccessfulExecutor);
        SubStepResponse<Object> subStepResponse = subStep.run(successfulPreviousResponse);

        assertFalse(subStepResponse.isSuccess());
        assertFalse(subStepResponse.hasData());
        assertTrue(subStepResponse.hasException());
        assertEquals(implementationException, subStepResponse.getException());
    }

    private Integer successfulFunction(Object object) {
        return testData;
    }

    private Integer unsuccessfulFunction(Object object) throws IntegrationException {
        throw implementationException;
    }

    private void successfulConsumer(Integer integer) {
        // No body, should always succeed.
    }

    private void unsuccessfulConsumer(Object object) throws IntegrationException {
        throw implementationException;
    }

    private Integer successfulSupplier() {
        return testData;
    }

    private Integer unsuccessfulSupplier() throws IntegrationException {
        throw implementationException;
    }

    private void successfulExecutor() {
        // No body, should always succeed.
    }

    private void unsuccessfulExecutor() throws IntegrationException {
        throw implementationException;
    }

}
