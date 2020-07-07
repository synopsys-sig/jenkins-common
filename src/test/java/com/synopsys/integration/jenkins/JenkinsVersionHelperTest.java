package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

public class JenkinsVersionHelperTest {

    private static String pluginName = "jenkins-common";

    @Test
    public void testGetPluginVersion() {
        String expectedPluginVersion = "3.13.39";

        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        Jenkins jenkinsMock = Mockito.mock(Jenkins.class);
        Plugin pluginMock = Mockito.mock(Plugin.class);
        PluginWrapper pluginWrapperMock = Mockito.mock(PluginWrapper.class);

        Mockito.when(jenkinsWrapperMock.getJenkins()).thenReturn(Optional.of(jenkinsMock));
        Mockito.when(jenkinsMock.getPlugin(pluginName)).thenReturn(pluginMock);
        Mockito.when(pluginMock.getWrapper()).thenReturn(pluginWrapperMock);
        Mockito.when(pluginWrapperMock.getVersion()).thenReturn(expectedPluginVersion);

        JenkinsVersionHelper jenkinsVersionHelper = new JenkinsVersionHelper(jenkinsWrapperMock);
        Optional<String> actualPluginVersion = jenkinsVersionHelper.getPluginVersion(pluginName);

        assertEquals(expectedPluginVersion, actualPluginVersion.orElse(null));
        assertTrue(actualPluginVersion.isPresent());
    }

    @Test
    public void testGetPluginVersionNull() {
        JenkinsWrapper jenkinsWrapper = JenkinsWrapper.initializeFromJenkinsJVM();
        JenkinsVersionHelper jenkinsVersionHelper = new JenkinsVersionHelper(jenkinsWrapper);
        Optional<String> actualPluginVersion = jenkinsVersionHelper.getPluginVersion(pluginName);

        assertFalse(actualPluginVersion.isPresent());
    }

    @Test
    public void testGetJenkinsVersion() {
        JenkinsWrapper jenkinsWrapperMock = Mockito.mock(JenkinsWrapper.class);
        VersionNumber expectedVersionNumber = new VersionNumber("Jenkins 1.2.3");
        Mockito.doReturn(Optional.of(expectedVersionNumber)).when(jenkinsWrapperMock).getVersion();

        JenkinsVersionHelper jenkinsVersionHelper = new JenkinsVersionHelper(jenkinsWrapperMock);
        Optional<String> actualVersionVersion = jenkinsVersionHelper.getJenkinsVersion();

        assertEquals(expectedVersionNumber.toString(), actualVersionVersion.orElse(null));
        assertTrue(actualVersionVersion.isPresent());
    }

}
