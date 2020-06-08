package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import hudson.Plugin;
import hudson.PluginWrapper;
import jenkins.model.Jenkins;

public class JenkinsVersionHelperTest {

    private static String pluginName = "jenkins-common";

    @Test
    public void testJenkinsVersionHelper() {
        String expectedPluginVersion = "3.13.39";

        Jenkins jenkinsMock = Mockito.mock(Jenkins.class);
        Plugin pluginMock = Mockito.mock(Plugin.class);
        PluginWrapper pluginWrapperMock = Mockito.mock(PluginWrapper.class);

        JenkinsVersionHelper jenkinsVersionHelper = new JenkinsVersionHelper(jenkinsMock);

        Mockito.when(jenkinsMock.getPlugin(pluginName)).thenReturn(pluginMock);
        Mockito.when(pluginMock.getWrapper()).thenReturn(pluginWrapperMock);
        Mockito.when(pluginWrapperMock.getVersion()).thenReturn(expectedPluginVersion);

        Optional<String> actualPluginVersion = jenkinsVersionHelper.getPluginVersion(pluginName);

        assertEquals(expectedPluginVersion, actualPluginVersion.get());
        assertTrue(actualPluginVersion.isPresent());
    }

    @Test
    public void testJenkinsVersionHelperNull() {
        JenkinsVersionHelper jenkinsVersionHelper = new JenkinsVersionHelper(null);
        Optional<String> actualPluginVersion = jenkinsVersionHelper.getPluginVersion(pluginName);

        assertFalse(actualPluginVersion.isPresent());
    }
    
}
