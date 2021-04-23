/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.extensions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;

import hudson.model.TaskListener;

public class JenkinsIntLogger extends IntLogger implements Serializable {
    private static final long serialVersionUID = -685871863395350470L;

    // We only really care about the PrintStream that the TaskListener contains, but that's not serializable.
    private final TaskListener jenkinsTaskListener;

    private LogLevel logLevel = LogLevel.INFO;

    public static JenkinsIntLogger logToListener(TaskListener jenkinsTaskListener) {
        return new JenkinsIntLogger(jenkinsTaskListener);
    }

    public static JenkinsIntLogger logToStandardOut() {
        return new JenkinsIntLogger(null);
    }

    private JenkinsIntLogger(TaskListener jenkinsTaskListener) {
        this.jenkinsTaskListener = jenkinsTaskListener;
    }

    public TaskListener getTaskListener() {
        return jenkinsTaskListener;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    /**
     * Prints the message regardless of the log level
     */
    @Override
    public void alwaysLog(String txt) {
        printLog(txt, null);
    }

    @Override
    public void debug(String txt) {
        if (logLevel.isLoggable(LogLevel.DEBUG)) {
            printLog(txt, null);
        }
    }

    @Override
    public void debug(String txt, Throwable e) {
        if (logLevel.isLoggable(LogLevel.DEBUG)) {
            printLog(txt, e);
        }
    }

    @Override
    public void error(Throwable e) {
        if (logLevel.isLoggable(LogLevel.ERROR)) {
            printLog(null, e);
        }
    }

    @Override
    public void error(String txt) {
        if (logLevel.isLoggable(LogLevel.ERROR)) {
            printLog(txt, null);
        }
    }

    @Override
    public void error(String txt, Throwable e) {
        if (logLevel.isLoggable(LogLevel.ERROR)) {
            printLog(txt, e);
        }
    }

    @Override
    public void info(String txt) {
        if (logLevel.isLoggable(LogLevel.INFO)) {
            printLog(txt, null);
        }
    }

    @Override
    public void trace(String txt) {
        if (logLevel.isLoggable(LogLevel.TRACE)) {
            printLog(txt, null);
        }
    }

    @Override
    public void trace(String txt, Throwable e) {
        if (logLevel.isLoggable(LogLevel.TRACE)) {
            printLog(txt, e);
        }
    }

    @Override
    public void warn(String txt) {
        if (logLevel.isLoggable(LogLevel.WARN)) {
            printLog(txt, null);
        }
    }

    private void printLog(String txt, Throwable e) {
        PrintStream logStream = jenkinsTaskListener != null ? jenkinsTaskListener.getLogger() : System.out;

        if (txt != null) {
            logStream.println(txt);
        }
        if (e != null) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logStream.println(sw.toString());
        }
    }

}
