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

import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

public class JenkinsVersionHelper {
    public static final String UNKNOWN_VERSION = "UNKNOWN_VERSION";

    public static String getPluginVersion(final String pluginName) {
        String pluginVersion = UNKNOWN_VERSION;
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) {
            // Jenkins still active
            final Plugin p = jenkins.getPlugin(pluginName);
            if (p != null) {
                // plugin found
                final PluginWrapper pw = p.getWrapper();
                if (pw != null) {
                    pluginVersion = pw.getVersion();
                }
            }
        }
        return pluginVersion;
    }

    public static String getJenkinsVersion() {
        final VersionNumber versionNumber = Jenkins.getVersion();
        if (versionNumber == null) {
            return UNKNOWN_VERSION;
        } else {
            return versionNumber.toString();
        }
    }

}
