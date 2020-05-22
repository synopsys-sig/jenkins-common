package com.synopsys.integration.stepworkflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AbstractSupplyingSubStepTest {

    private final String verificationData = "Verification output";
    private final RuntimeException verificationException = new RuntimeException("Verification runtime exception");
    private final SubStepResponse<String> subStepResponse_successOutput = new SubStepResponse<>(true, verificationData, verificationException);

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
    public void testAbstractSupplyingSubStep_Success(boolean subStepSucceeded, String data, Exception e) {
        AbstractSupplyingSubStep abstractSupplyingSubStep = new AbstractSupplyingSubStep_TestImpl();
        SubStepResponse<String> subStepResponse_Input = new SubStepResponse<>(subStepSucceeded, data, e);
        SubStepResponse subStepResponse_Output = abstractSupplyingSubStep.run(subStepResponse_Input);

        assertTrue(subStepResponse_Output.isSuccess());
        assertFalse(subStepResponse_Output.isFailure());
        assertTrue(subStepResponse_Output.hasData());
        assertEquals(subStepResponse_successOutput.getData(), subStepResponse_Output.getData());
        assertTrue(subStepResponse_Output.hasException());
        assertEquals(subStepResponse_successOutput.getException().getMessage(), subStepResponse_Output.getException().getMessage());
    }

    @ParameterizedTest
    @MethodSource("populateFailureTests")
    public void testAbstractSupplyingSubStep_failure(boolean subStepSucceeded, String data, Exception e) {
        AbstractSupplyingSubStep abstractSupplyingSubStep = new AbstractSupplyingSubStep_TestImpl();
        SubStepResponse<String> subStepResponse_Input = new SubStepResponse<>(subStepSucceeded, data, e);
        SubStepResponse<Object> subStepResponse_Output = abstractSupplyingSubStep.run(subStepResponse_Input);

        assertFalse(subStepResponse_Output.isSuccess());
        assertTrue(subStepResponse_Output.isFailure());
        assertFalse(subStepResponse_Output.hasData());
        assertNull(subStepResponse_Output.getData());
        assertEquals(subStepResponse_Input.hasException(), subStepResponse_Output.hasException());

        if (subStepResponse_Output.hasException()) {
            assertEquals(subStepResponse_Input.getException().getMessage(), subStepResponse_Output.getException().getMessage());
        }
    }

    public class AbstractSupplyingSubStep_TestImpl extends AbstractSupplyingSubStep<String> {

        @Override
        public SubStepResponse<String> run() {
            return subStepResponse_successOutput;
        }
    }

}
