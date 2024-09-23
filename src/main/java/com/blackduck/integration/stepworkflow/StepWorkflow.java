/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.stepworkflow;

import java.util.function.Predicate;

public class StepWorkflow<T> {
    protected FlowController<Object, ?> start;
    protected FlowController<?, T> end;

    protected StepWorkflow(FlowController<Object, ?> start, FlowController<?, T> end) {
        this.start = start;
        this.end = end;
    }

    protected StepWorkflow(FlowController<Object, T> startAndEnd) {
        this.start = startAndEnd;
        this.end = startAndEnd;
    }

    public static <R> Builder<R> first(SubStep<Object, R> firstStep) {
        return new Builder<>(firstStep);
    }

    public static <R> StepWorkflow<R> just(SubStep<Object, R> onlyStep) {
        return new StepWorkflow<>(new FlowController<>(onlyStep));
    }

    public StepWorkflowResponse<T> run() {
        start.runStep(SubStepResponse.SUCCESS());
        return new StepWorkflowResponse<>(end.response);
    }

    private SubStepResponse<T> runAsSubStep() {
        start.runStep(SubStepResponse.SUCCESS());
        return end.response;
    }

    protected static class FlowController<U, S> {
        protected final SubStep<U, S> step;
        protected FlowController<? super S, ?> next;
        protected SubStepResponse<S> response;

        protected FlowController(SubStep<U, S> current) {
            this.step = current;
        }

        protected SubStepResponse<S> getResponse() {
            return response;
        }

        protected <R> FlowController<?, R> append(SubStep<? super S, R> nextStep) {
            FlowController<? super S, R> nextController = new FlowController<>(nextStep);
            this.next = nextController;
            return nextController;
        }

        protected void runStep(SubStepResponse<? extends U> previousResponse) {
            response = step.run(previousResponse);
            if (next != null) {
                next.runStep(response);
            }
        }
    }

    public static class Builder<T> {
        protected final FlowController<Object, ?> start;
        protected final FlowController<?, T> end;

        protected Builder(SubStep<Object, T> firstStep) {
            FlowController<Object, T> firstFlowController = new FlowController<>(firstStep);
            this.start = firstFlowController;
            this.end = firstFlowController;
        }

        protected <S> Builder(Builder<S> previousStepWorkflowBuilder, SubStep<? super S, T> thisStep) {
            this.start = previousStepWorkflowBuilder.start;
            this.end = previousStepWorkflowBuilder.end.append(thisStep);
        }

        public <R> Builder<R> then(SubStep<? super T, R> subStep) {
            return new Builder<>(this, subStep);
        }

        public <R> ConditionalBuilder<T, R> andSometimes(SubStep<Object, R> subStep) {
            return new ConditionalBuilder<>(this, subStep);
        }

        public StepWorkflow<T> build() {
            return new StepWorkflow<>(start, end);
        }

        public StepWorkflowResponse<T> run() {
            return this.build().run();
        }

    }

    public static class ConditionalBuilder<P, T> {
        protected final Builder<T> conditionalStepWorkflowBuilder;
        protected final Builder<P> parentBuilder;

        protected ConditionalBuilder(Builder<P> parentBuilder, SubStep<Object, T> firstStep) {
            this.parentBuilder = parentBuilder;
            this.conditionalStepWorkflowBuilder = new Builder<>(firstStep);
        }

        protected <U> ConditionalBuilder(ConditionalBuilder<P, U> currentBuilder, Builder<T> conditionalStepWorkflowBuilder) {
            this.parentBuilder = currentBuilder.parentBuilder;
            this.conditionalStepWorkflowBuilder = conditionalStepWorkflowBuilder;
        }

        public <R> ConditionalBuilder<P, R> then(SubStep<? super T, R> subStep) {
            return new ConditionalBuilder<>(this, conditionalStepWorkflowBuilder.then(subStep));
        }

        public <B> Builder<Object> butOnlyIf(B objectToTest, Predicate<B> tester) {
            return this.build(previousResponse -> this.runConditionalWorkflow(objectToTest, tester, previousResponse));
        }

        protected Builder<Object> build(SubStep<P, Object> workflowAsSubstep) {
            return new Builder<>(parentBuilder, workflowAsSubstep);
        }

        protected <B> SubStepResponse<Object> runConditionalWorkflow(B objectToTest, Predicate<B> tester, SubStepResponse<? extends P> previousResponse) {
            if (previousResponse.isSuccess()) {
                if (tester.test(objectToTest)) {
                    SubStepResponse<T> response = conditionalStepWorkflowBuilder.build().runAsSubStep();
                    // The test occurs at runtime but our builder API demands a promise of typing at compile time. Since we don't know until runtime, we cannot promise data. -- rotte OCT 07 2019
                    return new SubStepResponse<>(response.isSuccess(), null, response.getException());
                }
                return SubStepResponse.SUCCESS();
            } else {
                return SubStepResponse.FAILURE(previousResponse);
            }
        }
    }

}
