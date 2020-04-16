/**
 * synopsys-polaris
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
package com.synopsys.integration.stepworkflow.jenkins;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.synopsys.integration.jenkins.JenkinsVersionHelper;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;

import hudson.AbortException;

public abstract class JenkinsStepWorkflow<T> {
    protected final JenkinsIntLogger logger;

    public JenkinsStepWorkflow(final JenkinsIntLogger jenkinsIntLogger) {
        this.logger = jenkinsIntLogger;
    }

    protected abstract PhoneHomeRequestBodyBuilder createPhoneHomeBuilder();

    protected abstract void validate() throws AbortException;

    protected abstract StepWorkflow<T> buildWorkflow() throws AbortException;

    public StepWorkflowResponse<T> perform() throws AbortException {
        final Optional<PhoneHomeResponse> phoneHomeResponse = beginPhoneHome();
        try {
            this.validate();
            return this.buildWorkflow().run();
        } finally {
            phoneHomeResponse.ifPresent(PhoneHomeResponse::getImmediateResult);
        }
    }

    protected Optional<PhoneHomeResponse> beginPhoneHome() {
        try {
            final PhoneHomeClient phoneHomeClient = new PhoneHomeClient(logger);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            final PhoneHomeService phoneHomeService = PhoneHomeService.createAsynchronousPhoneHomeService(logger, phoneHomeClient, executor);

            final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = this.createPhoneHomeBuilder();
            final PhoneHomeRequestBody phoneHomeRequestBody = this.addJenkinsMetadataAndBuildPhoneHomeRequest(phoneHomeRequestBodyBuilder);
            return Optional.ofNullable(phoneHomeService.phoneHome(phoneHomeRequestBody));
        } catch (final Exception e) {
            logger.trace("Phone home failed due to an unexpected exception:", e);
        }

        return Optional.empty();
    }

    protected PhoneHomeRequestBody addJenkinsMetadataAndBuildPhoneHomeRequest(final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder) {
        JenkinsVersionHelper.getJenkinsVersion()
            .ifPresent(jenkinsVersionString -> phoneHomeRequestBodyBuilder.addToMetaData("jenkins.version", jenkinsVersionString));
        return phoneHomeRequestBodyBuilder.build();
    }

}

