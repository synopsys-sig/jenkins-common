package com.sca.integration.jenkins.wrapper;

import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import hudson.util.Secret;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SynopsysCredentialsHelperTest {

    private final static String maskedPassword = "************************";
    private final static String credentialsId = "synopsys/blackduck";

    private static Stream<String> invalidCredentialIds() {
        return Stream.of("", " ", "  ", null);
    }

    @Test
    public void testGetIntegrationCredentialsByIdValidUser() {
        String credentialsUserName = StringUtils.upperCase(credentialsId);
        String credentialsPassWord = StringUtils.reverse(credentialsId);
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkinsWrapperMock);
        SynopsysCredentialsHelper spiedSynopsysCredentialsHelper = Mockito.spy(synopsysCredentialsHelper);
        UsernamePasswordCredentialsImpl usernamePasswordCredentialsMock = Mockito.mock(UsernamePasswordCredentialsImpl.class);
        Secret secret = Mockito.mock(Secret.class);

        Mockito.doReturn(Optional.of(usernamePasswordCredentialsMock)).when(spiedSynopsysCredentialsHelper).getCredentialsById(SynopsysCredentialsHelper.USERNAME_PASSWORD_CREDENTIALS_CLASS, credentialsId);
        Mockito.doReturn(credentialsUserName).when(usernamePasswordCredentialsMock).getUsername();
        Mockito.doReturn(secret).when(usernamePasswordCredentialsMock).getPassword();
        Mockito.doReturn(credentialsPassWord).when(secret).getPlainText();

        com.synopsys.integration.rest.credentials.Credentials credentials = spiedSynopsysCredentialsHelper.getIntegrationCredentialsById(credentialsId);
        assertEquals(credentialsUserName, credentials.getUsername().orElse(null));
        assertEquals(credentialsPassWord, credentials.getPassword().orElse(null));
        assertFalse(credentials.isBlank());
        assertEquals(maskedPassword, credentials.getMaskedPassword());
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialIds")
    public void testGetIntegrationCredentialsByIdInvalidUser(String credentialsId) {
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkinsWrapperMock);

        com.synopsys.integration.rest.credentials.Credentials credentials = synopsysCredentialsHelper.getIntegrationCredentialsById(credentialsId);
        assertEquals(Optional.empty(), credentials.getUsername());
        assertEquals(Optional.empty(), credentials.getPassword());
        assertTrue(credentials.isBlank());
        assertEquals(maskedPassword, credentials.getMaskedPassword());
    }

    @Test
    public void testGetApiTokenByCredentialsIdValidUser() {
        String apiTokenText = StringUtils.upperCase(credentialsId);
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkinsWrapperMock);
        SynopsysCredentialsHelper spiedSynopsysCredentialsHelper = Mockito.spy(synopsysCredentialsHelper);
        StringCredentialsImpl stringCredentialsImplMock = Mockito.mock(StringCredentialsImpl.class);
        Secret secretMock = Mockito.mock(Secret.class);

        Mockito.doReturn(Optional.of(stringCredentialsImplMock)).when(spiedSynopsysCredentialsHelper).getCredentialsById(SynopsysCredentialsHelper.API_TOKEN_CREDENTIALS_CLASS, credentialsId);
        Mockito.doReturn(secretMock).when(stringCredentialsImplMock).getSecret();
        Mockito.doReturn(apiTokenText).when(secretMock).getPlainText();

        assertEquals(apiTokenText, spiedSynopsysCredentialsHelper.getApiTokenByCredentialsId(credentialsId).orElse(null));
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialIds")
    public void testGetApiTokenByCredentialsIdInvalidUser(String credentialsId) {
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkinsWrapperMock);

        assertEquals(Optional.empty(), synopsysCredentialsHelper.getApiTokenByCredentialsId(credentialsId));
    }

    @Test
    public void testGetCredentialsByIdValidCredentials() {
        Class<StringCredentialsImpl> stringCredentialsClass = StringCredentialsImpl.class;
        StringCredentialsImpl expectedStringCredentialsMock = Mockito.mock(StringCredentialsImpl.class);
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);

        Mockito.when(jenkinsWrapperMock.getCredentialsById(ArgumentMatchers.any(IdMatcher.class), ArgumentMatchers.eq(stringCredentialsClass)))
            .thenReturn(Optional.ofNullable(expectedStringCredentialsMock));

        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkinsWrapperMock);

        assertEquals(expectedStringCredentialsMock, synopsysCredentialsHelper.getCredentialsById(stringCredentialsClass, credentialsId).orElse(null));
    }
}
