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

import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

public class JenkinsServicesFactory {
    private final JenkinsIntLogger logger;
    protected final Launcher launcher;
    protected final ThrowingSupplier<FilePath, AbortException> validatedWorkspace;
    protected final TaskListener listener;
    private final AbstractBuild<?, ?> build;

    public JenkinsServicesFactory(JenkinsIntLogger logger, AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, TaskListener listener) {
        this.logger = logger;
        this.launcher = launcher;
        this.validatedWorkspace = () -> validateWorkspace(workspace);
        this.listener = listener;
        this.build = build;
    }

    public JenkinsRemotingService createJenkinsRemotingService() throws AbortException {
        return new JenkinsRemotingService(launcher, validatedWorkspace.get(), listener);
    }

    public JenkinsBuildService createJenkinsBuildService() {
        return new JenkinsBuildService(logger, build);
    }

    public JenkinsConfigService createJenkinsConfigService() {
        return new JenkinsConfigService();
    }

    private FilePath validateWorkspace(FilePath workspace) throws AbortException {
        if (workspace == null) {
            throw new AbortException("Polaris cannot be executed: The workspace could not be determined.");
        }

        return workspace;
    }

}
