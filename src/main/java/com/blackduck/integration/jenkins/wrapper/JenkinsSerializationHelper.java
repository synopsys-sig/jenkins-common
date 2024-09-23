/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.wrapper;

import static hudson.model.Items.XSTREAM;

public class JenkinsSerializationHelper {
    private JenkinsSerializationHelper() {
    }
    
    public static void migrateFieldFrom(String oldName, Class clazz, String newName) {
        XSTREAM.aliasField(oldName, clazz, newName);
        XSTREAM.aliasField(newName, clazz, newName);
    }
}
