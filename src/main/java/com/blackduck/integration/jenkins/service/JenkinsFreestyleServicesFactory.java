/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.service;

import com.blackduck.integration.jenkins.extensions.JenkinsIntLogger;
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
