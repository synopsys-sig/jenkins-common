/*
 * jenkins-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.extensions;

import java.util.stream.Stream;

import hudson.util.ListBoxModel;

public interface JenkinsSelectBoxEnum {
    static ListBoxModel toListBoxModel(final JenkinsSelectBoxEnum[] selectBoxEnumValues) {
        return Stream.of(selectBoxEnumValues)
                   .collect(ListBoxModel::new, (model, value) -> model.add(value.getDisplayName(), value.name()), ListBoxModel::addAll);
    }

    String getDisplayName();

    String name();

}
