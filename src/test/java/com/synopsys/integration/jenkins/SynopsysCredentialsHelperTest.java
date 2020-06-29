package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.util.Secret;
import jenkins.model.Jenkins;

public class SynopsysCredentialsHelperTest {

    private final String maskedPassword = "************************";

    private static Stream<String> invalidCredentialIds() {
        return Stream.of("", " ", "  ", null);
    }

    @Test
    public void testSynopsysCredentialsHelperGetByIdValidJenkinsUser() {
        String credentialsId = "synopsys/blackduck";
        String credentialsUserName = StringUtils.upperCase(credentialsId);
        String credentialsPassWord = StringUtils.reverse(credentialsId);
        Jenkins jenkins = Mockito.mock(Jenkins.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkins);
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
    public void testSynopsysCredentialsHelperGetByIdInvalidUser(String credentialsId) {
        Jenkins jenkins = Mockito.mock(Jenkins.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkins);

        com.synopsys.integration.rest.credentials.Credentials credentials = synopsysCredentialsHelper.getIntegrationCredentialsById(credentialsId);
        assertEquals(Optional.empty(), credentials.getUsername());
        assertEquals(Optional.empty(), credentials.getPassword());
        assertTrue(credentials.isBlank());
        assertEquals(maskedPassword, credentials.getMaskedPassword());
    }

    @Test
    public void testSynopsysCredentialsHelperGetAPITokenValidJenkinsUser() {
        String credentialsId = "synopsys/blackduck";
        String apiTokenText = StringUtils.upperCase(credentialsId);
        Jenkins jenkins = Mockito.mock(Jenkins.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkins);
        SynopsysCredentialsHelper spiedSynopsysCredentialsHelper = Mockito.spy(synopsysCredentialsHelper);
        StringCredentialsImpl stringCredentialsImpl = Mockito.mock(StringCredentialsImpl.class);
        Secret secret = Mockito.mock(Secret.class);

        Mockito.doReturn(Optional.of(stringCredentialsImpl)).when(spiedSynopsysCredentialsHelper).getCredentialsById(SynopsysCredentialsHelper.API_TOKEN_CREDENTIALS_CLASS, credentialsId);
        Mockito.doReturn(secret).when(stringCredentialsImpl).getSecret();
        Mockito.doReturn(apiTokenText).when(secret).getPlainText();

        assertEquals(apiTokenText, spiedSynopsysCredentialsHelper.getApiTokenByCredentialsId(credentialsId).orElse(null));
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialIds")
    public void testSynopsysCredentialsHelperGetAPITokenInvalidUser(String credentialsId) {
        Jenkins jenkins = Mockito.mock(Jenkins.class);
        SynopsysCredentialsHelper synopsysCredentialsHelper = new SynopsysCredentialsHelper(jenkins);
        SynopsysCredentialsHelper spiedSynopsysCredentialsHelper = Mockito.spy(synopsysCredentialsHelper);

        assertEquals(Optional.empty(), spiedSynopsysCredentialsHelper.getApiTokenByCredentialsId(credentialsId));
    }

}
