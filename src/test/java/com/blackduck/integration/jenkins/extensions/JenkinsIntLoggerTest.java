package com.blackduck.integration.jenkins.extensions;

import com.blackduck.integration.log.LogLevel;
import hudson.model.TaskListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JenkinsIntLoggerTest {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private static final String textMessage = "This is a test message";
    private static final String expectedText = textMessage + "\n";

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(byteArrayOutputStream));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    private static Stream<Arguments> txtTestData() {
        return Stream.of(
            Arguments.of(null, null, LogLevel.OFF, ""),
            Arguments.of(null, null, LogLevel.ERROR, ""),
            Arguments.of(null, null, LogLevel.WARN, ""),
            Arguments.of(null, null, LogLevel.INFO, ""),
            Arguments.of(null, null, LogLevel.DEBUG, ""),
            Arguments.of(null, null, LogLevel.TRACE, ""),
            Arguments.of("", null, LogLevel.OFF, "\n"),
            Arguments.of("", null, LogLevel.ERROR, "\n"),
            Arguments.of("", null, LogLevel.WARN, "\n"),
            Arguments.of("", null, LogLevel.INFO, "\n"),
            Arguments.of("", null, LogLevel.DEBUG, "\n"),
            Arguments.of("", null, LogLevel.TRACE, "\n"),
            Arguments.of(textMessage, null, LogLevel.OFF, expectedText),
            Arguments.of(textMessage, null, LogLevel.ERROR, expectedText),
            Arguments.of(textMessage, null, LogLevel.WARN, expectedText),
            Arguments.of(textMessage, null, LogLevel.INFO, expectedText),
            Arguments.of(textMessage, null, LogLevel.DEBUG, expectedText),
            Arguments.of(textMessage, null, LogLevel.TRACE, expectedText)
        );
    }

    private static Stream<Arguments> exceptionTestData() {
        String exceptionMessage = "This is a test exception";
        RuntimeException runtimeException = new RuntimeException(exceptionMessage);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        runtimeException.printStackTrace(printWriter);
        String expectedException = stringWriter.toString() + "\n";

        return Stream.of(
            Arguments.of(null, runtimeException, LogLevel.OFF, expectedText),
            Arguments.of(null, runtimeException, LogLevel.ERROR, expectedException),
            Arguments.of(null, runtimeException, LogLevel.WARN, expectedException),
            Arguments.of(null, runtimeException, LogLevel.INFO, expectedException),
            Arguments.of(null, runtimeException, LogLevel.DEBUG, expectedException),
            Arguments.of(null, runtimeException, LogLevel.TRACE, expectedException),
            Arguments.of(textMessage, runtimeException, LogLevel.OFF, expectedText + expectedException),
            Arguments.of(textMessage, runtimeException, LogLevel.ERROR, expectedText + expectedException),
            Arguments.of(textMessage, runtimeException, LogLevel.WARN, expectedText + expectedException),
            Arguments.of(textMessage, runtimeException, LogLevel.INFO, expectedText + expectedException),
            Arguments.of(textMessage, runtimeException, LogLevel.DEBUG, expectedText + expectedException),
            Arguments.of(textMessage, runtimeException, LogLevel.TRACE, expectedText + expectedException)
        );
    }

    @Test
    public void testJenkinsIntLoggerTaskListener() {
        TaskListener taskListener = Mockito.mock(TaskListener.class);
        Mockito.when(taskListener.getLogger()).thenReturn(new PrintStream(byteArrayOutputStream));

        JenkinsIntLogger jenkinsIntLogger = JenkinsIntLogger.logToListener(taskListener);
        jenkinsIntLogger.setLogLevel(LogLevel.OFF);
        jenkinsIntLogger.alwaysLog(textMessage);

        assertEquals(taskListener, jenkinsIntLogger.getTaskListener());
        assertEquals(expectedText, byteArrayOutputStream.toString());
    }

    @ParameterizedTest
    @MethodSource("txtTestData")
    public void testJenkinsIntLoggerAlwaysLog(String txt, Throwable e, LogLevel logLevel, String expected) {

        assertNull(e, "This test requires that e is null.");

        JenkinsIntLogger jenkinsIntLogger = JenkinsIntLogger.logToStandardOut();
        jenkinsIntLogger.setLogLevel(logLevel);
        jenkinsIntLogger.alwaysLog(txt);
        assertNull(jenkinsIntLogger.getTaskListener());
        assertEquals(logLevel, jenkinsIntLogger.getLogLevel());
        assertEquals(expected, byteArrayOutputStream.toString());
    }

    @ParameterizedTest
    @MethodSource({ "txtTestData", "exceptionTestData" })
    public void testJenkinsIntLoggerError(String txt, Throwable e, LogLevel logLevel, String expected) {
        JenkinsIntLogger jenkinsIntLogger = JenkinsIntLogger.logToStandardOut();
        jenkinsIntLogger.setLogLevel(logLevel);
        expected = jenkinsIntLogger.getLogLevel().isLoggable(LogLevel.ERROR) ? expected : "";

        if ((e == null && txt == null) || (e != null && txt != null)) {
            jenkinsIntLogger.error(txt, e);
        } else if (txt == null) {
            jenkinsIntLogger.error(e);
        } else if (e == null) {
            jenkinsIntLogger.error(txt);
        }

        assertEquals(logLevel, jenkinsIntLogger.getLogLevel());
        assertEquals(expected, byteArrayOutputStream.toString());
    }

    @ParameterizedTest
    @MethodSource("txtTestData")
    public void testJenkinsIntLoggerWarn(String txt, Throwable e, LogLevel logLevel, String expected) {
        assertNull(e, "This test requires that e is null.");

        JenkinsIntLogger jenkinsIntLogger = JenkinsIntLogger.logToStandardOut();
        jenkinsIntLogger.setLogLevel(logLevel);
        expected = jenkinsIntLogger.getLogLevel().isLoggable(LogLevel.WARN) ? expected : "";

        jenkinsIntLogger.warn(txt);

        assertEquals(logLevel, jenkinsIntLogger.getLogLevel());
        assertEquals(expected, byteArrayOutputStream.toString());
    }

    @ParameterizedTest
    @MethodSource("txtTestData")
    public void testJenkinsIntLoggerInfo(String txt, Throwable e, LogLevel logLevel, String expected) {
        assertNull(e, "This test requires that e is null.");

        JenkinsIntLogger jenkinsIntLogger = JenkinsIntLogger.logToStandardOut();
        jenkinsIntLogger.setLogLevel(logLevel);
        expected = jenkinsIntLogger.getLogLevel().isLoggable(LogLevel.INFO) ? expected : "";

        jenkinsIntLogger.info(txt);

        assertEquals(logLevel, jenkinsIntLogger.getLogLevel());
        assertEquals(expected, byteArrayOutputStream.toString());
    }

    @ParameterizedTest
    @MethodSource({ "txtTestData", "exceptionTestData" })
    public void testJenkinsIntLoggerDebug(String txt, Throwable e, LogLevel logLevel, String expected) {
        JenkinsIntLogger jenkinsIntLogger = JenkinsIntLogger.logToStandardOut();
        jenkinsIntLogger.setLogLevel(logLevel);
        expected = jenkinsIntLogger.getLogLevel().isLoggable(LogLevel.DEBUG) ? expected : "";

        if (e == null) {
            jenkinsIntLogger.debug(txt);
        } else {
            jenkinsIntLogger.debug(txt, e);
        }

        assertEquals(logLevel, jenkinsIntLogger.getLogLevel());
        assertEquals(expected, byteArrayOutputStream.toString());
    }

    @ParameterizedTest
    @MethodSource({ "txtTestData", "exceptionTestData" })
    public void testJenkinsIntLoggerTrace(String txt, Throwable e, LogLevel logLevel, String expected) {
        JenkinsIntLogger jenkinsIntLogger = JenkinsIntLogger.logToStandardOut();
        jenkinsIntLogger.setLogLevel(logLevel);
        expected = jenkinsIntLogger.getLogLevel().isLoggable(LogLevel.TRACE) ? expected : "";

        if (e == null) {
            jenkinsIntLogger.trace(txt);
        } else {
            jenkinsIntLogger.trace(txt, e);
        }

        assertEquals(logLevel, jenkinsIntLogger.getLogLevel());
        assertEquals(expected, byteArrayOutputStream.toString());
    }
}
