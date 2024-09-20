/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.service;

import com.blackduck.integration.util.IntEnvironmentVariables;
import jenkins.tasks.SimpleBuildWrapper;

public class JenkinsWrapperContextService {
    private final SimpleBuildWrapper.Context context;

    public JenkinsWrapperContextService(SimpleBuildWrapper.Context context) {
        this.context = context;
    }

    public void populateEnvironment(IntEnvironmentVariables intEnvironmentVariables) {
        intEnvironmentVariables
            .getVariables()
            .forEach(context::env);
    }
}
