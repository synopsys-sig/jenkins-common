package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JenkinsProxyHelperTest {

    ProxyInfo proxyInfo;

    static String includeHost = "www.include.com";
    static String excludeHost = "www.exclude.com";

    static Pattern includePattern = Pattern.compile(includeHost);
    static Pattern excludePattern = Pattern.compile(excludeHost);

    static final String url = "https://" + includeHost;
    final String proxyHost = "proxyHost";
    final String proxyUsername = "proxyUsername";
    final String proxyPassword = "proxyPassword";
    final String ntlmDomain = "ntlmDomain";
    final String ntlmWorkstation = "ntlmWorkstation";

    private static Stream<Arguments> populateNoProxyInfoTests() {
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(includePattern);
        return Stream.of(
            Arguments.of("", patterns),
            Arguments.of(null, patterns),
            Arguments.of(url, patterns)
        );
    }

    private static Stream<Arguments> populateValidProxyInfoTests() {
        return Stream.of(
            Arguments.of(url, new ArrayList<>()),
            Arguments.of(url, null),
            Arguments.of(url, Arrays.asList(excludePattern))
        );
    }

    @ParameterizedTest
    @MethodSource("populateNoProxyInfoTests")
    public void testGetProxyInfo_NoProxyInfo(String inputUrl, List<Pattern> ignorePatterns) {
        // The returned proxyInfo object should be equal to ProxyInfo.NO_PROXY_INFO
        proxyInfo = JenkinsProxyHelper.getProxyInfo(inputUrl, proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, ignorePatterns, ntlmDomain, ntlmWorkstation);
        assertFalse(proxyInfo.getHost().isPresent());
        assertEquals(0, proxyInfo.getPort());
    }

    @ParameterizedTest
    @MethodSource("populateValidProxyInfoTests")
    public void testGetProxyInfo_PopulatedProxyInfo(String inputUrl, List<Pattern> ignorePatterns) {
        // The returned proxyInfo object should be populated with the values supplied to getProxyInfo
        proxyInfo = JenkinsProxyHelper.getProxyInfo(inputUrl, proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, ignorePatterns, ntlmDomain, ntlmWorkstation);
        assertEquals(proxyHost, proxyInfo.getHost().orElse(null));
        assertEquals(Integer.MAX_VALUE, proxyInfo.getPort());
    }

    @Test
    public void testGetProxyInfoFromJenkins_NullJenkins() {
        proxyInfo = JenkinsProxyHelper.getProxyInfoFromJenkins(url);
        assertFalse(proxyInfo.getHost().isPresent());
        assertEquals(0, proxyInfo.getPort());
    }

}
