package com.lindar.teamcity.jelastic.util;

public interface JelasticProperties {
    String JELASTIC_PREDEPLOY_HOOK  = "jelastic-predeploy-hook";
    String JELASTIC_POSTDEPLOY_HOOK = "jelastic-postdeploy-hook";
    String NODE_GROUP               = "nodeGroup";
    String NODE_GROUP_DISPLAY_NAME  = "nodeGroupDisplayName";
    String DELAY                    = "delay";
    String ENVIRONMENT              = "environment";
    String CONTEXT                  = "context";
    String JELASTIC_PASSWORD        = "jelastic-password";
    String JELASTIC_HOSTER          = "jelastic-hoster";

    String AUTH_URL = "https://%s/1.0/users/authentication/rest/signin";
    String UPLOAD_URL = "https://%s/1.0/storage/uploader/rest/upload";
    String DEPLOY_URL = "https://%s/1.0/environment/deployment/rest/deployarchive";

    String GET_DOMAINS_PATH = "/1.0/environment/binder/rest/getdomains";
    String GET_NODEGROUP_PATH = "/1.0/environment/nodegroup/rest/get";

    String FID_VALUE     = "123456";
    String FID_PARAM     = "fid";
    String SESSION_PARAM = "session";
    String FILE_PARAM    = "file";
}
