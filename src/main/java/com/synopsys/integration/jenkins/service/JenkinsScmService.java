package com.synopsys.integration.jenkins.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;

import com.synopsys.integration.jenkins.ChangeSetFilter;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.rest.RestConstants;

import hudson.scm.ChangeLogSet;
import jenkins.scm.RunWithSCM;

public class JenkinsScmService {
    private final JenkinsIntLogger logger;
    private final RunWithSCM<?, ?> build;

    public JenkinsScmService(JenkinsIntLogger logger, RunWithSCM<?, ?> build) {
        this.logger = logger;
        this.build = build;
    }

    public List<String> getFilePathsFromChangeSet(ChangeSetFilter changeSetFilter) {
        return build.getChangeSets().stream()
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
