/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.stepworkflow.jenkins;

import java.io.Serializable;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.stepworkflow.SubStepResponse;

import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;

public class RemoteSubStep<R extends Serializable> implements SubStep<Object, R> {
    protected final VirtualChannel virtualChannel;
    protected final Callable<R, ? extends IntegrationException> callable;

    protected RemoteSubStep(final VirtualChannel virtualChannel, final Callable<R, ? extends IntegrationException> callable) {
        this.virtualChannel = virtualChannel;
        this.callable = callable;
    }

    public static <S extends Serializable> RemoteSubStep<S> of(final VirtualChannel virtualChannel, final Callable<S, ? extends IntegrationException> callable) {
        return new RemoteSubStep<>(virtualChannel, callable);
    }

    @Override
    public SubStepResponse<R> run(final SubStepResponse<?> previousResponse) {
        return SubStep.defaultExecution(previousResponse.isSuccess(), previousResponse, () -> SubStepResponse.SUCCESS(virtualChannel.call(callable)));
    }

}
