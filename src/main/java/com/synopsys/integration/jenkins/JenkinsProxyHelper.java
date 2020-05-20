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
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private List<Pattern> ignoredProxyHosts;
    private String ntlmDomain;
    private String ntlmWorkstation;
    private Jenkins jenkins;
    private ProxyInfo proxyInfo = ProxyInfo.NO_PROXY_INFO;

    public JenkinsProxyHelper(Jenkins jenkins) {
        this.jenkins = jenkins;
        this.ntlmWorkstation = StringUtils.EMPTY;
    }

    public JenkinsProxyHelper(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword, List<Pattern> ignoredProxyHosts, String ntlmDomain,
        String ntlmWorkstation) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.ignoredProxyHosts = ignoredProxyHosts;
        this.ntlmDomain = ntlmDomain;
        this.ntlmWorkstation = ntlmWorkstation;
    }

    public ProxyInfo getProxyInfo(String url) {
        if (jenkins != null) {
            parseJenkinsForProxyConfig();
        }

        if (shouldUseProxy(url, ignoredProxyHosts)) {
            ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();

            CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
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

    private void parseJenkinsForProxyConfig() {
        ProxyConfiguration proxyConfiguration = jenkins.proxy;
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

        this.proxyHost = proxyConfiguration.name;
        this.proxyPort = proxyConfiguration.port;
        this.proxyUsername = username;
        this.proxyPassword = proxyConfiguration.getPassword();
        this.ignoredProxyHosts = proxyConfiguration.getNoProxyHostPatterns();
        this.ntlmDomain = ntlmDomain;
    }

    private boolean shouldUseProxy(String url, List<Pattern> noProxyHosts) {
        try {
            URL actualURL = new URL(url);
            return !shouldIgnoreHost(actualURL.getHost(), noProxyHosts);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean shouldIgnoreHost(String hostToMatch, List<Pattern> ignoredProxyHostPatterns) {
        if (StringUtils.isBlank(hostToMatch) || ignoredProxyHostPatterns == null || ignoredProxyHostPatterns.isEmpty()) {
            return false;
        }

        for (Pattern ignoredProxyHostPattern : ignoredProxyHostPatterns) {
            Matcher m = ignoredProxyHostPattern.matcher(hostToMatch);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

}
