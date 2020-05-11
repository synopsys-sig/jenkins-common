package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JenkinsProxyHelperTest {

    final String proxyHost = "proxyHost";
    final String proxyUsername = "proxyUsername";
    final String proxyPassword = "proxyPassword";
    final String ntlmDomain = "ntlmDomain";
    final String ntlmWorkstation = "ntlmWorkstation";
    final int proxyPort = 13;

    private static Stream<Arguments> populateNoProxyInfoTests() {
        String hostToExclude = "www.exclude.com";
        Pattern hostExcludePattern = Pattern.compile(hostToExclude);
        List<Pattern> ignoredProxyHosts = new ArrayList<>();
        ignoredProxyHosts.add(hostExcludePattern);

        return Stream.of(
            Arguments.of("", ignoredProxyHosts),
            Arguments.of(null, ignoredProxyHosts),
            Arguments.of("https://" + hostToExclude, ignoredProxyHosts)
        );
    }

    private static Stream<Arguments> populateValidProxyInfoTests() {
        String hostToInclude = "https://www.include.com";
        String hostToExclude = "www.exclude.com";
        Pattern hostExcludePattern = Pattern.compile(hostToExclude);

        return Stream.of(
            Arguments.of(hostToInclude, new ArrayList<>()),
            Arguments.of(hostToInclude, null),
            Arguments.of(hostToInclude, Arrays.asList(hostExcludePattern))
        );
    }

    @ParameterizedTest
    @MethodSource("populateNoProxyInfoTests")
    public void testGetProxyInfo_NoProxyInfo(String url, List<Pattern> ignoredProxyHosts) {
        // The returned proxyInfo object should be equal to ProxyInfo.NO_PROXY_INFO
        ProxyInfo proxyInfo = JenkinsProxyHelper.getProxyInfo(url, proxyHost, proxyPort, proxyUsername, proxyPassword, ignoredProxyHosts, ntlmDomain, ntlmWorkstation);
        assertEquals(ProxyInfo.NO_PROXY_INFO.getProxy().isPresent(), proxyInfo.getProxy().isPresent());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getHost().isPresent(), proxyInfo.getHost().isPresent());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getPort(), proxyInfo.getPort());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getProxyCredentials().isPresent(), proxyInfo.getProxyCredentials().isPresent());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getUsername(), proxyInfo.getUsername());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getPassword(), proxyInfo.getPassword());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getNtlmDomain(), proxyInfo.getNtlmDomain());
        assertEquals(ProxyInfo.NO_PROXY_INFO.getNtlmWorkstation(), proxyInfo.getNtlmWorkstation());
    }

    @ParameterizedTest
    @MethodSource("populateValidProxyInfoTests")
    public void testGetProxyInfo_PopulatedProxyInfo(String url, List<Pattern> ignoredProxyHosts) {
        // The returned proxyInfo object should be populated with the values supplied to getProxyInfo
        ProxyInfo proxyInfo = JenkinsProxyHelper.getProxyInfo(url, proxyHost, proxyPort, proxyUsername, proxyPassword, ignoredProxyHosts, ntlmDomain, ntlmWorkstation);
        assertTrue(proxyInfo.getProxy().isPresent());
        assertTrue(proxyInfo.getHost().isPresent());
        assertEquals(proxyHost, proxyInfo.getHost().orElse(null));
        assertEquals(proxyPort, proxyInfo.getPort());
        assertTrue(proxyInfo.getProxyCredentials().isPresent());
        assertEquals(Optional.of(proxyUsername), proxyInfo.getUsername());
        assertEquals(Optional.of(proxyPassword), proxyInfo.getPassword());
        assertEquals(Optional.of(ntlmDomain), proxyInfo.getNtlmDomain());
        assertEquals(Optional.of(ntlmWorkstation), proxyInfo.getNtlmWorkstation());
    }

    @Test
    public void testGetProxyInfoFromJenkins_NullJenkins() {
        String url = "https://www.exclude.com";
        ProxyInfo proxyInfo = JenkinsProxyHelper.getProxyInfoFromJenkins(url);
        assertFalse(proxyInfo.getHost().isPresent());
        assertEquals(0, proxyInfo.getPort());
    }

}
