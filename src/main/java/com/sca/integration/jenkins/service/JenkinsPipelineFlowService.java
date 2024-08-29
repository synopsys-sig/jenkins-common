/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.sca.integration.jenkins.service;

import com.sca.integration.jenkins.extensions.ChangeBuildStatusTo;
import com.sca.integration.jenkins.extensions.JenkinsIntLogger;
import hudson.model.Result;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

public class JenkinsPipelineFlowService {
    private final JenkinsIntLogger logger;
    private final FlowNode flowNode;
    private final Run<?, ?> run;

    public JenkinsPipelineFlowService(JenkinsIntLogger logger, Run<?, ?> run, FlowNode flowNode) {
        this.logger = logger;
        this.flowNode = flowNode;
        this.run = run;
    }

    public FlowNode getFlowNode() {
        return flowNode;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public void markStageUnstable(String message) {
        logger.warn(message);
        flowNode.addOrReplaceAction(new WarningAction(Result.UNSTABLE).withMessage(message));
        run.setResult(Result.UNSTABLE);
    }

    public void markStageAs(ChangeBuildStatusTo changeBuildStatusTo, String message) {
        Result result = changeBuildStatusTo.getResult();
        logger.alwaysLog("Setting stage status to " + result.toString());
        flowNode.addOrReplaceAction(new WarningAction(result).withMessage(message));
        run.setResult(result);
    }

}
