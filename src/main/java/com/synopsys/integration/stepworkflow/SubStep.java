/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.stepworkflow;

import com.synopsys.integration.function.ThrowingConsumer;
import com.synopsys.integration.function.ThrowingExecutor;
import com.synopsys.integration.function.ThrowingFunction;
import com.synopsys.integration.function.ThrowingSupplier;

@FunctionalInterface
public interface SubStep<T, R> {
    static <T, R, E extends Exception> SubStepResponse<R> defaultExecution(final boolean runCondition, final SubStepResponse<T> previousResponse, final ThrowingSupplier<SubStepResponse<R>, E> successSupplier) {
        try {
            if (runCondition) {
                return successSupplier.get();
            } else {
                return SubStepResponse.FAILURE(previousResponse);
            }
        } catch (final Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return SubStepResponse.FAILURE(e);
        }
    }

    static <T, R, E extends Exception> SubStep<T, R> ofFunction(final ThrowingFunction<T, R, E> throwingFunction) {
        return previousResponse -> SubStep.defaultExecution(previousResponse.isSuccess() && previousResponse.hasData(), previousResponse, () -> {
            final R data = throwingFunction.apply(previousResponse.getData());
            return SubStepResponse.SUCCESS(data);
        });
    }

    static <T, E extends Exception> SubStep<T, Object> ofConsumer(final ThrowingConsumer<T, E> throwingConsumer) {
        return previousResponse -> SubStep.defaultExecution(previousResponse.isSuccess() && previousResponse.hasData(), previousResponse, () -> {
            throwingConsumer.accept(previousResponse.getData());
            return SubStepResponse.SUCCESS();
        });
    }

    static <R, E extends Exception> SubStep<Object, R> ofSupplier(final ThrowingSupplier<R, E> throwingSupplier) {
        return previousResponse -> SubStep.defaultExecution(previousResponse.isSuccess(), previousResponse, () -> {
            final R data = throwingSupplier.get();
            return SubStepResponse.SUCCESS(data);
        });
    }

    static <E extends Exception> SubStep<Object, Object> ofExecutor(final ThrowingExecutor<E> throwingExecutor) {
        return previousResponse -> SubStep.defaultExecution(previousResponse.isSuccess(), previousResponse, () -> {
            throwingExecutor.execute();
            return SubStepResponse.SUCCESS();
        });
    }

    SubStepResponse<R> run(final SubStepResponse<? extends T> previousResponse);

}
