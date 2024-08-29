package com.sca.integration.stepworkflow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractConsumingSubStepTest {

    private final String verificationData = "Verification output";
    private final RuntimeException verificationException = new RuntimeException("Verification runtime exception");
    private final SubStepResponse<Object> subStepResponseSuccessOutput = new SubStepResponse<>(true, verificationData, verificationException);

    private static Stream<Arguments> populateSuccessTests() {
        final String successData = "Success Data";
        RuntimeException runtimeException = new RuntimeException("Success runtime exception");
        return Stream.of(
            Arguments.of(true, "", null),
            Arguments.of(true, "", runtimeException),
            Arguments.of(true, successData, null),
            Arguments.of(true, successData, runtimeException)
        );
    }

    private static Stream<Arguments> populateFailureTests() {
        final String failedData = "Failed Data";
        RuntimeException runtimeException = new RuntimeException("Failure runtime exception");
        return Stream.of(
            Arguments.of(true, null, null),
            Arguments.of(true, null, runtimeException),
            Arguments.of(false, null, null),
            Arguments.of(false, null, runtimeException),
            Arguments.of(false, "", null),
            Arguments.of(false, "", runtimeException),
            Arguments.of(false, failedData, null),
            Arguments.of(false, failedData, runtimeException)
        );
    }

    @ParameterizedTest
    @MethodSource("populateSuccessTests")
    public void testAbstractConsumingSubStepSuccess(boolean subStepSucceeded, String data, Exception e) {
        AbstractConsumingSubStep<String> abstractConsumingSubStep = new AbstractConsumingSubStepTestImpl();
        SubStepResponse<String> previousSubStepResponse = new SubStepResponse<>(subStepSucceeded, data, e);
        SubStepResponse<Object> abstractConsumingSubStepResponse = abstractConsumingSubStep.run(previousSubStepResponse);

        assertTrue(abstractConsumingSubStepResponse.isSuccess());
        assertFalse(abstractConsumingSubStepResponse.isFailure());
        assertTrue(abstractConsumingSubStepResponse.hasData());
        assertEquals(subStepResponseSuccessOutput.getData(), abstractConsumingSubStepResponse.getData());
        assertTrue(abstractConsumingSubStepResponse.hasException());
        assertEquals(subStepResponseSuccessOutput.getException().getMessage(), abstractConsumingSubStepResponse.getException().getMessage());
    }

    @ParameterizedTest
    @MethodSource("populateFailureTests")
    public void testAbstractConsumingSubStepFailure(boolean subStepSucceeded, String data, Exception e) {
        AbstractConsumingSubStep<String> abstractConsumingSubStep = new AbstractConsumingSubStepTestImpl();
        SubStepResponse<String> previousSubStepResponse = new SubStepResponse<>(subStepSucceeded, data, e);
        SubStepResponse<Object> abstractConsumingSubStepResponse = abstractConsumingSubStep.run(previousSubStepResponse);

        assertFalse(abstractConsumingSubStepResponse.isSuccess());
        assertTrue(abstractConsumingSubStepResponse.isFailure());
        assertFalse(abstractConsumingSubStepResponse.hasData());
        assertNull(abstractConsumingSubStepResponse.getData());
        assertEquals(previousSubStepResponse.hasException(), abstractConsumingSubStepResponse.hasException());

        if (abstractConsumingSubStepResponse.hasException()) {
            assertEquals(previousSubStepResponse.getException().getMessage(), abstractConsumingSubStepResponse.getException().getMessage());
        }
    }

    public class AbstractConsumingSubStepTestImpl extends AbstractConsumingSubStep<String> {

        @Override
        public SubStepResponse<Object> run(String thisData) {
            return subStepResponseSuccessOutput;
        }
    }

}
