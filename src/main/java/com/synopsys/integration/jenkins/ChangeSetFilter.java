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
package com.synopsys.integration.jenkins;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Predicate;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.log.IntLogger;

import hudson.scm.ChangeLogSet;

public class ChangeSetFilter {
    private final IntLogger logger;
    private final Set<String> excludedSet;
    private final Set<String> includedSet;

    /**
     * Provide a comma-separated list of names to exclude and/or a comma-separated list of names to include. Exclusion rules always win.
     */
    public ChangeSetFilter(final IntLogger logger, final String toExclude, final String toInclude) {
        this(logger, createSetFromString(toExclude), createSetFromString(toInclude));
    }

    private ChangeSetFilter(final IntLogger logger, final Set<String> excludedSet, final Set<String> includedSet) {
        this.logger = logger;
        this.excludedSet = excludedSet;
        this.includedSet = includedSet;
    }

    public static ChangeSetFilter createAcceptAllFilter(final IntLogger logger) {
        return new ChangeSetFilter(logger, Collections.emptySet(), Collections.emptySet());
    }

    private static Set<String> createSetFromString(final String s) {
        final Set<String> set = new HashSet<>();
        final StringTokenizer stringTokenizer = new StringTokenizer(StringUtils.trimToEmpty(s), ",");
        while (stringTokenizer.hasMoreTokens()) {
            set.add(StringUtils.trimToEmpty(stringTokenizer.nextToken()));
        }
        return set;
    }

    public boolean shouldInclude(final ChangeLogSet.AffectedFile affectedFile) {
        final String affectedFilePath = affectedFile.getPath();
        final String affectedEditType = affectedFile.getEditType().getName();

        final boolean shouldInclude = shouldInclude(affectedFilePath);
        if (shouldInclude) {
            logger.debug(String.format("Type: %s File Path: %s Included in change set", affectedEditType, affectedFilePath));
        } else {
            logger.debug(String.format("Type: %s File Path: %s Excluded from change set", affectedEditType, affectedFilePath));
        }

        return shouldInclude;
    }

    private boolean shouldInclude(final String filePath) {
        // ChangeLogSet.AffectedFile getPath is normalized to use the / separator
        final String fileName;
        if (filePath.contains("/")) {
            fileName = StringUtils.substringAfter(filePath, "/");
        } else {
            fileName = filePath;
        }

        final Predicate<String> caseInsensitiveWildcardMatch = pattern -> FilenameUtils.wildcardMatch(fileName, pattern, IOCase.INSENSITIVE);

        final boolean excluded = excludedSet.stream().anyMatch(caseInsensitiveWildcardMatch);
        final boolean included = includedSet.isEmpty() || includedSet.stream().anyMatch(caseInsensitiveWildcardMatch);

        return included && !excluded;
    }

}
