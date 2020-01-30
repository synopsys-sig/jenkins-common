/**
 * jenkins-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public JenkinsIntLogger(final TaskListener jenkinsTaskListener) {
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
    public void setLogLevel(final LogLevel level) {
        this.logLevel = level;
    }

    /**
     * Prints the message regardless of the log level
     */
    @Override
    public void alwaysLog(final String txt) {
        printLog(txt, null);
    }

    @Override
    public void debug(final String txt) {
        if (logLevel.isLoggable(LogLevel.DEBUG)) {
            printLog(txt, null);
        }
    }

    @Override
    public void debug(final String txt, final Throwable e) {
        if (logLevel.isLoggable(LogLevel.DEBUG)) {
            printLog(txt, e);
        }
    }

    @Override
    public void error(final Throwable e) {
        if (logLevel.isLoggable(LogLevel.ERROR)) {
            printLog(null, e);
        }
    }

    @Override
    public void error(final String txt) {
        if (logLevel.isLoggable(LogLevel.ERROR)) {
            printLog(txt, null);
        }
    }

    @Override
    public void error(final String txt, final Throwable e) {
        if (logLevel.isLoggable(LogLevel.ERROR)) {
            printLog(txt, e);
        }
    }

    @Override
    public void info(final String txt) {
        if (logLevel.isLoggable(LogLevel.INFO)) {
            printLog(txt, null);
        }
    }

    @Override
    public void trace(final String txt) {
        if (logLevel.isLoggable(LogLevel.TRACE)) {
            printLog(txt, null);
        }
    }

    @Override
    public void trace(final String txt, final Throwable e) {
        if (logLevel.isLoggable(LogLevel.TRACE)) {
            printLog(txt, e);
        }
    }

    @Override
    public void warn(final String txt) {
        if (logLevel.isLoggable(LogLevel.WARN)) {
            printLog(txt, null);
        }
    }

    private void printLog(final String txt, final Throwable e) {
        final PrintStream logStream = jenkinsTaskListener != null ? jenkinsTaskListener.getLogger() : System.out;

        if (txt != null) {
            logStream.println(txt);
        }
        if (e != null) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logStream.println(sw.toString());
        }
    }

}
