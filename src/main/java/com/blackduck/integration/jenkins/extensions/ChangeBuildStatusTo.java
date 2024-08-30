/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.extensions;

import hudson.model.Result;

public enum ChangeBuildStatusTo implements JenkinsSelectBoxEnum {
    SUCCESS("Do not change build status (only log)", Result.SUCCESS),
    FAILURE("Fail the build", Result.FAILURE),
    UNSTABLE("Mark build as Unstable", Result.UNSTABLE);

    private final String displayName;
    private final Result result;

    ChangeBuildStatusTo(final String displayName, final Result result) {
        this.displayName = displayName;
        this.result = result;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public Result getResult() {
        return result;
    }

}
