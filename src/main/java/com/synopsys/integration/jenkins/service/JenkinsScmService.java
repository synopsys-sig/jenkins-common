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
