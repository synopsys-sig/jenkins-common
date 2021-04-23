/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
