/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.sca.integration.stepworkflow.jenkins;

import com.sca.integration.stepworkflow.SubStep;
import com.sca.integration.stepworkflow.SubStepResponse;
import com.synopsys.integration.exception.IntegrationException;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;

import java.io.Serializable;

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