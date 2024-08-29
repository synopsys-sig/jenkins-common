/*
 * jenkins-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.sca.integration.jenkins.extensions;

import hudson.util.ListBoxModel;

import java.util.stream.Stream;

public interface JenkinsSelectBoxEnum {
    static ListBoxModel toListBoxModel(final JenkinsSelectBoxEnum[] selectBoxEnumValues) {
        return Stream.of(selectBoxEnumValues)
                   .collect(ListBoxModel::new, (model, value) -> model.add(value.getDisplayName(), value.name()), ListBoxModel::addAll);
    }

    String getDisplayName();

    String name();

}
