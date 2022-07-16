/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
