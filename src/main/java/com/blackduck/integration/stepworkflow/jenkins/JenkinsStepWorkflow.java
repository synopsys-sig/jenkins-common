/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.stepworkflow.jenkins;

import com.blackduck.integration.jenkins.wrapper.JenkinsVersionHelper;
import com.blackduck.integration.stepworkflow.StepWorkflow;
import com.blackduck.integration.stepworkflow.StepWorkflowResponse;
import com.google.gson.Gson;
import com.blackduck.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.google.analytics.GoogleAnalyticsConstants;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import hudson.AbortException;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class JenkinsStepWorkflow<T> {
    protected final JenkinsIntLogger logger;
    protected final JenkinsVersionHelper jenkinsVersionHelper;

    public JenkinsStepWorkflow(JenkinsIntLogger logger, JenkinsVersionHelper jenkinsVersionHelper) {
        this.logger = logger;
        this.jenkinsVersionHelper = jenkinsVersionHelper;
    }

    protected abstract PhoneHomeRequestBodyBuilder createPhoneHomeBuilder();

    protected abstract StepWorkflow<T> buildWorkflow() throws AbortException;

    /**
     * The public facing method to run this workflow. Implementing classes should call {@link JenkinsStepWorkflow#runWorkflow runWorkflow} in the method body and then either
     * handle the {@link StepWorkflowResponse StepWorkflowResponse} with response handling logic, or call {@link StepWorkflowResponse#getDataOrThrowException() getDataOrThrowException}.
     * @return the resulting meaningful data from the workflow execution
     * @throws Exception
     */
    public abstract Object perform() throws Exception;

    protected StepWorkflowResponse<T> runWorkflow() throws AbortException {
        Optional<PhoneHomeResponse> phoneHomeResponse = beginPhoneHome();
        try {
            return this.buildWorkflow()
                .run();
        } finally {
            phoneHomeResponse.ifPresent(PhoneHomeResponse::getImmediateResult);
        }
    }

    protected Optional<PhoneHomeResponse> beginPhoneHome() {
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            Gson gson = new Gson();
            PhoneHomeClient phoneHomeClient = new PhoneHomeClient(logger, httpClientBuilder, gson, GoogleAnalyticsConstants.PRODUCTION_INTEGRATIONS_TRACKING_ID);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            PhoneHomeService phoneHomeService = PhoneHomeService.createAsynchronousPhoneHomeService(logger, phoneHomeClient, executor);

            PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = this.createPhoneHomeBuilder();
            PhoneHomeRequestBody phoneHomeRequestBody = this.addJenkinsMetadataAndBuildPhoneHomeRequest(phoneHomeRequestBodyBuilder);
            return Optional.ofNullable(phoneHomeService.phoneHome(phoneHomeRequestBody));
        } catch (Exception e) {
            logger.trace("Phone home failed due to an unexpected exception:", e);
        }

        return Optional.empty();
    }

    protected PhoneHomeRequestBody addJenkinsMetadataAndBuildPhoneHomeRequest(PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder) {
        jenkinsVersionHelper.getJenkinsVersion()
            .ifPresent(jenkinsVersionString -> phoneHomeRequestBodyBuilder.addToMetaData("jenkins.version", jenkinsVersionString));
        return phoneHomeRequestBodyBuilder.build();
    }

}

