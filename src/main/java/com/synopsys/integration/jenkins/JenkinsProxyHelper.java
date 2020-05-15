/**
 * jenkins-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.jenkins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;

public class JenkinsProxyHelper {
    private String url;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private List<Pattern> ignoredProxyHosts;
    private String ntlmDomain;
    private String ntlmWorkstation;
    private ProxyInfo proxyInfo = ProxyInfo.NO_PROXY_INFO;

    public JenkinsProxyHelper(final ProxyConfiguration proxyConfiguration, final String url) {
        this.url = url;

        this.proxyHost = proxyConfiguration.name;
        this.proxyPort = proxyConfiguration.port;
        this.proxyPassword = proxyConfiguration.getPassword();
        this.ignoredProxyHosts = proxyConfiguration.getNoProxyHostPatterns();
        this.ntlmWorkstation = StringUtils.EMPTY;

        if (StringUtils.isNotBlank(proxyConfiguration.getUserName())) {
            final String[] possiblyDomainSlashUsername = proxyConfiguration.getUserName().split(Pattern.quote("\\"));
            if (possiblyDomainSlashUsername.length == 1 || possiblyDomainSlashUsername[0].length() == 0) {
                this.ntlmDomain = null;
                this.proxyUsername = proxyConfiguration.getUserName();
            } else {
                this.ntlmDomain = possiblyDomainSlashUsername[0];
                this.proxyUsername = possiblyDomainSlashUsername[1];
            }
        } else {
            this.ntlmDomain = null;
            this.proxyUsername = null;
        }
    }

    public JenkinsProxyHelper(final String url, final String proxyHost, final int proxyPort, final String proxyUsername, final String proxyPassword, final List<Pattern> ignoredProxyHosts, final String ntlmDomain,
        String ntlmWorkstation) {
        this.url = url;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.ignoredProxyHosts = ignoredProxyHosts;
        this.ntlmDomain = ntlmDomain;
        this.ntlmWorkstation = ntlmWorkstation;
    }

    public ProxyInfo getProxyInfo() {
        if (shouldUseProxy(url, ignoredProxyHosts)) {
            final ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();

            final CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
            credentialsBuilder.setUsernameAndPassword(proxyUsername, proxyPassword);

            proxyInfoBuilder.setHost(proxyHost);
            proxyInfoBuilder.setPort(proxyPort);
            proxyInfoBuilder.setCredentials(credentialsBuilder.build());
            proxyInfoBuilder.setNtlmDomain(StringUtils.trimToNull(ntlmDomain));
            proxyInfoBuilder.setNtlmWorkstation(StringUtils.trimToNull(ntlmWorkstation));

            proxyInfo = proxyInfoBuilder.build();
        }

        return proxyInfo;
    }

    private boolean shouldUseProxy(final String url, final List<Pattern> noProxyHosts) {
        try {
            final URL actualURL = new URL(url);
            return !shouldIgnoreHost(actualURL.getHost(), noProxyHosts);
        } catch (final MalformedURLException e) {
            return false;
        }
    }

    private boolean shouldIgnoreHost(final String hostToMatch, final List<Pattern> ignoredProxyHostPatterns) {
        if (StringUtils.isBlank(hostToMatch) || ignoredProxyHostPatterns == null || ignoredProxyHostPatterns.isEmpty()) {
            return false;
        }

        for (final Pattern ignoredProxyHostPattern : ignoredProxyHostPatterns) {
            final Matcher m = ignoredProxyHostPattern.matcher(hostToMatch);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static ProxyInfo getProxyInfoFromJenkins(final String url) {
        final ProxyConfiguration proxyConfiguration = Optional.ofNullable(Jenkins.getInstanceOrNull())
                                                    .map(jenkins -> jenkins.proxy)
                                                    .orElse(null);

        if (proxyConfiguration == null) {
            return ProxyInfo.NO_PROXY_INFO;
        } else {
            return new JenkinsProxyHelper(proxyConfiguration, url).getProxyInfo();
        }
    }

    @Deprecated
    public static ProxyInfo getProxyInfo(final String url, final String proxyHost, final int proxyPort, final String proxyUsername, final String proxyPassword, final List<Pattern> ignoredProxyHosts, final String ntlmDomain,
        final String ntlmWorkstation) {
        return new JenkinsProxyHelper(url, proxyHost, proxyPort, proxyUsername, proxyPassword, ignoredProxyHosts, ntlmDomain, ntlmWorkstation).getProxyInfo();
    }

}
