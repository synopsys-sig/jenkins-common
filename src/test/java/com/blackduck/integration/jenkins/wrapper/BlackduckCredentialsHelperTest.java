package com.blackduck.integration.jenkins.wrapper;

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

public class BlackduckCredentialsHelperTest {

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
        BlackduckCredentialsHelper blackduckCredentialsHelper = new BlackduckCredentialsHelper(jenkinsWrapperMock);
        BlackduckCredentialsHelper spiedBlackduckCredentialsHelper = Mockito.spy(blackduckCredentialsHelper);
        UsernamePasswordCredentialsImpl usernamePasswordCredentialsMock = Mockito.mock(UsernamePasswordCredentialsImpl.class);
        Secret secret = Mockito.mock(Secret.class);

        Mockito.doReturn(Optional.of(usernamePasswordCredentialsMock)).when(spiedBlackduckCredentialsHelper).getCredentialsById(BlackduckCredentialsHelper.USERNAME_PASSWORD_CREDENTIALS_CLASS, credentialsId);
        Mockito.doReturn(credentialsUserName).when(usernamePasswordCredentialsMock).getUsername();
        Mockito.doReturn(secret).when(usernamePasswordCredentialsMock).getPassword();
        Mockito.doReturn(credentialsPassWord).when(secret).getPlainText();

        com.blackduck.integration.rest.credentials.Credentials credentials = spiedBlackduckCredentialsHelper.getIntegrationCredentialsById(credentialsId);
        assertEquals(credentialsUserName, credentials.getUsername().orElse(null));
        assertEquals(credentialsPassWord, credentials.getPassword().orElse(null));
        assertFalse(credentials.isBlank());
        assertEquals(maskedPassword, credentials.getMaskedPassword());
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialIds")
    public void testGetIntegrationCredentialsByIdInvalidUser(String credentialsId) {
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        BlackduckCredentialsHelper blackduckCredentialsHelper = new BlackduckCredentialsHelper(jenkinsWrapperMock);

        com.blackduck.integration.rest.credentials.Credentials credentials = blackduckCredentialsHelper.getIntegrationCredentialsById(credentialsId);
        assertEquals(Optional.empty(), credentials.getUsername());
        assertEquals(Optional.empty(), credentials.getPassword());
        assertTrue(credentials.isBlank());
        assertEquals(maskedPassword, credentials.getMaskedPassword());
    }

    @Test
    public void testGetApiTokenByCredentialsIdValidUser() {
        String apiTokenText = StringUtils.upperCase(credentialsId);
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        BlackduckCredentialsHelper blackduckCredentialsHelper = new BlackduckCredentialsHelper(jenkinsWrapperMock);
        BlackduckCredentialsHelper spiedBlackduckCredentialsHelper = Mockito.spy(blackduckCredentialsHelper);
        StringCredentialsImpl stringCredentialsImplMock = Mockito.mock(StringCredentialsImpl.class);
        Secret secretMock = Mockito.mock(Secret.class);

        Mockito.doReturn(Optional.of(stringCredentialsImplMock)).when(spiedBlackduckCredentialsHelper).getCredentialsById(BlackduckCredentialsHelper.API_TOKEN_CREDENTIALS_CLASS, credentialsId);
        Mockito.doReturn(secretMock).when(stringCredentialsImplMock).getSecret();
        Mockito.doReturn(apiTokenText).when(secretMock).getPlainText();

        assertEquals(apiTokenText, spiedBlackduckCredentialsHelper.getApiTokenByCredentialsId(credentialsId).orElse(null));
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialIds")
    public void testGetApiTokenByCredentialsIdInvalidUser(String credentialsId) {
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        BlackduckCredentialsHelper blackduckCredentialsHelper = new BlackduckCredentialsHelper(jenkinsWrapperMock);

        assertEquals(Optional.empty(), blackduckCredentialsHelper.getApiTokenByCredentialsId(credentialsId));
    }

    @Test
    public void testGetCredentialsByIdValidCredentials() {
        Class<StringCredentialsImpl> stringCredentialsClass = StringCredentialsImpl.class;
        StringCredentialsImpl expectedStringCredentialsMock = Mockito.mock(StringCredentialsImpl.class);
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);

        Mockito.when(jenkinsWrapperMock.getCredentialsById(ArgumentMatchers.any(IdMatcher.class), ArgumentMatchers.eq(stringCredentialsClass)))
            .thenReturn(Optional.ofNullable(expectedStringCredentialsMock));

        BlackduckCredentialsHelper blackduckCredentialsHelper = new BlackduckCredentialsHelper(jenkinsWrapperMock);

        assertEquals(expectedStringCredentialsMock, blackduckCredentialsHelper.getCredentialsById(stringCredentialsClass, credentialsId).orElse(null));
    }
}
