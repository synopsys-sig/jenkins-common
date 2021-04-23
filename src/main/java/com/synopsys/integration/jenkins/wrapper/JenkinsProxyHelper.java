/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.wrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

import hudson.ProxyConfiguration;

public class JenkinsProxyHelper {
    public static final JenkinsProxyHelper NO_PROXY = new JenkinsProxyHelper();

    private final boolean isBlank;

    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;
    private final List<Pattern> ignoredProxyHosts;
    private final String ntlmDomain;
    private final String ntlmWorkstation;

    public JenkinsProxyHelper(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword, List<Pattern> ignoredProxyHosts, String ntlmDomain,
        String ntlmWorkstation) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.ignoredProxyHosts = ignoredProxyHosts;
        this.ntlmDomain = ntlmDomain;
        this.ntlmWorkstation = ntlmWorkstation;
        this.isBlank = false;
    }

    public JenkinsProxyHelper() {
        this.proxyHost = null;
        this.proxyPort = 0;
        this.proxyUsername = null;
        this.proxyPassword = null;
        this.ignoredProxyHosts = null;
        this.ntlmDomain = null;
        this.ntlmWorkstation = null;
        this.isBlank = true;
    }

    public static JenkinsProxyHelper fromProxyConfiguration(ProxyConfiguration proxyConfiguration) {
        if (proxyConfiguration == null) {
            return NO_PROXY;
        }

        String username = null;
        String ntlmDomain = null;
        if (StringUtils.isNotBlank(proxyConfiguration.getUserName())) {
            String[] possiblyDomainSlashUsername = proxyConfiguration.getUserName().split(Pattern.quote("\\"));
            if (possiblyDomainSlashUsername.length == 1 || possiblyDomainSlashUsername[0].length() == 0) {
                ntlmDomain = null;
                username = proxyConfiguration.getUserName();
            } else {
                ntlmDomain = possiblyDomainSlashUsername[0];
                username = possiblyDomainSlashUsername[1];
            }
        }

        return new JenkinsProxyHelper(proxyConfiguration.name, proxyConfiguration.port, username, proxyConfiguration.getPassword(), proxyConfiguration.getNoProxyHostPatterns(), ntlmDomain, null);
    }

    public ProxyInfo getProxyInfo(String url) {
        if (isBlank || shouldNotUseProxy(url)) {
            return ProxyInfo.NO_PROXY_INFO;
        }

        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword(proxyUsername, proxyPassword);

        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
        proxyInfoBuilder.setHost(proxyHost);
        proxyInfoBuilder.setPort(proxyPort);
        proxyInfoBuilder.setCredentials(credentialsBuilder.build());
        proxyInfoBuilder.setNtlmDomain(StringUtils.trimToNull(ntlmDomain));
        proxyInfoBuilder.setNtlmWorkstation(StringUtils.trimToNull(ntlmWorkstation));

        return proxyInfoBuilder.build();
    }

    private boolean shouldNotUseProxy(String url) {
        if (ignoredProxyHosts == null || ignoredProxyHosts.isEmpty()) {
            return false;
        }

        try {
            URL actualURL = new URL(url);

            for (Pattern ignoredProxyHostPattern : ignoredProxyHosts) {
                Matcher m = ignoredProxyHostPattern.matcher(actualURL.getHost());
                if (m.matches()) {
                    return true;
                }
            }

        } catch (MalformedURLException e) {
            return true;
        }
        return false;
    }

}
