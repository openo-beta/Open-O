<%@ page import="org.oscarehr.util.LoggedInInfo" %>
<%@ page import="org.oscarehr.util.SpringUtils" %>
<%@ page import="org.oscarehr.common.dao.AppDefinitionDao" %>
<%@ page import="org.oscarehr.common.dao.AppUserDao" %>
<%@ page import="org.oscarehr.common.model.AppDefinition" %>
<%@ page import="org.oscarehr.common.model.AppUser" %>
<%@ page import="org.oscarehr.app.AppOAuth1Config" %>
<%@ page import="org.oscarehr.ws.rest.FormsService" %>
<%@ page import="org.codehaus.jettison.json.JSONArray" %>
<%@ page import="org.codehaus.jettison.json.JSONObject" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>
<%@page import="java.util.ArrayList" %>
<%@page import="org.apache.commons.text.StringEscapeUtils" %>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <script type="text/javascript" src="<%=request.getContextPath() %>/js/global.js"></script>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="eform.download.msgDownloadEform"/></title>
        <link rel="stylesheet" href="<%=request.getContextPath() %>/css/bootstrap.css" type="text/css">
        <link rel="stylesheet" href="<%=request.getContextPath() %>/css/bootstrap-responsive.css" type="text/css">
        <link rel="stylesheet" href="<%=request.getContextPath() %>/css/DT_bootstrap.css" type="text/css">
        <link rel="stylesheet" href="<%=request.getContextPath() %>/library/DataTables-1.10.12/media/css/jquery.dataTables.min.css" type="text/css">
        <script type="text/javascript" language="javascript" src="<%=request.getContextPath() %>/library/jquery/jquery-3.6.4.min.js"></script>
        <script type="text/javascript" language="javascript" src="<%=request.getContextPath() %>/library/DataTables/datatables.min.js"></script>
        <script type="text/javascript" language="javascript" src="<%=request.getContextPath() %>/library/DataTables-1.10.12/media/js/dataTables.bootstrap.min.js"></script>
        <style>
            h2 {
                font-size: 18.0pt;
                font-weight: normal;
                margin-top: 0;
                margin-bottom: 0;
            }

            body {
                font-size: 12.0pt;
                font-family: Helvetica;
            }
        </style>
    </head>
    <body>
    <div>
        <p>The K2A service has been discontinued and eForm downloads are no longer available through this interface.</p>
    </div>
    <div>&nbsp;</div>
    </body>
</html>