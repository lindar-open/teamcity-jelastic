<%@ page import="com.lindar.teamcity.AppCommon" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
    Api URL: <strong><props:displayValue name="<%=AppCommon.PARAM_API%>"
                                            emptyValue="default"/></strong>
</div>

<div class="parameter">
    Username: <strong><props:displayValue name="<%=AppCommon.PARAM_USERNAME%>"
                                          emptyValue="none"/></strong>
</div>

<div class="parameter">
    Environment: <strong><props:displayValue name="<%=AppCommon.PARAM_ENVIRONMENT%>"
                                        emptyValue="none"/></strong>
</div>

<div class="parameter">
    Node Group: <strong><props:displayValue name="<%=AppCommon.PARAM_NODE_GROUP%>"
                                             emptyValue="none"/></strong>
</div>

<div class="parameter">
    File Path: <strong><props:displayValue name="<%=AppCommon.PARAM_FILEPATH%>"
                                             emptyValue="none"/></strong>
</div>
