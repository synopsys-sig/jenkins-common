/**
 * jenkins-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.stepworkflow;

import java.util.function.Predicate;

public class StepWorkflowConditionalBuilder<P, T> {
    private final StepWorkflowBuilder<T> conditionalStepWorkflowBuilder;
    private final StepWorkflowBuilder<P> parentBuilder;

    public StepWorkflowConditionalBuilder(final StepWorkflowBuilder<P> parentBuilder, final SubStep<Object, T> firstStep) {
        this.parentBuilder = parentBuilder;
        this.conditionalStepWorkflowBuilder = new StepWorkflowBuilder<>(firstStep);
    }

    public <U> StepWorkflowConditionalBuilder(final StepWorkflowConditionalBuilder<P, U> currentBuilder, final StepWorkflowBuilder<T> conditionalStepWorkflowBuilder) {
        this.parentBuilder = currentBuilder.parentBuilder;
        this.conditionalStepWorkflowBuilder = conditionalStepWorkflowBuilder;
    }

    public <R> StepWorkflowConditionalBuilder<P, R> then(final SubStep<T, R> subStep) {
        return new StepWorkflowConditionalBuilder<>(this, conditionalStepWorkflowBuilder.then(subStep));
    }

    public <B> StepWorkflowBuilder<Object> butOnlyIf(final B objectToTest, final Predicate<B> tester) {
        return this.build(previousResponse -> this.runConditionalWorkflow(objectToTest, tester, previousResponse));
    }

    public StepWorkflowBuilder<Object> build(final SubStep<P, Object> workflowAsSubstep) {
        return new StepWorkflowBuilder<>(parentBuilder, workflowAsSubstep);
    }

    public <B> SubStepResponse<Object> runConditionalWorkflow(final B objectToTest, final Predicate<B> tester, final SubStepResponse<? extends P> previousResponse) {
        if (previousResponse.isSuccess()) {
            if (tester.test(objectToTest)) {
                final SubStepResponse<T> response = conditionalStepWorkflowBuilder.build().runAsSubStep();
                // The test occurs at runtime but our builder API demands a promise of typing at compile time. Since we don't know until runtime, we cannot promise data. -- rotte OCT 07 2019
                return new SubStepResponse<>(response.isSuccess(), null, response.getException());
            }
            return SubStepResponse.SUCCESS();
        } else {
            return SubStepResponse.FAILURE(previousResponse);
        }
    }
}
