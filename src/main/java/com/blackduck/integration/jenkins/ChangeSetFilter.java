/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins;

import com.synopsys.integration.log.IntLogger;
import hudson.scm.ChangeLogSet;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Predicate;

public class ChangeSetFilter {
    private final IntLogger logger;
    private final Set<String> excludedSet;
    private final Set<String> includedSet;

    /**
     * Provide a comma-separated list of names to exclude and/or a comma-separated list of names to include. Exclusion rules always win.
     */
    public ChangeSetFilter(IntLogger logger) {
        this(logger, new HashSet<>(), new HashSet<>());
    }

    private ChangeSetFilter(IntLogger logger, Set<String> excludedSet, Set<String> includedSet) {
        this.logger = logger;
        this.excludedSet = excludedSet;
        this.includedSet = includedSet;
    }

    public ChangeSetFilter includeMatching(String toInclude) {
        includedSet.addAll(createSetFromString(toInclude));

        return this;
    }

    public ChangeSetFilter excludeMatching(String toExclude) {
        excludedSet.addAll(createSetFromString(toExclude));

        return this;
    }

    private Set<String> createSetFromString(String s) {
        Set<String> set = new HashSet<>();
        StringTokenizer stringTokenizer = new StringTokenizer(StringUtils.trimToEmpty(s), ",");
        while (stringTokenizer.hasMoreTokens()) {
            set.add(StringUtils.trimToEmpty(stringTokenizer.nextToken()));
        }
        return set;
    }

    public boolean shouldInclude(ChangeLogSet.AffectedFile affectedFile) {
        String affectedFilePath = affectedFile.getPath();
        String affectedEditType = affectedFile.getEditType().getName();

        boolean shouldInclude = shouldInclude(affectedFilePath);
        if (shouldInclude) {
            logger.debug(String.format("Type: %s File Path: %s Included in change set", affectedEditType, affectedFilePath));
        } else {
            logger.debug(String.format("Type: %s File Path: %s Excluded from change set", affectedEditType, affectedFilePath));
        }

        return shouldInclude;
    }

    private boolean shouldInclude(String filePath) {
        // ChangeLogSet.AffectedFile getPath is normalized to use the / separator
        String fileName;
        if (filePath.contains("/")) {
            fileName = StringUtils.substringAfter(filePath, "/");
        } else {
            fileName = filePath;
        }

        Predicate<String> caseInsensitiveWildcardMatch = pattern -> FilenameUtils.wildcardMatch(fileName, pattern, IOCase.INSENSITIVE);

        boolean excluded = excludedSet.stream().anyMatch(caseInsensitiveWildcardMatch);
        boolean included = includedSet.isEmpty() || includedSet.stream().anyMatch(caseInsensitiveWildcardMatch);

        return included && !excluded;
    }

}
