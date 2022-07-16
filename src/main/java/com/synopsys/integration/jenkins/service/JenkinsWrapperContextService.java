/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.service;

import com.synopsys.integration.util.IntEnvironmentVariables;

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
