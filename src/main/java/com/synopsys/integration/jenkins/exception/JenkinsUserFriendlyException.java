/**
 * synopsys-polaris
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
package com.synopsys.integration.jenkins.exception;

import hudson.AbortException;

/**
 * An integrations-specific alternative to AbortExceptions, so we know we're only handling AbortExceptions thrown by our own code.
 * Extending AbortException buys us the special functionality that Jenkins has when handling uncaught AbortExceptions in our pipeline steps.
 * Ideally, this class would also extend IntegrationException, but sadly we cannot do that.
 * -- rotte OCT 2020
 */
public class JenkinsUserFriendlyException extends AbortException {
    private static final long serialVersionUID = 4947006408980692375L;

    public JenkinsUserFriendlyException() {
        super();
    }

    public JenkinsUserFriendlyException(String message) {
        super(message);
    }
}
