/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.stepworkflow;

public class SubStepResponse<T> {
    private final boolean subStepSucceeded;
    private final Exception exception;
    private final T data;

    public SubStepResponse(final boolean subStepSucceeded, final T data, final Exception e) {
        this.subStepSucceeded = subStepSucceeded;
        this.exception = e;
        this.data = data;
    }

    // You should not return no data on a success unless you explicitly claim to return no data -- rotte OCT 9 2019
    public static SubStepResponse<Object> SUCCESS() {
        return SUCCESS(null);
    }

    public static <S> SubStepResponse<S> SUCCESS(final S data) {
        return new SubStepResponse<>(true, data, null);
    }

    public static <S> SubStepResponse<S> FAILURE(final SubStepResponse previousFailure) {
        return FAILURE(previousFailure.exception);
    }

    public static <S> SubStepResponse<S> FAILURE(final Exception e) {
        return new SubStepResponse<>(false, null, e);
    }

    public boolean isSuccess() {
        return subStepSucceeded;
    }

    public boolean isFailure() {
        return !subStepSucceeded;
    }

    public boolean hasData() {
        return data != null;
    }

    public T getData() {
        return data;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }
}
