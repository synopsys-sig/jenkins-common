/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import hudson.EnvVars;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import jenkins.model.GlobalConfiguration;

public class JenkinsConfigService {
    private final EnvVars envVars;
    private final Node node;
    private final TaskListener listener;

    public JenkinsConfigService(EnvVars envVars, Node node, TaskListener listener) {
        this.envVars = envVars;
        this.node = node;
        this.listener = listener;
    }

    public <T extends GlobalConfiguration> Optional<T> getGlobalConfiguration(Class<T> configurationClass) {
        T globalConfig = GlobalConfiguration.all().get(configurationClass);
        return Optional.ofNullable(globalConfig);
    }

    public <T extends ToolInstallation, D extends ToolDescriptor<T>> List<T> getToolInstallations(Class<D> descriptorClass) {
        D toolDescriptor = ToolInstallation.all().get(descriptorClass);

        if (toolDescriptor == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(toolDescriptor.getInstallations());
        }
    }

    public <T extends ToolInstallation & NodeSpecific<T> & EnvironmentSpecific<T>, D extends ToolDescriptor<T>> Optional<T> getInstallationForNodeAndEnvironment(Class<D> descriptorClass, String toolInstallationName)
        throws IOException, InterruptedException {
        Optional<T> environmentReadyToolInstallation = getToolInstallations(descriptorClass).stream()
                                                           .filter(installation -> installation.getName().equals(toolInstallationName))
                                                           .findFirst()
                                                           .map(installation -> installation.forEnvironment(envVars));

        if (environmentReadyToolInstallation.isPresent()) {
            T toolInstallation = environmentReadyToolInstallation.get();
            return Optional.ofNullable(toolInstallation.forNode(node, listener));
        }
        return Optional.empty();
    }

}
