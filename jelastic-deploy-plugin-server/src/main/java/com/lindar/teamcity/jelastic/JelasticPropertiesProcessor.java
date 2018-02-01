package com.lindar.teamcity.jelastic;

import com.lindar.teamcity.AppCommon;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class JelasticPropertiesProcessor implements PropertiesProcessor {
    @Override
    public Collection<InvalidProperty> process(Map<String, String> properties) {
        Collection<InvalidProperty> result = new HashSet<InvalidProperty>();

        if (StringUtil.isEmptyOrSpaces(properties.get(AppCommon.PARAM_API))) {
            result.add(new InvalidProperty(AppCommon.PARAM_API, "Api Url is required"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(AppCommon.PARAM_USERNAME))) {
            result.add(new InvalidProperty(AppCommon.PARAM_USERNAME, "Username is required"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(AppCommon.PARAM_PASSWORD))) {
            result.add(new InvalidProperty(AppCommon.PARAM_PASSWORD, "Password is required"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(AppCommon.PARAM_ENVIRONMENT))) {
            result.add(new InvalidProperty(AppCommon.PARAM_ENVIRONMENT, "Environment is required"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(AppCommon.PARAM_FILEPATH))) {
            result.add(new InvalidProperty(AppCommon.PARAM_FILEPATH, "Filepath is required"));
        }

        return result;
    }
}
