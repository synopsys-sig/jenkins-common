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
package com.synopsys.integration.jenkins.wrapper;

import static hudson.model.Items.XSTREAM;

import java.util.Collections;
import java.util.Optional;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;

import hudson.ProxyConfiguration;
import hudson.security.ACL;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

public class JenkinsWrapper {

    private Jenkins jenkins;

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

    public void migrateFieldFrom(String oldName, Class clazz, String newName) {
        XSTREAM.aliasField(oldName, clazz, newName);
        XSTREAM.aliasField(newName, clazz, newName);
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
