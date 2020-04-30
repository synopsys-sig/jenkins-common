package com.synopsys.integration.jenkins;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;

import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;

public class ChangeSetFilterTest {

    String validPath = "valid_path";
    String invalidPath = "invalid_path";
    String dummyPath = "dummy_path";

    EditType editType = new EditType("MockEditTypeName", "MockEditTypeDescription");
    JenkinsIntLogger MockJenkinsIntLogger = Mockito.mock(JenkinsIntLogger.class);
    ChangeLogSet.AffectedFile mockAffectedFile = new MockAffectedFile(validPath, editType);

    @Test
    public void testShouldIncludeTrue() {
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, "", "").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, "", null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, "", validPath).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, invalidPath, "").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, invalidPath, null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, invalidPath, validPath).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, null, "").shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, null, null).shouldInclude(mockAffectedFile));
        assertTrue(new ChangeSetFilter(MockJenkinsIntLogger, null, validPath).shouldInclude(mockAffectedFile));
    }

    @Test
    public void testShouldIncludeFalse() {
        assertFalse(new ChangeSetFilter(MockJenkinsIntLogger, "", invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(MockJenkinsIntLogger, invalidPath, invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(MockJenkinsIntLogger, null, invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(MockJenkinsIntLogger, validPath, "").shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(MockJenkinsIntLogger, validPath, invalidPath).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(MockJenkinsIntLogger, validPath, null).shouldInclude(mockAffectedFile));
        assertFalse(new ChangeSetFilter(MockJenkinsIntLogger, validPath, validPath).shouldInclude(mockAffectedFile));
    }

    @Test
    public void testSuccessfulCreateWithPopulatedFilters() {
        new ChangeSetFilter(MockJenkinsIntLogger, dummyPath, dummyPath);
    }

    @Test
    public void testSuccessfulCreateWithEmptyFilters() {
        new ChangeSetFilter(MockJenkinsIntLogger, "", "");
    }

    @Test
    public void testSuccessfulCreateWithNullFilters() {
        new ChangeSetFilter(MockJenkinsIntLogger, null, dummyPath);
        new ChangeSetFilter(MockJenkinsIntLogger, dummyPath, null);
        new ChangeSetFilter(MockJenkinsIntLogger, null, null);
    }

    @Test
    public void testSuccessfulCreateWithAcceptAllFilter() {
        ChangeSetFilter.createAcceptAllFilter(null);
    }

    private class MockAffectedFile implements ChangeLogSet.AffectedFile {

        private String path;
        private EditType editType;

        public MockAffectedFile(String path, EditType editType) {
            this.path = path;
            this.editType = editType;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public EditType getEditType() {
            return editType;
        }
    }
}
