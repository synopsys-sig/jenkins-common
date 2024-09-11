/*
 * jenkins-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.service;

import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JenkinsRemotingService {
    private final Launcher launcher;
    private final FilePath workspace;
    private final TaskListener listener;

    public JenkinsRemotingService(Launcher launcher, FilePath workspace, TaskListener listener) {
        this.launcher = launcher;
        this.workspace = workspace;
        this.listener = listener;
    }

    public List<String> tokenizeArgumentString(String argumentString) {
        return Arrays.asList(Util.tokenize(argumentString));
    }

    public List<String> resolveEnvironmentVariables(IntEnvironmentVariables intEnvironmentVariables, List<String> argumentList) {
        return argumentList.stream()
                   .map(argument -> Util.replaceMacro(argument, intEnvironmentVariables.getVariables()))
                   .collect(Collectors.toList());
    }

    public int launch(IntEnvironmentVariables intEnvironmentVariables, List<String> commandLine) throws IOException, InterruptedException {
        return launcher.launch()
                   .cmds(commandLine)
                   .envs(intEnvironmentVariables.getVariables())
                   .pwd(workspace)
                   .stdout(listener)
                   .quiet(true)
                   .join();
    }

    public <T, E extends Throwable> T call(Callable<T, E> callable) throws E, IOException, InterruptedException {
        VirtualChannel virtualChannel = launcher.getChannel();
        if (virtualChannel == null) {
            // It's rare for the launcher's channel to be null, but if it is we can fall back to the workspace. We rely on the launcher first and foremost because that's how we can run on docker agents. --rotte JUL 2020
            virtualChannel = workspace.getChannel();
        }

        return virtualChannel.call(callable);
    }

    public boolean isRemoteUnix() {
        return launcher.isUnix();
    }

    public OperatingSystemType getRemoteOperatingSystemType() throws IOException, InterruptedException {
        return call(new OperatingSystemTypeCallable());
    }

    public String getRemoteWorkspacePath() {
        return workspace.getRemote();
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public FilePath getWorkspace() {
        return workspace;
    }

    public FilePath getRemoteFilePath(String filePath) {
        return new FilePath(getVirtualChannel(), filePath);
    }

    private VirtualChannel getVirtualChannel() {
        VirtualChannel virtualChannel = launcher.getChannel();
        if (virtualChannel == null) {
            // It's rare for the launcher's channel to be null, but if it is we can fall back to the workspace. We rely on the launcher first and foremost because that's how we can run on docker agents. --rotte JUL 2020
            virtualChannel = workspace.getChannel();
        }

        return virtualChannel;
    }

    public static class OperatingSystemTypeCallable extends MasterToSlaveCallable<OperatingSystemType, RuntimeException> {
        private static final long serialVersionUID = 1943720716430585353L;

        @Override
        public OperatingSystemType call() {
            return OperatingSystemType.determineFromSystem();
        }
    }

}
