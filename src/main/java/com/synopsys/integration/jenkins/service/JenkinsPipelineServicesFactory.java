/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.service;

import org.jenkinsci.plugins.workflow.graph.FlowNode;

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;

public class JenkinsPipelineServicesFactory extends JenkinsServicesFactory {
    protected final FlowNode flowNode;

    public JenkinsPipelineServicesFactory(JenkinsIntLogger logger, EnvVars envVars, FlowNode flowNode, Launcher launcher, TaskListener listener, Node node, Run<?, ?> run, FilePath workspace) {
        super(logger, envVars, launcher, listener, node, run, workspace);
        this.flowNode = flowNode;
    }

    public JenkinsPipelineFlowService createJenkinsPipelineFlowService() {
        return new JenkinsPipelineFlowService(logger, run, flowNode);
    }

}
