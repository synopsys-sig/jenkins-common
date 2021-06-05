package com.synopsys.integration.jenkins.wrapper;

import static hudson.model.Items.XSTREAM;

public class JenkinsSerializationHelper {
    private JenkinsSerializationHelper() {
    }
    
    public static void migrateFieldFrom(String oldName, Class clazz, String newName) {
        XSTREAM.aliasField(oldName, clazz, newName);
        XSTREAM.aliasField(newName, clazz, newName);
    }
}
