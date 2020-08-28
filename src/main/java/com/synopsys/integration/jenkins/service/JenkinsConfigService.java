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
