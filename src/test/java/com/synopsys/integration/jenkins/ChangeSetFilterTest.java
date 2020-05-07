package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;

public class ChangeSetFilterTest {

    String validPath = "valid_path";
    String validPathWithSlash = "with_slash/valid_path";
    String invalidPath = "invalid_path";
    String dummyPath = "dummy_path";

    EditType editType = new EditType("EditType-Name", "EditType-Description");
    IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    ChangeLogSet.AffectedFile mockAffectedFile = createAffectedFile(validPath, editType);
    ChangeLogSet.AffectedFile mockAffectedFileWithSlash = createAffectedFile(validPathWithSlash, editType);

    @Test
    public void testShouldIncludeTrue() {
        assertTrue(new ChangeSetFilter(intLogger, "", "").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, "", null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, "", validPath).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, invalidPath, "").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, invalidPath, null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, invalidPath, validPath).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, null, "").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, null, null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(intLogger, null, validPath).shouldInclude(mockAffectedFile));

        assertTrue(new ChangeSetFilter(intLogger, null, validPath).shouldInclude(mockAffectedFileWithSlash));
    }

    @Test
    public void testShouldIncludeFalse() {
        assertFalse(new ChangeSetFilter(intLogger, "", invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger, invalidPath, invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger, null, invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger, validPath, "").shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger, validPath, invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger, validPath, null).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(intLogger, validPath, validPath).shouldInclude(mockAffectedFile));

        assertFalse(new ChangeSetFilter(intLogger, validPath, validPath).shouldInclude(mockAffectedFileWithSlash));
    }

    @Test
    public void testSuccessfulCreateWithPopulatedFilters() {
        new ChangeSetFilter(intLogger, dummyPath, dummyPath);
    }

    @Test
    public void testSuccessfulCreateWithEmptyFilters() {
        new ChangeSetFilter(intLogger, "", "");
    }

    @Test
    public void testSuccessfulCreateWithNullFilters() {
        new ChangeSetFilter(intLogger, null, dummyPath);
        new ChangeSetFilter(intLogger, dummyPath, null);
        new ChangeSetFilter(intLogger, null, null);
    }

    @Test
    public void testSuccessfulCreateWithAcceptAllFilter() {
        ChangeSetFilter.createAcceptAllFilter(null);
    }

    private static ChangeLogSet.AffectedFile createAffectedFile(String path, EditType editType) {
        ChangeLogSet.AffectedFile mockAffectedFile = Mockito.mock(ChangeLogSet.AffectedFile.class);
        Mockito.when(mockAffectedFile.getPath()).thenReturn(path);
        Mockito.when(mockAffectedFile.getEditType()).thenReturn(editType);
        return mockAffectedFile;
    }
}
