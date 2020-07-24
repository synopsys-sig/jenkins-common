package com.synopsys.integration.jenkins.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;

import com.synopsys.integration.rest.proxy.ProxyInfo;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;

public class JenkinsProxyHelperTest {
    final static String expectedNtlmDomain = "ntlmDomain";
    final static String expectedProxyUsername = "proxyUsername";
    final static String expectedProxyHost = "proxyHost";
    final static String expectedProxyPassword = "proxyPassword";
    final static String expectedNtlmWorkstation = "ntlmWorkstation";
    final static int expectedProxyPort = 13;
    final static String hostToExclude = "www.exclude.com";
    final static String hostToInclude = "https://www.include.com";
    final static Pattern hostExcludePattern = Pattern.compile(hostToExclude);

    private static Stream<Arguments> noProxyInfo() {
        List<Pattern> ignoredProxyHosts = new ArrayList<>();
        ignoredProxyHosts.add(hostExcludePattern);

        return Stream.of(
            Arguments.of("", ignoredProxyHosts, null),
            Arguments.of(null, ignoredProxyHosts, null),
            Arguments.of("https://" + hostToExclude, ignoredProxyHosts, null)
        );
    }

    private static Stream<Arguments> noProxyInfoWithNtlmDomain() {
        List<Pattern> ignoredProxyHosts = new ArrayList<>();
        ignoredProxyHosts.add(hostExcludePattern);

        return Stream.of(
            Arguments.of("", ignoredProxyHosts, expectedNtlmDomain),
            Arguments.of(null, ignoredProxyHosts, expectedNtlmDomain + "\\" + expectedProxyUsername),
            Arguments.of("https://" + hostToExclude, ignoredProxyHosts, "\\" + expectedProxyUsername)
        );
    }

    private static Stream<Arguments> validProxyInfoInputTestData() {
        return Stream.of(
            Arguments.of(hostToInclude, new ArrayList<>(), null, expectedProxyUsername),
            Arguments.of(hostToInclude, null, null, expectedProxyUsername),
            Arguments.of(hostToInclude, Arrays.asList(hostExcludePattern), null, expectedProxyUsername),
            Arguments.of(hostToInclude, new ArrayList<>(), expectedNtlmDomain, expectedNtlmDomain + "\\" + expectedProxyUsername),
            Arguments.of(hostToInclude, null, expectedNtlmDomain, expectedNtlmDomain + "\\" + expectedProxyUsername),
            Arguments.of(hostToInclude, Arrays.asList(hostExcludePattern), null, expectedProxyUsername)
        );
    }

    @Test
    public void testFromJenkinsNullJenkins() {
        JenkinsWrapper jenkinsWrapper = JenkinsWrapper.initializeFromJenkinsJVM();
        ProxyInfo proxyInfo = JenkinsProxyHelper.fromJenkins(jenkinsWrapper).getProxyInfo("www.null-jenkins.com");
        noProxyInfoValidation(proxyInfo);
    }

    @ParameterizedTest
    @MethodSource("noProxyInfo")
    public void testConstructorNoProxyInfo(String url, List<Pattern> ignoredProxyHosts, String testNtlmDomain) {
        ProxyInfo proxyInfo = new JenkinsProxyHelper(expectedProxyHost, expectedProxyPort, expectedProxyUsername, expectedProxyPassword, ignoredProxyHosts, testNtlmDomain, expectedNtlmWorkstation).getProxyInfo(url);
        noProxyInfoValidation(proxyInfo);
    }

    @ParameterizedTest
    @MethodSource({ "noProxyInfo", "noProxyInfoWithNtlmDomain" })
    public void testFromJenkinsNoProxyInfoJenkins(String url, List<Pattern> ignoredProxyHosts, String proxyGetUserName) {
        ProxyConfiguration proxyConfigurationMock = Mockito.mock(ProxyConfiguration.class);
        Jenkins jenkinsMock = Mockito.mock(Jenkins.class);
        JenkinsWrapper jenkinsWrapper = new JenkinsWrapper(jenkinsMock);

        setJenkinsFields(jenkinsMock, proxyConfigurationMock);

        Mockito.when(proxyConfigurationMock.getUserName()).thenReturn(proxyGetUserName);
        Mockito.when(proxyConfigurationMock.getPassword()).thenReturn(expectedProxyPassword);
        Mockito.when(proxyConfigurationMock.getNoProxyHostPatterns()).thenReturn(ignoredProxyHosts);

        ProxyInfo proxyInfo = JenkinsProxyHelper.fromJenkins(jenkinsWrapper).getProxyInfo(url);
        noProxyInfoValidation(proxyInfo);
    }

    private void noProxyInfoValidation(ProxyInfo proxyInfo) {
        assertEquals(ProxyInfo.NO_PROXY_INFO.getProxy(), proxyInfo.getProxy());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getHost(), proxyInfo.getHost());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getPort(), proxyInfo.getPort());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getProxyCredentials(), proxyInfo.getProxyCredentials());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getUsername(), proxyInfo.getUsername());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getPassword(), proxyInfo.getPassword());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getNtlmDomain(), proxyInfo.getNtlmDomain());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getNtlmWorkstation(), proxyInfo.getNtlmWorkstation());
    }

    @ParameterizedTest
    @MethodSource({ "validProxyInfoInputTestData" })
    public void testConstructorValidProxyInfo(String url, List<Pattern> ignoredProxyHosts, String testNtlmDomain) {
        ProxyInfo proxyInfo = new JenkinsProxyHelper(expectedProxyHost, expectedProxyPort, expectedProxyUsername, expectedProxyPassword, ignoredProxyHosts, testNtlmDomain, expectedNtlmWorkstation).getProxyInfo(url);
        validProxyInfoValidation(proxyInfo, testNtlmDomain, expectedNtlmWorkstation);
    }

    @ParameterizedTest
    @MethodSource({ "validProxyInfoInputTestData" })
    public void testFromJenkinsValidProxyInfoJenkins(String url, List<Pattern> ignoredProxyHosts, String expectedNtlmDomain, String proxyGetUserName) {
        ProxyConfiguration proxyConfigurationMock = Mockito.mock(ProxyConfiguration.class);
        Jenkins jenkinsMock = Mockito.mock(Jenkins.class);
        JenkinsWrapper jenkinsWrapper = new JenkinsWrapper(jenkinsMock);

        setJenkinsFields(jenkinsMock, proxyConfigurationMock);

        Mockito.when(proxyConfigurationMock.getUserName()).thenReturn(proxyGetUserName);
        Mockito.when(proxyConfigurationMock.getPassword()).thenReturn(expectedProxyPassword);
        Mockito.when(proxyConfigurationMock.getNoProxyHostPatterns()).thenReturn(ignoredProxyHosts);

        ProxyInfo proxyInfo = JenkinsProxyHelper.fromJenkins(jenkinsWrapper).getProxyInfo(url);
        validProxyInfoValidation(proxyInfo, expectedNtlmDomain, StringUtils.EMPTY);
    }

    private void validProxyInfoValidation(ProxyInfo proxyInfo, String ntlmDomain, String ntlmWorkstation) {
        assertTrue(proxyInfo.getProxy().isPresent());
        assertTrue(proxyInfo.getHost().isPresent());
        assertEquals(expectedProxyHost, proxyInfo.getHost().get());
        assertEquals(expectedProxyPort, proxyInfo.getPort());
        assertTrue(proxyInfo.getProxyCredentials().isPresent());
        assertEquals(expectedProxyUsername, proxyInfo.getUsername().orElse(null));
        assertEquals(expectedProxyPassword, proxyInfo.getPassword().orElse(null));
        assertEquals(ntlmDomain, proxyInfo.getNtlmDomain().orElse(null));
        assertEquals(ntlmWorkstation, proxyInfo.getNtlmWorkstation().orElse(StringUtils.EMPTY));
    }

    private void setJenkinsFields(Jenkins jenkins, ProxyConfiguration proxyConfiguration) {
        try {
            FieldSetter.setField(jenkins, jenkins.getClass().getField("proxy"), proxyConfiguration);
            FieldSetter.setField(proxyConfiguration, proxyConfiguration.getClass().getDeclaredField("name"), expectedProxyHost);
            FieldSetter.setField(proxyConfiguration, proxyConfiguration.getClass().getDeclaredField("port"), expectedProxyPort);
        } catch (NoSuchFieldException exception) {
            fail("Jenkins has changed it's API: ", exception);
        }
    }
}
