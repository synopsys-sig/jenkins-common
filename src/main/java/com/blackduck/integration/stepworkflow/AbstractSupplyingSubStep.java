/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.stepworkflow;

public abstract class AbstractSupplyingSubStep<R> implements SubStep<Object, R> {
    public abstract SubStepResponse<R> run();

    @Override
    public SubStepResponse<R> run(final SubStepResponse previousResponse) {
        if (previousResponse.isSuccess()) {
            return run();
        } else {
            return SubStepResponse.FAILURE(previousResponse);
        }
    }
}