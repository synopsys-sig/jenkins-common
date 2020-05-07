package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JenkinsProxyHelperTest {

    ProxyInfo proxyInfo;
    List<Pattern> patterns = new ArrayList<>();

    final String url_1 = "www.include.com";
    final String url_2 = "www.exclude.com";

    Pattern pattern_1 = Pattern.compile(url_1);
    Pattern pattern_2 = Pattern.compile(url_2);

    final String protocol = "https://";
    final String proxyHost = "proxyHost";
    final String proxyUsername = "proxyUsername";
    final String proxyPassword = "proxyPassword";
    final String ntlmDomain = "ntlmDomain";
    final String ntlmWorkstation = "ntlmWorkstation";

    @Test
    public void testGetProxyInfoFromJenkins_NullJenkins() {
        proxyInfo = JenkinsProxyHelper.getProxyInfoFromJenkins(protocol + url_1);
        assertFalse(proxyInfo.getHost().isPresent());
        assertEquals(0, proxyInfo.getPort());
    }

    @Test
    public void testGetProxyInfo_EmptyUrlMatchingExclude() {
        // The returning proxyInfo object should be equal to ProxyInfo.NO_PROXY_INFO
        patterns.clear();
        patterns.add(pattern_1);
        proxyInfo = JenkinsProxyHelper.getProxyInfo("", proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, patterns, ntlmDomain, ntlmWorkstation);
        assertFalse(proxyInfo.getHost().isPresent());
        assertEquals(0, proxyInfo.getPort());
    }

    @Test
    public void testGetProxyInfo_NullUrlMatchingExclude() {
        // The returning proxyInfo object should be equal to ProxyInfo.NO_PROXY_INFO
        patterns.clear();
        patterns.add(pattern_1);
        proxyInfo = JenkinsProxyHelper.getProxyInfo(null, proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, patterns, ntlmDomain, ntlmWorkstation);
        assertFalse(proxyInfo.getHost().isPresent());
        assertEquals(0, proxyInfo.getPort());
    }

    @Test
    public void testGetProxyInfo_ValidUrlMatchingExclude() {
        // The returning proxyInfo object should be equal to ProxyInfo.NO_PROXY_INFO
        patterns.clear();
        patterns.add(pattern_1);
        proxyInfo = JenkinsProxyHelper.getProxyInfo(protocol + url_1, proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, patterns, ntlmDomain, ntlmWorkstation);
        assertFalse(proxyInfo.getHost().isPresent());
        assertEquals(0, proxyInfo.getPort());
    }

    @Test
    public void testGetProxyInfo_ValidUrlEmptyExclude() {
        // The returning proxyInfo object should be populated with the values supplied to getProxyInfo
        patterns.clear();
        proxyInfo = JenkinsProxyHelper.getProxyInfo(protocol + url_1, proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, patterns, ntlmDomain, ntlmWorkstation);
        assertEquals(proxyHost, proxyInfo.getHost().orElse(null));
        assertEquals(Integer.MAX_VALUE, proxyInfo.getPort());
    }

    @Test
    public void testGetProxyInfo_ValidUrlNullExclude() {
        // The returning proxyInfo object should be populated with the values supplied to getProxyInfo
        proxyInfo = JenkinsProxyHelper.getProxyInfo(protocol + url_1, proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, null, ntlmDomain, ntlmWorkstation);
        assertEquals(proxyUsername, proxyInfo.getUsername().orElse(null));
        assertEquals(proxyPassword, proxyInfo.getPassword().orElse(null));
    }

    @Test
    public void testGetProxyInfo_ValidUrlNonMatchingExclude() {
        // The returning proxyInfo object should be populated with the values supplied to getProxyInfo
        patterns.clear();
        patterns.add(pattern_2);
        proxyInfo = JenkinsProxyHelper.getProxyInfo(protocol + url_1, proxyHost, Integer.MAX_VALUE, proxyUsername, proxyPassword, patterns, ntlmDomain, ntlmWorkstation);
        assertEquals(ntlmDomain, proxyInfo.getNtlmDomain().orElse(null));
        assertEquals(ntlmWorkstation, proxyInfo.getNtlmWorkstation().orElse(null));
    }
}
