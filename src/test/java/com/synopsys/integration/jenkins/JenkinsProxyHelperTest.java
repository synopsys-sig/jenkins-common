package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    final static String ntlmDomain = "ntlmDomain";

    final String proxyHost = "proxyHost";
    final String proxyUsername = "proxyUsername";
    final String proxyPassword = "proxyPassword";
    final String ntlmWorkstation = "ntlmWorkstation";
    final int proxyPort = 13;

    private static Stream<Arguments> noProxyInfo() {
        final String hostToExclude = "www.exclude.com";
        Pattern hostExcludePattern = Pattern.compile(hostToExclude);
        List<Pattern> ignoredProxyHosts = new ArrayList<>();
        ignoredProxyHosts.add(hostExcludePattern);

        return Stream.of(
            Arguments.of("", ignoredProxyHosts, null),
            Arguments.of(null, ignoredProxyHosts, null),
            Arguments.of("https://" + hostToExclude, ignoredProxyHosts, null)
        );
    }

    private static Stream<Arguments> noProxyInfoWithNtlmDomain() {
        final String hostToExclude = "www.exclude.com";
        Pattern hostExcludePattern = Pattern.compile(hostToExclude);
        List<Pattern> ignoredProxyHosts = new ArrayList<>();
        ignoredProxyHosts.add(hostExcludePattern);

        return Stream.of(
            Arguments.of("", ignoredProxyHosts, ntlmDomain),
            Arguments.of(null, ignoredProxyHosts, ntlmDomain),
            Arguments.of("https://" + hostToExclude, ignoredProxyHosts, ntlmDomain)
        );
    }

    private static Stream<Arguments> validProxyInfo() {
        final String hostToInclude = "https://www.include.com";
        final String hostToExclude = "www.exclude.com";
        Pattern hostExcludePattern = Pattern.compile(hostToExclude);

        return Stream.of(
            Arguments.of(hostToInclude, new ArrayList<>(), null),
            Arguments.of(hostToInclude, null, null),
            Arguments.of(hostToInclude, Arrays.asList(hostExcludePattern), null)
        );
    }

    private static Stream<Arguments> validProxyInfoWithNtlmDomain() {
        final String hostToInclude = "https://www.include.com";
        final String hostToExclude = "www.exclude.com";
        Pattern hostExcludePattern = Pattern.compile(hostToExclude);

        return Stream.of(
            Arguments.of(hostToInclude, new ArrayList<>(), ntlmDomain),
            Arguments.of(hostToInclude, null, ntlmDomain),
            Arguments.of(hostToInclude, Arrays.asList(hostExcludePattern), ntlmDomain)
        );
    }

    @Test
    public void testJenkinsProxyHelperNullJenkins() {
        ProxyInfo proxyInfo = JenkinsProxyHelper.fromJenkins(null).getProxyInfo("www.null-jenkins.com");
        noProxyInfoValidation(proxyInfo);
    }

    @ParameterizedTest
    @MethodSource("noProxyInfo")
    public void testJenkinsProxyHelperNoProxyInfo(String url, List<Pattern> ignoredProxyHosts, String testNtlmDomain) {
        ProxyInfo proxyInfo = new JenkinsProxyHelper(proxyHost, proxyPort, proxyUsername, proxyPassword, ignoredProxyHosts, testNtlmDomain, ntlmWorkstation).getProxyInfo(url);
        noProxyInfoValidation(proxyInfo);
    }

    @ParameterizedTest
    @MethodSource({ "noProxyInfo", "noProxyInfoWithNtlmDomain" })
    public void testJenkinsProxyHelperNoProxyInfoJenkins(String url, List<Pattern> ignoredProxyHosts, String testNtlmDomain) throws NoSuchFieldException {
        String returnProxyUserName = (testNtlmDomain == null ? "" : ntlmDomain + "\\") + proxyUsername;
        ProxyConfiguration proxyConfiguration = Mockito.mock(ProxyConfiguration.class);
        Jenkins jenkins = Mockito.mock(Jenkins.class);
        FieldSetter.setField(jenkins, jenkins.getClass().getField("proxy"), proxyConfiguration);
        FieldSetter.setField(proxyConfiguration, proxyConfiguration.getClass().getDeclaredField("name"), proxyHost);
        FieldSetter.setField(proxyConfiguration, proxyConfiguration.getClass().getDeclaredField("port"), proxyPort);
        Mockito.when(proxyConfiguration.getUserName()).thenReturn(returnProxyUserName);
        Mockito.when(proxyConfiguration.getPassword()).thenReturn(proxyPassword);
        Mockito.when(proxyConfiguration.getNoProxyHostPatterns()).thenReturn(ignoredProxyHosts);

        ProxyInfo proxyInfo = JenkinsProxyHelper.fromJenkins(jenkins).getProxyInfo(url);
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
    @MethodSource({ "validProxyInfo", "validProxyInfoWithNtlmDomain" })
    public void testJenkinsProxyHelperValidProxyInfo(String url, List<Pattern> ignoredProxyHosts, String testProxyUsername) {
        ProxyInfo proxyInfo = new JenkinsProxyHelper(proxyHost, proxyPort, proxyUsername, proxyPassword, ignoredProxyHosts, testProxyUsername, ntlmWorkstation).getProxyInfo(url);
        validProxyInfoValidation(proxyInfo, proxyHost, proxyPort, proxyUsername, proxyPassword, testProxyUsername, ntlmWorkstation);
    }

    @ParameterizedTest
    @MethodSource({ "validProxyInfo", "validProxyInfoWithNtlmDomain" })
    public void testJenkinsProxyHelperValidProxyInfoJenkins(String url, List<Pattern> ignoredProxyHosts, String testNtlmDomain) throws NoSuchFieldException {
        String returnProxyUserName = (testNtlmDomain == null ? "" : ntlmDomain + "\\") + proxyUsername;
        ProxyConfiguration proxyConfiguration = Mockito.mock(ProxyConfiguration.class);
        Jenkins jenkins = Mockito.mock(Jenkins.class);
        FieldSetter.setField(jenkins, jenkins.getClass().getField("proxy"), proxyConfiguration);
        FieldSetter.setField(proxyConfiguration, proxyConfiguration.getClass().getDeclaredField("name"), proxyHost);
        FieldSetter.setField(proxyConfiguration, proxyConfiguration.getClass().getDeclaredField("port"), proxyPort);
        Mockito.when(proxyConfiguration.getUserName()).thenReturn(returnProxyUserName);
        Mockito.when(proxyConfiguration.getPassword()).thenReturn(proxyPassword);
        Mockito.when(proxyConfiguration.getNoProxyHostPatterns()).thenReturn(ignoredProxyHosts);

        ProxyInfo proxyInfo = JenkinsProxyHelper.fromJenkins(jenkins).getProxyInfo(url);
        validProxyInfoValidation(proxyInfo, proxyHost, proxyPort, proxyUsername, proxyPassword, testNtlmDomain, StringUtils.EMPTY);
    }

    private void validProxyInfoValidation(ProxyInfo proxyInfo, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword,
        String ntlmDomain, String ntlmWorkstation) {
        assertTrue(proxyInfo.getProxy().isPresent());
        assertTrue(proxyInfo.getHost().isPresent());
        assertEquals(proxyHost, proxyInfo.getHost().get());
        assertEquals(proxyPort, proxyInfo.getPort());
        assertTrue(proxyInfo.getProxyCredentials().isPresent());
        assertEquals(proxyUsername, proxyInfo.getUsername().get());
        assertEquals(proxyPassword, proxyInfo.getPassword().get());
        assertEquals(ntlmDomain, proxyInfo.getNtlmDomain().orElse(null));
        assertEquals(ntlmWorkstation, proxyInfo.getNtlmWorkstation().orElse(StringUtils.EMPTY));
    }

}
