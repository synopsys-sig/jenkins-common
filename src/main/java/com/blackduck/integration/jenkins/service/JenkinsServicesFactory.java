/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.service;

import com.blackduck.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.function.ThrowingSupplier;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.tasks.SimpleBuildWrapper;

public class JenkinsServicesFactory {
    protected final JenkinsIntLogger logger;
    protected final EnvVars envVars;
    protected final Launcher launcher;
    protected final Node node;
    protected final Run<?, ?> run;
    protected final ThrowingSupplier<FilePath, AbortException> validatedWorkspace;
    protected final TaskListener listener;

    public JenkinsServicesFactory(JenkinsIntLogger logger, EnvVars envVars, Launcher launcher, TaskListener listener, Node node, Run<?, ?> run, FilePath workspace) {
        this.logger = logger;
        this.envVars = envVars;
        this.launcher = launcher;
        this.node = node;
        this.run = run;
        this.validatedWorkspace = () -> validateWorkspace(workspace);
        this.listener = listener;
    }

    public JenkinsRemotingService createJenkinsRemotingService() throws AbortException {
        return new JenkinsRemotingService(launcher, validatedWorkspace.get(), listener);
    }

    public JenkinsConfigService createJenkinsConfigService() {
        return new JenkinsConfigService(envVars, node, listener);
    }

    public JenkinsRunService createJenkinsRunService() {
        return new JenkinsRunService(logger, run);
    }

    // This method only currently exists for consistency, but as additional capabilities are added to the WrapperContextService it could make use of the data the JenkinsServicesFactory has available to it. --rotte JUN 2021
    public JenkinsWrapperContextService createJenkinsWrapperContextService(SimpleBuildWrapper.Context context) {
        return new JenkinsWrapperContextService(context);
    }

    private FilePath validateWorkspace(FilePath workspace) throws AbortException {
        if (workspace == null) {
            throw new AbortException("Cannot execute this Synopsys integration: The workspace could not be determined.");
        }

        return workspace;
    }

}
