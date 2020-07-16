package com.synopsys.integration.stepworkflow.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.wrapper.JenkinsVersionHelper;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.UniquePhoneHomeProduct;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.stepworkflow.StepWorkflow;
import com.synopsys.integration.stepworkflow.StepWorkflowResponse;
import com.synopsys.integration.stepworkflow.SubStep;
import com.synopsys.integration.util.NameVersion;

import hudson.AbortException;
import hudson.model.TaskListener;

public class JenkinsStepWorkflowTest {
    private static final String jenkinsVersion = "Jenkins 3.13.39";
    private static final String customerId = "testCustomerId";
    private static final String hostName = "testHostName";
    private static final String productVersion = "testProductVersion";

    private static final String responseData = "testResponseData";

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

    @Test
    public void testRunWorkflow() throws AbortException {
        Optional<PhoneHomeResponse> phoneHomeResponseMock = Optional.ofNullable(Mockito.mock(PhoneHomeResponse.class));
        JenkinsStepWorkflow<String> spiedJenkinsStepWorkflow = Mockito.spy(jenkinsStepWorkflow);
        Mockito.doReturn(phoneHomeResponseMock).when(spiedJenkinsStepWorkflow).beginPhoneHome();

        StepWorkflowResponse<String> stringStepWorkflowResponse = spiedJenkinsStepWorkflow.runWorkflow();

        assertTrue(stringStepWorkflowResponse.wasSuccessful());
        assertEquals(responseData, stringStepWorkflowResponse.getData());
        assertNull(stringStepWorkflowResponse.getException());

        Mockito.verify(spiedJenkinsStepWorkflow, Mockito.times(1)).beginPhoneHome();
        Mockito.verify(phoneHomeResponseMock.get(), Mockito.times(1)).getImmediateResult();
    }

    @Test
    public void testRunWorkflowException() throws AbortException {
        Optional<PhoneHomeResponse> phoneHomeResponseMock = Optional.ofNullable(Mockito.mock(PhoneHomeResponse.class));
        JenkinsStepWorkflow<String> spiedJenkinsStepWorkflow = Mockito.spy(jenkinsStepWorkflow);
        Mockito.doReturn(phoneHomeResponseMock).when(spiedJenkinsStepWorkflow).beginPhoneHome();
        Mockito.when(spiedJenkinsStepWorkflow.buildWorkflow()).thenThrow(new AbortException());

        assertThrows(AbortException.class, spiedJenkinsStepWorkflow::runWorkflow);

        Mockito.verify(spiedJenkinsStepWorkflow, Mockito.times(1)).beginPhoneHome();
        Mockito.verify(phoneHomeResponseMock.get(), Mockito.times(1)).getImmediateResult();
    }

    @Test
    public void testBeginPhoneHomeException() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TaskListener taskListener = Mockito.mock(TaskListener.class);
        Mockito.when(taskListener.getLogger()).thenReturn(new PrintStream(byteArrayOutputStream));
        JenkinsIntLogger jenkinsIntLogger = new JenkinsIntLogger(taskListener);
        jenkinsIntLogger.setLogLevel(LogLevel.TRACE);

        JenkinsStepWorkflow<String> jenkinsStepWorkflow = new JenkinsStepWorkflowImpl(jenkinsIntLogger, jenkinsVersionHelperMock);
        JenkinsStepWorkflow<String> spiedJenkinsStepWorkflow = Mockito.spy(jenkinsStepWorkflow);
        PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilderMock = Mockito.mock(PhoneHomeRequestBodyBuilder.class);

        Mockito.doReturn(phoneHomeRequestBodyBuilderMock).when(spiedJenkinsStepWorkflow).createPhoneHomeBuilder();
        Mockito.doThrow(new RuntimeException()).when(spiedJenkinsStepWorkflow).createPhoneHomeBuilder();

        Optional<PhoneHomeResponse> phoneHomeResponse = spiedJenkinsStepWorkflow.beginPhoneHome();

        assertEquals(Optional.empty(), phoneHomeResponse);
        assertTrue(byteArrayOutputStream.toString().startsWith("Phone home failed due to an unexpected exception:"));
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
            return StepWorkflow.just(SubStep.ofSupplier(() -> responseData));
        }

        @Override
        public Object perform() throws Exception {
            return null;
        }

    }

}
