/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.stepworkflow;

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
