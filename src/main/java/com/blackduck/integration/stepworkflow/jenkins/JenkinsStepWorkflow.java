/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.stepworkflow.jenkins;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jenkins.wrapper.JenkinsVersionHelper;
import com.blackduck.integration.log.SilentIntLogger;
import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.stepworkflow.StepWorkflow;
import com.blackduck.integration.stepworkflow.StepWorkflowResponse;
import com.blackduck.integration.util.ResourceUtil;
import com.google.gson.Gson;
import com.blackduck.integration.jenkins.extensions.JenkinsIntLogger;
import com.blackduck.integration.phonehome.PhoneHomeClient;
import com.blackduck.integration.phonehome.PhoneHomeResponse;
import com.blackduck.integration.phonehome.PhoneHomeService;
import com.blackduck.integration.phonehome.google.analytics.GoogleAnalyticsConstants;
import com.blackduck.integration.phonehome.request.PhoneHomeRequestBody;
import com.blackduck.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.JsonSyntaxException;
import hudson.AbortException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class JenkinsStepWorkflow<T> {
    protected final JenkinsIntLogger logger;
    protected final JenkinsVersionHelper jenkinsVersionHelper;
    private final static int TIMEOUT_IN_SECONDS = 20;
    private final Gson gson;
    private final IntHttpClient intHttpClient;
    private final static String CREDENTIALS_PATH = "https://static-content.app.blackduck.com/detect/analytics/creds.json";
    private final static String TEST_CREDENTIALS_PATH = "https://static-content.saas-staging.blackduck.com/detect/analytics/creds.json";

    public JenkinsStepWorkflow(JenkinsIntLogger logger, JenkinsVersionHelper jenkinsVersionHelper) {
        this.logger = logger;
        this.jenkinsVersionHelper = jenkinsVersionHelper;
        this.gson = new Gson();
        this.intHttpClient = new IntHttpClient(
                new SilentIntLogger(),
                gson,
                TIMEOUT_IN_SECONDS,
                true,
                ProxyInfo.NO_PROXY_INFO
        );
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
            PhoneHomeCredentials phoneHomeCredentials = getGa4Credentials();
            PhoneHomeClient phoneHomeClient = new PhoneHomeClient(logger, httpClientBuilder, gson, phoneHomeCredentials.getApiSecret(), phoneHomeCredentials.getMeasurementId());
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

    protected PhoneHomeCredentials getGa4Credentials() throws IOException, InterruptedException, JsonSyntaxException, IntegrationException {
        String fileUrl = CREDENTIALS_PATH;
        if (isTestEnvironment()) {
            fileUrl = TEST_CREDENTIALS_PATH;
            logger.debug("Phone home is operational for a test environment.");
        }
        logger.debug("Downloading phone home credentials.");
        RequestBuilder createRequestBuilder = intHttpClient.createRequestBuilder(HttpMethod.GET);
        HttpUriRequest request = createRequestBuilder
                .setUri(fileUrl)
                .build();
        Response response = intHttpClient.execute(request);
        return gson.fromJson(response.getContentString(), PhoneHomeCredentials.class);
    }

    private boolean isTestEnvironment() {
        String projectVersion = "";
        try {
            String versionFileContents = ResourceUtil.getResourceAsString(this.getClass(), "/version.txt", StandardCharsets.UTF_8.toString());
            projectVersion = String.valueOf(Arrays.asList(versionFileContents.split("\n")).stream()
                    .filter(s -> s.startsWith("version=")).
                    map(s -> StringUtils.removeStart(s, "version="))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Error parsing version string from version file")));;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (StringUtils.contains(projectVersion, "SIGQA")
                || StringUtils.contains(projectVersion, "SNAPSHOT"));
    }

}

