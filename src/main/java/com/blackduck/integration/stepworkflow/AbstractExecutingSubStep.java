/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.stepworkflow;

public abstract class AbstractExecutingSubStep implements SubStep<Object, Object> {
    public abstract SubStepResponse<Object> run();

    public SubStepResponse<Object> run(final SubStepResponse previousResponse) {
        if (previousResponse.isSuccess()) {
            return run();
        } else {
            return SubStepResponse.FAILURE(previousResponse);
        }
    }
}
