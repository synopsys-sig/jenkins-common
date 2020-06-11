package com.synopsys.integration.jenkins.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import hudson.util.ListBoxModel;

public class JenkinsSelectBoxEnumTest {

    @Test
    public void testJenkinsSelectBoxEnum() {
        JenkinsSelectBoxEnum[] jenkinsSelectBoxEnums = { ChangeBuildStatusTo.SUCCESS, ChangeBuildStatusTo.FAILURE, ChangeBuildStatusTo.UNSTABLE };
        ListBoxModel listBoxModel = JenkinsSelectBoxEnum.toListBoxModel(jenkinsSelectBoxEnums);

        assertEquals(jenkinsSelectBoxEnums.length, listBoxModel.size());

        for (int i = 0; i < listBoxModel.size(); i++) {
            assertEquals(jenkinsSelectBoxEnums[i].getDisplayName(), listBoxModel.get(i).name, "Name did not match at index:" + i);
            assertEquals(jenkinsSelectBoxEnums[i].name(), listBoxModel.get(i).value, "Value did not match at index:" + i);
        }
    }
}
