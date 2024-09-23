package com.blackduck.integration.jenkins;

import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangeSetFilterTest {
    private final String validPath = "valid_path";
    private final String validPathWithSlash = "with_slash/valid_path";
    private final String invalidPath = "invalid_path";

    private final EditType editType = new EditType("EditType-Name", "EditType-Description");
    private final IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    private final ChangeLogSet.AffectedFile mockAffectedFile = createAffectedFile(validPath, editType);
    private final ChangeLogSet.AffectedFile mockAffectedFileWithSlash = createAffectedFile(validPathWithSlash, editType);

    @Test
    public void testShouldInclude() {
        assertTrue(new ChangeSetFilter(intLogger).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).includeMatching("").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).includeMatching(null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).includeMatching(validPath).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching("").includeMatching("").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching("").includeMatching(null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching("").includeMatching(validPath).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching(invalidPath).includeMatching("").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching(invalidPath).includeMatching(null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching(invalidPath).includeMatching(validPath).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching(null).includeMatching("").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching(null).includeMatching(null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger).excludeMatching(null).includeMatching(validPath).shouldInclude(mockAffectedFile));

        assertTrue(new ChangeSetFilter(intLogger).excludeMatching(null).includeMatching(validPath).shouldInclude(mockAffectedFileWithSlash));
    }

    @Test
    public void testShouldNotInclude() {
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching("").includeMatching(invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(invalidPath).includeMatching(invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(null).includeMatching(invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(validPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(validPath).includeMatching("").shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(validPath).includeMatching(invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(validPath).includeMatching(null).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(validPath).includeMatching(validPath).shouldInclude(mockAffectedFile));

        assertFalse(new ChangeSetFilter(intLogger).excludeMatching(validPath).includeMatching(validPath).shouldInclude(mockAffectedFileWithSlash));
    }

    private static ChangeLogSet.AffectedFile createAffectedFile(String path, EditType editType) {
        ChangeLogSet.AffectedFile mockAffectedFile = Mockito.mock(ChangeLogSet.AffectedFile.class);
        Mockito.when(mockAffectedFile.getPath()).thenReturn(path);
        Mockito.when(mockAffectedFile.getEditType()).thenReturn(editType);
        return mockAffectedFile;
    }
}
