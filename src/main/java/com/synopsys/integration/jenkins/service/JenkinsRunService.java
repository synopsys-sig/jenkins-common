/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;

import com.synopsys.integration.jenkins.ChangeSetFilter;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.rest.RestConstants;

import hudson.model.Action;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;

public class JenkinsRunService {
    private final JenkinsIntLogger logger;
    private final Run<?, ?> run;
    private final RunWrapper runWrapper;

    public JenkinsRunService(JenkinsIntLogger logger, Run<?, ?> run) {
        this.logger = logger;
        this.run = run;
        this.runWrapper = new RunWrapper(run, true);

    }

    public Run getRun() {
        return run;
    }

    public void addAction(Action a) {
        run.addAction(a);
    }

    public ChangeSetFilter newChangeSetFilter() {
        return new ChangeSetFilter(logger);
    }

    public List<String> getFilePathsFromChangeSet(ChangeSetFilter changeSetFilter) throws Exception {
        return runWrapper.getChangeSets().stream()
                   .filter(changeLogSet -> !changeLogSet.isEmptySet())
                   .flatMap(this::toEntries)
                   .peek(this::logEntry)
                   .flatMap(this::toAffectedFiles)
                   .filter(changeSetFilter::shouldInclude)
                   .map(ChangeLogSet.AffectedFile::getPath)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toList());
    }

    private Stream<? extends ChangeLogSet.Entry> toEntries(ChangeLogSet<? extends ChangeLogSet.Entry> changeLogSet) {
        return StreamSupport.stream(changeLogSet.spliterator(), false);
    }

    private Stream<? extends ChangeLogSet.AffectedFile> toAffectedFiles(ChangeLogSet.Entry entry) {
        return entry.getAffectedFiles().stream();
    }

    private void logEntry(ChangeLogSet.Entry entry) {
        if (logger.getLogLevel().isLoggable(LogLevel.DEBUG)) {
            Date date = new Date(entry.getTimestamp());
            logger.debug(String.format("Commit %s by %s on %s: %s", entry.getCommitId(), entry.getAuthor(), RestConstants.formatDate(date), entry.getMsg()));
        }

    }

}
