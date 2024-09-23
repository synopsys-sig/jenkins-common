/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.stepworkflow;

public abstract class AbstractConsumingSubStep<T> implements SubStep<T, Object> {
    public abstract SubStepResponse<Object> run(T data);

    @Override
    public SubStepResponse<Object> run(final SubStepResponse<? extends T> previousResponse) {
        if (previousResponse.isSuccess() && previousResponse.hasData()) {
            return run(previousResponse.getData());
        } else {
            return SubStepResponse.FAILURE(previousResponse);
        }
    }
}
