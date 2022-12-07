package com.lindar.teamcity.jelastic;

import com.lindar.teamcity.AppCommon;
import com.lindar.teamcity.jelastic.api.JelasticService;
import com.lindar.teamcity.jelastic.api.model.AuthenticationResponse;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class JelasticBuildProcess extends SyncBuildProcessAdapter {

    private final AgentRunningBuild agentRunningBuild;
    private final BuildRunnerContext buildRunnerContext;

    public JelasticBuildProcess(@NotNull AgentRunningBuild agentRunningBuild, @NotNull BuildRunnerContext buildRunnerContext) {
        super(buildRunnerContext.getBuild().getBuildLogger());
        this.agentRunningBuild = agentRunningBuild;
        this.buildRunnerContext = buildRunnerContext;
    }

    @Override
    protected BuildFinishedStatus runProcess() {

        Map<String, String> runnerParameters = buildRunnerContext.getRunnerParameters();

        String hostUrl = runnerParameters.get(AppCommon.PARAM_API);
        String username = runnerParameters.get(AppCommon.PARAM_USERNAME);
        String password = runnerParameters.get(AppCommon.PARAM_PASSWORD);
        String filepath = runnerParameters.get(AppCommon.PARAM_FILEPATH);
        String environment = runnerParameters.get(AppCommon.PARAM_ENVIRONMENT);
        String nodeGroup = runnerParameters.get(AppCommon.PARAM_NODE_GROUP);

        JelasticService jelasticService = new JelasticService();
        jelasticService.setScheme("https");
        jelasticService.setApiHoster(hostUrl);
        jelasticService.setEnvironment(environment);
        jelasticService.setFileBaseDir(agentRunningBuild.getCheckoutDirectory().getAbsolutePath());
        jelasticService.setFilePath(filepath);
        jelasticService.setNodeGroup(nodeGroup);

        AuthenticationResponse authenticationResponse = jelasticService.authentication(username, password);
        if (authenticationResponse.getResult() == 0) {
            myLogger.message("------------------------------------------------------------------------");
            myLogger.message("   Authentication : SUCCESS");
            //myLogger.message("          Session : " + authenticationResponse.getSession());
            myLogger.message("              Uid : " + authenticationResponse.getUid());
            myLogger.message("------------------------------------------------------------------------");
            val uploadResponse = jelasticService.upload(authenticationResponse);
            if (uploadResponse.getResult() == 0) {
                myLogger.message("      File UpLoad : SUCCESS");
                myLogger.message("         File URL : " + uploadResponse.getFile());
                myLogger.message("        File size : " + uploadResponse.getSize());
                myLogger.message("------------------------------------------------------------------------");
                val createObject = jelasticService.createObject(uploadResponse, authenticationResponse);
                if (createObject.getResult() == 0) {
                    myLogger.message("File registration : SUCCESS");
                    myLogger.message("  Registration ID : " + createObject.getResponse().getObject().getId());
                    myLogger.message("     Developer ID : " + createObject.getResponse().getObject().getDeveloper());
                    myLogger.message("------------------------------------------------------------------------");
                    val deploy = jelasticService.deploy(authenticationResponse, uploadResponse);
                    if (deploy.getResponse().getResult() == 0) {
                        myLogger.message("      Deploy file : SUCCESS");
                        myLogger.message("       Node Group : " + nodeGroup);
                        myLogger.message("       Deploy log :");
                        myLogger.message(deploy.getResponse().getResponses()[0].getOut());
                    } else {
                        myLogger.error("          Deploy : FAILED");
                        myLogger.error("           Error : " + deploy.getResponse().getError());
                        return BuildFinishedStatus.FINISHED_FAILED;
                    }
                } else {
                    myLogger.error("Create object : FAILED");
                    myLogger.error("        Error : " + createObject.getError());
                    return BuildFinishedStatus.FINISHED_FAILED;
                }
            } else {
                myLogger.error("File upload : FAILED");
                myLogger.error("      Error : " + uploadResponse.getError());
                return BuildFinishedStatus.FINISHED_FAILED;
            }
        } else {
            myLogger.error("Authentication : FAILED");
            myLogger.error("         Error : " + authenticationResponse.getError());
            return BuildFinishedStatus.FINISHED_FAILED;
        }
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
