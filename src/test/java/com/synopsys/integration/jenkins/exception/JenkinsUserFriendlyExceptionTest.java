package com.synopsys.integration.jenkins.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class JenkinsUserFriendlyExceptionTest {

    @Test
    public void testNoMessage() {
        JenkinsUserFriendlyException jenkinsUserFriendlyException = new JenkinsUserFriendlyException();
        assertNull(jenkinsUserFriendlyException.getMessage(), String.format("Expected no message and found <%s>", jenkinsUserFriendlyException.getMessage()));
    }

    @Test
    public void testMessage() {
        String exceptionMessage = "Test Exception Message";
        JenkinsUserFriendlyException jenkinsUserFriendlyException = new JenkinsUserFriendlyException(exceptionMessage);
        assertEquals(exceptionMessage, jenkinsUserFriendlyException.getMessage());
    }
}
