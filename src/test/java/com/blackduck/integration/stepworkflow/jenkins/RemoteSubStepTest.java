package com.blackduck.integration.stepworkflow.jenkins;

import com.synopsys.integration.stepworkflow.SubStepResponse;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;

public class RemoteSubStepTest {

    private final String testData = "Test mock Data";
    private final static VirtualChannel virtualChannel = Mockito.mock(VirtualChannel.class);
    private final static Callable callable = Mockito.mock(Callable.class);

    private static Stream<Arguments> inputTestData() {
        return Stream.of(
            Arguments.of(new RemoteSubStep<>(virtualChannel, callable)),
            Arguments.of(RemoteSubStep.of(virtualChannel, callable))
        );
    }

    @ParameterizedTest
    @MethodSource("inputTestData")
    public void testRemoteSubStepSuccess(RemoteSubStep<String> remoteSubStep) throws IOException, InterruptedException {
        final String subStepResponseSuccessData = "Test success Data";
        Exception subStepResponseSuccessException = new RuntimeException("Test success Exception");
        SubStepResponse<String> subStepResponseSuccess = new SubStepResponse<>(true, subStepResponseSuccessData, subStepResponseSuccessException);

        Mockito.when(virtualChannel.call(callable)).thenReturn(testData);

        SubStepResponse<String> subStepResponse = remoteSubStep.run(subStepResponseSuccess);

        assertTrue(subStepResponse.isSuccess());
        assertFalse(subStepResponse.isFailure());
        assertTrue(subStepResponse.hasData());
        assertEquals(testData, subStepResponse.getData());
        assertFalse(subStepResponse.hasException());
        assertNull(subStepResponse.getException());
    }

    @ParameterizedTest
    @MethodSource("inputTestData")
    public void testRemoteSubStepFailed(RemoteSubStep<String> remoteSubStep) throws IOException, InterruptedException {
        final String subStepResponseFailedData = "Test failed Data";
        Exception subStepResponseFailedException = new RuntimeException("Test failed Exception");
        SubStepResponse<String> subStepResponseFail = new SubStepResponse<>(false, subStepResponseFailedData, subStepResponseFailedException);

        Mockito.when(virtualChannel.call(callable)).thenReturn(testData);

        SubStepResponse<String> subStepResponse = remoteSubStep.run(subStepResponseFail);

        assertFalse(subStepResponse.isSuccess());
        assertTrue(subStepResponse.isFailure());
        assertFalse(subStepResponse.hasData());
        assertNull(subStepResponse.getData());
        assertTrue(subStepResponse.hasException());
        assertEquals(subStepResponseFailedException, subStepResponse.getException());
    }

}
