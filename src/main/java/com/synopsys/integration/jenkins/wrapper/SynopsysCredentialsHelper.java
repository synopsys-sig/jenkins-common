/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.wrapper;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;

import hudson.util.Secret;

public class SynopsysCredentialsHelper {
    public static final Class<StringCredentialsImpl> API_TOKEN_CREDENTIALS_CLASS = StringCredentialsImpl.class;
    public static final CredentialsMatcher API_TOKEN_CREDENTIALS = CredentialsMatchers.instanceOf(API_TOKEN_CREDENTIALS_CLASS);
    public static final Class<UsernamePasswordCredentialsImpl> USERNAME_PASSWORD_CREDENTIALS_CLASS = UsernamePasswordCredentialsImpl.class;
    public static final CredentialsMatcher API_TOKEN_OR_USERNAME_PASSWORD_CREDENTIALS = CredentialsMatchers
                                                                                            .either(CredentialsMatchers.instanceOf(API_TOKEN_CREDENTIALS_CLASS), CredentialsMatchers.instanceOf(USERNAME_PASSWORD_CREDENTIALS_CLASS));
    private final JenkinsWrapper jenkinsWrapper;

    public SynopsysCredentialsHelper(JenkinsWrapper jenkinsWrapper) {
        this.jenkinsWrapper = jenkinsWrapper;
    }

    public com.synopsys.integration.rest.credentials.Credentials getIntegrationCredentialsById(String credentialsId) {
        Optional<UsernamePasswordCredentialsImpl> credentials = getUsernamePasswordCredentialsById(credentialsId);

        CredentialsBuilder credentialsBuilder = com.synopsys.integration.rest.credentials.Credentials.newBuilder();

        credentials.map(UsernamePasswordCredentialsImpl::getUsername)
            .ifPresent(credentialsBuilder::setUsername);

        credentials.map(UsernamePasswordCredentialsImpl::getPassword)
            .map(Secret::getPlainText)
            .ifPresent(credentialsBuilder::setPassword);

        return credentialsBuilder.build();
    }

    public Optional<String> getApiTokenByCredentialsId(String credentialsId) {
        return getApiTokenCredentialsById(credentialsId)
                   .map(StringCredentialsImpl::getSecret)
                   .map(Secret::getPlainText);
    }

    public Optional<UsernamePasswordCredentialsImpl> getUsernamePasswordCredentialsById(String credentialsId) {
        return getCredentialsById(USERNAME_PASSWORD_CREDENTIALS_CLASS, credentialsId);
    }

    public Optional<StringCredentialsImpl> getApiTokenCredentialsById(String credentialsId) {
        return getCredentialsById(API_TOKEN_CREDENTIALS_CLASS, credentialsId);
    }

    public <T extends Credentials> Optional<T> getCredentialsById(Class<T> credentialsType, String credentialsId) {
        if (StringUtils.isBlank(credentialsId)) {
            return Optional.empty();
        }

        IdMatcher idMatcher = new IdMatcher(credentialsId);

        return jenkinsWrapper.getCredentialsById(idMatcher, credentialsType);
    }
}
