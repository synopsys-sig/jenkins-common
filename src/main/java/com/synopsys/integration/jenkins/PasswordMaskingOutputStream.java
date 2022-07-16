/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import hudson.console.LineTransformationOutputStream;

public class PasswordMaskingOutputStream extends LineTransformationOutputStream {
    public static final String MASKED_PASSWORD = "********";

    private final OutputStream wrappedOutputStream;
    private final String passwordToMask;

    public PasswordMaskingOutputStream(OutputStream wrappedOutputStream, String passwordToMask) {
        this.wrappedOutputStream = wrappedOutputStream;
        this.passwordToMask = passwordToMask;
    }

    @Override
    protected void eol(byte[] bytes, int len) throws IOException {
        String line = new String(bytes, 0, len, StandardCharsets.UTF_8);

        if (StringUtils.isNotBlank(passwordToMask)) {
            line = line.replaceAll(passwordToMask, MASKED_PASSWORD);
        }

        wrappedOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
    }

}