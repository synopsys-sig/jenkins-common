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
    private static final JenkinsProxyHelper NO_PROXY = new JenkinsProxyHelper();
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

    public static JenkinsProxyHelper fromJenkins(Jenkins jenkins) {
        ProxyConfiguration proxyConfig = Optional.ofNullable(jenkins)
                                             .map(instance -> instance.proxy)
                                             .orElse(null);
        if (proxyConfig == null) {
            return NO_PROXY;
        }

        String username = null;
        String ntlmDomain = null;
        if (StringUtils.isNotBlank(proxyConfig.getUserName())) {
            String[] possiblyDomainSlashUsername = proxyConfig.getUserName().split(Pattern.quote("\\"));
            if (possiblyDomainSlashUsername.length == 1 || possiblyDomainSlashUsername[0].length() == 0) {
                ntlmDomain = null;
                username = proxyConfig.getUserName();
            } else {
                ntlmDomain = possiblyDomainSlashUsername[0];
                username = possiblyDomainSlashUsername[1];
            }
        }

        return new JenkinsProxyHelper(proxyConfig.name, proxyConfig.port, username, proxyConfig.getPassword(), proxyConfig.getNoProxyHostPatterns(), ntlmDomain, StringUtils.EMPTY);
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
        try {
            URL actualURL = new URL(url);
            return shouldIgnoreHost(actualURL.getHost());
        } catch (MalformedURLException e) {
            return true;
        }
    }

    private boolean shouldIgnoreHost(String hostToMatch) {
        if (StringUtils.isBlank(hostToMatch) || ignoredProxyHosts == null || ignoredProxyHosts.isEmpty()) {
            return false;
        }

        for (Pattern ignoredProxyHostPattern : ignoredProxyHosts) {
            Matcher m = ignoredProxyHostPattern.matcher(hostToMatch);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

}
