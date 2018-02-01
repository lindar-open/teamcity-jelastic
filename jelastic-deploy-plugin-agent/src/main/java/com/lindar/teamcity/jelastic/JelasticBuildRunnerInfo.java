package com.lindar.teamcity.jelastic;

import com.lindar.teamcity.AppCommon;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import org.jetbrains.annotations.NotNull;

public class JelasticBuildRunnerInfo implements AgentBuildRunnerInfo {
    @NotNull
    @Override
    public String getType() {
        return AppCommon.JELASTIC_RUN_TYPE;
    }

    @Override
    public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
        return true;
    }
}
