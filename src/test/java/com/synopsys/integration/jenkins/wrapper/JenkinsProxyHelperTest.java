package com.synopsys.integration.jenkins.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

import hudson.ProxyConfiguration;

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
            Arguments.of(new ArrayList<>(), null, expectedProxyUsername),
            Arguments.of(null, null, expectedProxyUsername),
            Arguments.of(Collections.singletonList(hostExcludePattern), null, expectedProxyUsername),
            Arguments.of(new ArrayList<>(), expectedNtlmDomain, expectedNtlmDomain + "\\" + expectedProxyUsername),
            Arguments.of(null, expectedNtlmDomain, expectedNtlmDomain + "\\" + expectedProxyUsername),
            Arguments.of(Collections.singletonList(hostExcludePattern), null, expectedProxyUsername)
        );
    }

    @Test
    public void testFromJenkinsNullProxyConfig() {
        ProxyInfo actual = JenkinsProxyHelper.fromJenkins(JenkinsWrapper.initializeFromJenkinsJVM()).getProxyInfo("www.null-jenkins.com");
        assertEquals(ProxyInfo.NO_PROXY_INFO, actual);
    }

    @ParameterizedTest
    @MethodSource("noProxyInfo")
    public void testConstructorNoProxyInfo(String url, List<Pattern> ignoredProxyHosts, String testNtlmDomain) {
        ProxyInfo actual = new JenkinsProxyHelper(expectedProxyHost, expectedProxyPort, expectedProxyUsername, expectedProxyPassword, ignoredProxyHosts, testNtlmDomain, expectedNtlmWorkstation).getProxyInfo(url);
        assertEquals(ProxyInfo.NO_PROXY_INFO, actual);
    }

    @ParameterizedTest
    @MethodSource({ "validProxyInfoInputTestData" })
    public void testConstructorValidProxyInfo(List<Pattern> ignoredProxyHosts, String expectedNtlmDomain, String unusedProxyUsername) {
        ProxyInfo expected = createExpectedProxyInfo(expectedNtlmDomain, expectedNtlmWorkstation);
        ProxyInfo actual = new JenkinsProxyHelper(expectedProxyHost, expectedProxyPort, expectedProxyUsername, expectedProxyPassword, ignoredProxyHosts, expectedNtlmDomain, expectedNtlmWorkstation).getProxyInfo(hostToInclude);
        assertEquals(expected, actual);
    }

    @Test
    public void testFromProxyConfigurationNullProxyConfig() {
        ProxyInfo actual = JenkinsProxyHelper.fromProxyConfiguration(null).getProxyInfo("www.null-jenkins.com");
        assertEquals(ProxyInfo.NO_PROXY_INFO, actual);
    }

    @ParameterizedTest
    @MethodSource({ "noProxyInfo", "noProxyInfoWithNtlmDomain" })
    public void testFromProxyConfigurationNoProxyInfo(String url, List<Pattern> unusedProxyHosts, String testUserName) {
        ProxyConfiguration inputProxyConfiguration = createProxyConfiguration(testUserName, null);
        ProxyInfo actual = JenkinsProxyHelper.fromProxyConfiguration(inputProxyConfiguration).getProxyInfo(url);
        assertEquals(ProxyInfo.NO_PROXY_INFO, actual);
    }

    @ParameterizedTest
    @MethodSource({ "validProxyInfoInputTestData" })
    public void testFromProxyConfigurationValidProxyConfig(List<Pattern> unusedProxyHosts, String expectedNtlmDomain, String expectedProxyUsername) {
        ProxyInfo expected = createExpectedProxyInfo(expectedNtlmDomain,null);
        ProxyConfiguration inputProxyConfiguration = createProxyConfiguration(expectedProxyUsername, expectedProxyPassword);
        ProxyInfo actual = JenkinsProxyHelper.fromProxyConfiguration(inputProxyConfiguration).getProxyInfo(hostToInclude);
        assertEquals(expected, actual);
    }

    private ProxyInfo createExpectedProxyInfo(String ntlmDomain, String ntlmWorkstation) {
        ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
        CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        credentialsBuilder.setUsernameAndPassword(expectedProxyUsername, expectedProxyPassword);
        credentialsBuilder.setPassword(expectedProxyPassword);
        proxyInfoBuilder.setHost(expectedProxyHost);
        proxyInfoBuilder.setPort(expectedProxyPort);
        proxyInfoBuilder.setNtlmDomain(ntlmDomain);
        proxyInfoBuilder.setNtlmWorkstation(ntlmWorkstation);
        proxyInfoBuilder.setCredentials(credentialsBuilder.build());

        return proxyInfoBuilder.build();
    }

    private ProxyConfiguration createProxyConfiguration(String userName, String expectedPassWord) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(expectedProxyHost, expectedProxyPort, userName, null, hostToExclude);

        if (expectedPassWord == null || expectedPassWord.isEmpty()) {
            return proxyConfiguration;
        } else {
            ProxyConfiguration spiedProxyConfiguration = Mockito.spy(proxyConfiguration);
            Mockito.doReturn(expectedPassWord).when(spiedProxyConfiguration).getPassword();
            return spiedProxyConfiguration;
        }
    }

}
