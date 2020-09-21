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

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.model.TaskListener;

public class JenkinsFreestyleServicesFactory extends JenkinsServicesFactory {
    public final AbstractBuild<?, ?> build;

    public JenkinsFreestyleServicesFactory(JenkinsIntLogger logger, AbstractBuild<?, ?> build, EnvVars envVars, Launcher launcher, TaskListener listener, Node node, FilePath workspace) {
        super(logger, envVars, launcher, listener, node, build, workspace);
        this.build = build;
    }

    public JenkinsBuildService createJenkinsBuildService() {
        return new JenkinsBuildService(logger, build);
    }

}
