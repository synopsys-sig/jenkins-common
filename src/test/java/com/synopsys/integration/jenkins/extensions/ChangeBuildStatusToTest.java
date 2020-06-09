package com.synopsys.integration.jenkins.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import hudson.model.Result;

public class ChangeBuildStatusToTest {

    private static Stream<Arguments> testValues() {
        return Stream.of(
            Arguments.of(ChangeBuildStatusTo.SUCCESS, "Do not change build status (only log)", Result.SUCCESS),
            Arguments.of(ChangeBuildStatusTo.FAILURE, "Fail the build", Result.FAILURE),
            Arguments.of(ChangeBuildStatusTo.UNSTABLE, "Mark build as Unstable", Result.UNSTABLE)
        );
    }

    @ParameterizedTest
    @MethodSource("testValues")
    public void testChangeBuildStatusTo(ChangeBuildStatusTo changeBuildStatusTo, String expectedDisplayName, Result expectedResult) {
        assertEquals(expectedDisplayName, changeBuildStatusTo.getDisplayName());
        assertEquals(expectedResult, changeBuildStatusTo.getResult());
    }

}
