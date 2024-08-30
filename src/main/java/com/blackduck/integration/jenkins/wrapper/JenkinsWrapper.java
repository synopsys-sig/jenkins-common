/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.wrapper;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import hudson.ProxyConfiguration;
import hudson.security.ACL;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

import java.util.Collections;
import java.util.Optional;

public class JenkinsWrapper {
    private final Jenkins jenkins;

    public JenkinsWrapper(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public static JenkinsWrapper initializeFromJenkinsJVM() {
        return new JenkinsWrapper(Jenkins.getInstanceOrNull());
    }

    public Optional<Jenkins> getJenkins() {
        return Optional.ofNullable(jenkins);
    }

    protected Optional<VersionNumber> getVersion() {
        return Optional.ofNullable(Jenkins.getVersion());
    }

    protected <T extends Credentials> Optional<T> getCredentialsById(IdMatcher idMatcher, Class<T> credentialsType) {
        return CredentialsProvider.lookupCredentials(credentialsType, jenkins, ACL.SYSTEM, Collections.emptyList()).stream()
                   .filter(idMatcher::matches)
                   .findAny();
    }

    protected Optional<ProxyConfiguration> getProxyConfiguration() {
        return Optional.ofNullable(jenkins)
                   .map(instance -> instance.proxy);
    }

    public JenkinsVersionHelper getVersionHelper() {
        return new JenkinsVersionHelper(this);
    }

    public JenkinsProxyHelper getProxyHelper() {
        return JenkinsProxyHelper.fromProxyConfiguration(getProxyConfiguration().orElse(null));
    }

    public SynopsysCredentialsHelper getCredentialsHelper() {
        return new SynopsysCredentialsHelper(this);
    }

}
