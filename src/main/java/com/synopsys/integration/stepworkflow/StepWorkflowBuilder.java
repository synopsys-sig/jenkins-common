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

public class StepWorkflowBuilder<T> {
    private final StepWorkflowController<Object, ?> start;
    private final StepWorkflowController<?, T> end;

    public StepWorkflowBuilder(final SubStep<Object, T> firstStep) {
        final StepWorkflowController<Object, T> firstFlowController = new StepWorkflowController<>(firstStep);
        this.start = firstFlowController;
        this.end = firstFlowController;
    }

    public <S> StepWorkflowBuilder(final StepWorkflowBuilder<S> previousStepWorkflowBuilder, final SubStep<? super S, T> thisStep) {
        this.start = previousStepWorkflowBuilder.start;
        this.end = previousStepWorkflowBuilder.end.append(thisStep);
    }

    public <R> StepWorkflowBuilder<R> then(final SubStep<? super T, R> subStep) {
        return new StepWorkflowBuilder<>(this, subStep);
    }

    public <R> StepWorkflowConditionalBuilder<T, R> andSometimes(final SubStep<Object, R> subStep) {
        return new StepWorkflowConditionalBuilder<>(this, subStep);
    }

    public StepWorkflow<T> build() {
        return new StepWorkflow<T>(start, end);
    }

    public StepWorkflowResponse<T> run() {
        return this.build().run();
    }
}