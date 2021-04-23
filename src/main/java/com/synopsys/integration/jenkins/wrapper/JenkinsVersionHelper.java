/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.wrapper;

import java.util.Optional;

import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.util.VersionNumber;

public class JenkinsVersionHelper {
    private final JenkinsWrapper jenkinsWrapper;

    public JenkinsVersionHelper(JenkinsWrapper jenkinsWrapper) {
        this.jenkinsWrapper = jenkinsWrapper;
    }

    public Optional<String> getPluginVersion(String pluginName) {
        return jenkinsWrapper.getJenkins()
                   .map(instance -> instance.getPlugin(pluginName))
                   .map(Plugin::getWrapper)
                   .map(PluginWrapper::getVersion);
    }

    public Optional<String> getJenkinsVersion() {
        return jenkinsWrapper.getVersion()
                   .map(VersionNumber::toString);
    }

}
