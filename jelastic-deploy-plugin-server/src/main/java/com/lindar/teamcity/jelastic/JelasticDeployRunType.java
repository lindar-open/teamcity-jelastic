package com.lindar.teamcity.jelastic;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.lindar.teamcity.AppCommon.JELASTIC_RUN_TYPE;

public class JelasticDeployRunType extends RunType {

    private final PluginDescriptor myDescriptor;

    public JelasticDeployRunType(@NotNull final RunTypeRegistry registry,
                              @NotNull final PluginDescriptor descriptor) {
        registry.registerRunType(this);
        myDescriptor = descriptor;
    }

    @NotNull
    @Override
    public String getType() {
        return JELASTIC_RUN_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Jelastic Deploy";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Deploy a artifact to a Jelastic environment";
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new JelasticPropertiesProcessor();
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return myDescriptor.getPluginResourcesPath() + "editJelasticDeployParams.jsp";
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return myDescriptor.getPluginResourcesPath() + "viewJelasticDeployParams.jsp";
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
