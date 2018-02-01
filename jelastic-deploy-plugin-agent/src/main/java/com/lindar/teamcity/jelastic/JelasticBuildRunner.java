package com.lindar.teamcity.jelastic;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import org.jetbrains.annotations.NotNull;

public class JelasticBuildRunner implements AgentBuildRunner {
    @NotNull
    @Override
    public BuildProcess createBuildProcess(@NotNull AgentRunningBuild agentRunningBuild, @NotNull BuildRunnerContext buildRunnerContext) throws RunBuildException {
        return new JelasticBuildProcess(agentRunningBuild, buildRunnerContext);
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getRunnerInfo() {
        return new JelasticBuildRunnerInfo();
    }
}
