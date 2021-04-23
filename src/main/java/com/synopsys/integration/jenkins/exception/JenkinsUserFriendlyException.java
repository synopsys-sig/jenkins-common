/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.exception;

import hudson.AbortException;

/**
 * An integrations-specific alternative to AbortExceptions, so we know we're only handling AbortExceptions thrown by our own code.
 * Extending AbortException buys us the special functionality that Jenkins has when handling uncaught AbortExceptions in our pipeline steps.
 * Ideally, this class would also extend IntegrationException, but sadly we cannot do that.
 * -- rotte OCT 2020
 */
public class JenkinsUserFriendlyException extends AbortException {
    private static final long serialVersionUID = 4947006408980692375L;

    public JenkinsUserFriendlyException() {
        super();
    }

    public JenkinsUserFriendlyException(String message) {
        super(message);
    }
}
