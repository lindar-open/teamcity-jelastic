<%@ page import="com.lindar.teamcity.AppCommon" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<l:settingsGroup title="Jelastic Deploy Parameters" className="advancedSetting">
    <tr>
        <th class="noBorder"><label>Api URL: </label></th>
        <td>
            <props:textProperty name="<%=AppCommon.PARAM_API%>" className="longField"/>
            <span class="smallNote">The api for the Jelastic install (for mircloud use app.mircloud.host).</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"><label>Username: </label></th>
        <td>
            <props:textProperty name="<%=AppCommon.PARAM_USERNAME%>"  className="longField"/>
            <span class="smallNote">Username to login to Jelastic.</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"><label>Password: </label></th>
        <td>
            <props:passwordProperty name="<%=AppCommon.PARAM_PASSWORD%>"  className="longField"/>
            <span class="smallNote">Password to login to Jelastic.</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"><label>Environment: </label></th>
        <td>
            <props:textProperty name="<%=AppCommon.PARAM_ENVIRONMENT%>"  className="longField"/>
            <span class="smallNote">Environment to deploy to.</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"><label>Node Group: </label></th>
        <td>
            <props:textProperty name="<%=AppCommon.PARAM_NODE_GROUP%>"  className="longField"/>
            <span class="smallNote">(optional) defaults to the first node</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"><label>File Path: </label></th>
        <td>
            <props:textProperty name="<%=AppCommon.PARAM_FILEPATH%>"  className="longField"/>
            <span class="smallNote">Path to file / artifact to deploy</span>
        </td>
    </tr>
</l:settingsGroup>
