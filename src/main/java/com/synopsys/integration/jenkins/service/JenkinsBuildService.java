/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.service;

import java.io.IOException;
import java.util.Optional;

import com.synopsys.integration.jenkins.extensions.ChangeBuildStatusTo;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Executor;
import hudson.model.JDK;
import hudson.model.Result;

public class JenkinsBuildService {
    private final JenkinsIntLogger logger;
    private final AbstractBuild<?, ?> build;

    public JenkinsBuildService(JenkinsIntLogger logger, AbstractBuild<?, ?> build) {
        this.logger = logger;
        this.build = build;
    }

    public void markBuildFailed(String message) {
        logger.error(message);
        build.setResult(Result.FAILURE);
    }

    public void markBuildFailed(Exception e) {
        logger.error(e);
        build.setResult(Result.FAILURE);
    }

    public void markBuildUnstable(Exception e) {
        logger.error(e);
        build.setResult(Result.UNSTABLE);
    }

    public void markBuildAs(ChangeBuildStatusTo changeBuildStatusTo) {
        Result result = changeBuildStatusTo.getResult();
        logger.alwaysLog("Setting build status to " + result.toString());
        build.setResult(result);
    }

    public Optional<String> getJDKRemoteHomeOrEmpty() throws InterruptedException {
        Optional<JDK> possibleJdk = Optional.ofNullable(build)
                                        .map(AbstractBuild::getProject)
                                        .map(AbstractProject::getJDK);

        if (possibleJdk.isPresent()) {
            try {
                JDK jdk = possibleJdk.get();
                JDK nodeJdk = jdk.forNode(build.getBuiltOn(), logger.getTaskListener());
                return Optional.ofNullable(nodeJdk.getHome());
            } catch (IOException ignored) {
                // If we can't get it, just return empty
            }
        }

        return Optional.empty();
    }

    public void markBuildAborted() {
        build.setResult(Result.ABORTED);
    }

    public void markBuildInterrupted() {
        Executor executor = build.getExecutor();
        if (executor == null) {
            markBuildAborted();
        } else {
            build.setResult(executor.abortResult());
        }
    }

    public AbstractBuild getBuild() {
        return build;
    }

    public void addAction(Action a) {
        build.addAction(a);
    }

    public String getWorkspaceOrProjectWorkspace() {
        FilePath buildWorkspace = build.getWorkspace();

        if (buildWorkspace != null) {
            return buildWorkspace.getRemote();
        } else {
            return build.getProject().getCustomWorkspace();
        }
    }

}
