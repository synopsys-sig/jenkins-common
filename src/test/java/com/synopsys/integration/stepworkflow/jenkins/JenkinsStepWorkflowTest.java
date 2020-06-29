package com.synopsys.integration.stepworkflow.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.jenkins.JenkinsVersionHelper;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.phonehome.UniquePhoneHomeProduct;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.util.NameVersion;

import hudson.AbortException;

public class JenkinsStepWorkflowTest {
    private static final String jenkinsVersion = "Jenkins 3.13.39";
    private static final String customerId = "testCustomerId";
    private static final String hostName = "testHostName";
    private static final String productVersion = "testProductVersion";

    private static final NameVersion artifactInfo = new NameVersion("testName-NameVersion", "testVersion-NameVersion");
    private static final UniquePhoneHomeProduct product = UniquePhoneHomeProduct.create("testName-UniquePhoneHomeProduct");
    private static final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = new PhoneHomeRequestBodyBuilder(customerId, hostName, artifactInfo, product, productVersion);

    private final JenkinsVersionHelper jenkinsVersionHelperMock = Mockito.mock(JenkinsVersionHelper.class);
    private final JenkinsIntLogger jenkinsIntLoggerMock = Mockito.mock(JenkinsIntLogger.class);
    private final JenkinsStepWorkflow<String> jenkinsStepWorkflow = new JenkinsStepWorkflowImpl(jenkinsIntLoggerMock, jenkinsVersionHelperMock);

    @Test
    public void testCreatePhoneHomeRequestBodyValidJenkins() {
        Mockito.when(jenkinsVersionHelperMock.getJenkinsVersion()).thenReturn(Optional.of(jenkinsVersion));
        PhoneHomeRequestBody phoneHomeRequestBody = jenkinsStepWorkflow.addJenkinsMetadataAndBuildPhoneHomeRequest(phoneHomeRequestBodyBuilder);

        assertEquals(phoneHomeRequestBodyBuilder.getCustomerId(), phoneHomeRequestBody.getCustomerId());
        assertEquals(phoneHomeRequestBodyBuilder.getHostName(), phoneHomeRequestBody.getHostName());
        assertEquals(phoneHomeRequestBodyBuilder.getArtifactInfo().getName(), phoneHomeRequestBody.getArtifactId());
        assertEquals(phoneHomeRequestBodyBuilder.getArtifactInfo().getVersion(), phoneHomeRequestBody.getArtifactVersion());
        assertEquals(phoneHomeRequestBodyBuilder.getProduct().getName(), phoneHomeRequestBody.getProductName());
        assertEquals(phoneHomeRequestBodyBuilder.getProductVersion(), phoneHomeRequestBody.getProductVersion());
        assertEquals(phoneHomeRequestBodyBuilder.getMetaData(), phoneHomeRequestBody.getMetaData());
        assertTrue(phoneHomeRequestBody.getMetaData().containsKey("jenkins.version"));
        assertTrue(phoneHomeRequestBody.getMetaData().containsValue(jenkinsVersion));
    }

    @Test
    public void testCreatePhoneHomeRequestBodyNullJenkins() {
        Mockito.when(jenkinsVersionHelperMock.getJenkinsVersion()).thenReturn(Optional.empty());
        PhoneHomeRequestBody phoneHomeRequestBody = jenkinsStepWorkflow.addJenkinsMetadataAndBuildPhoneHomeRequest(phoneHomeRequestBodyBuilder);

        assertEquals(phoneHomeRequestBodyBuilder.getCustomerId(), phoneHomeRequestBody.getCustomerId());
        assertEquals(phoneHomeRequestBodyBuilder.getHostName(), phoneHomeRequestBody.getHostName());
        assertEquals(phoneHomeRequestBodyBuilder.getArtifactInfo().getName(), phoneHomeRequestBody.getArtifactId());
        assertEquals(phoneHomeRequestBodyBuilder.getArtifactInfo().getVersion(), phoneHomeRequestBody.getArtifactVersion());
        assertEquals(phoneHomeRequestBodyBuilder.getProduct().getName(), phoneHomeRequestBody.getProductName());
        assertEquals(phoneHomeRequestBodyBuilder.getProductVersion(), phoneHomeRequestBody.getProductVersion());
        assertEquals(phoneHomeRequestBodyBuilder.getMetaData(), phoneHomeRequestBody.getMetaData());
        assertFalse(phoneHomeRequestBody.getMetaData().containsKey("jenkins.version"));
        assertFalse(phoneHomeRequestBody.getMetaData().containsValue(jenkinsVersion));
    }

    public static class JenkinsStepWorkflowImpl extends JenkinsStepWorkflow<String> {

        public JenkinsStepWorkflowImpl(JenkinsIntLogger jenkinsIntLogger, JenkinsVersionHelper jenkinsVersionHelper) {
            super(jenkinsIntLogger, jenkinsVersionHelper);
        }

        @Override
        protected PhoneHomeRequestBodyBuilder createPhoneHomeBuilder() {
            return phoneHomeRequestBodyBuilder;
        }

        @Override
        protected StepWorkflow<String> buildWorkflow() throws AbortException {
            return null;
        }

        @Override
        public Object perform() throws Exception {
            return null;
        }
    }

}
