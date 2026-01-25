<%--
    Initializes the window.oscarOpenInTabs JavaScript variable based on:
    1. System property open_in_tabs (true/false/optional)
    2. User preference tab_view (if system property is 'optional')

    Include this in pages where you want to support the "Open in Tabs" feature.
    Compatible with OSCAR 19 which uses UserProperty.OPEN_IN_TABS = "tab_view"
--%>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@page import="ca.openosp.openo.utility.SpringUtils" %>
<%@page import="ca.openosp.openo.commn.dao.UserPropertyDAO" %>
<%@page import="ca.openosp.openo.commn.model.UserProperty" %>
<%@page import="ca.openosp.OscarProperties" %>
<%
    boolean openInTabs = false;
    String openInTabsProperty = OscarProperties.getInstance().getProperty("open_in_tabs", "false");
    String debugInfo = "prop=" + openInTabsProperty;

    if ("true".equalsIgnoreCase(openInTabsProperty)) {
        // System-wide setting forces tabs
        openInTabs = true;
        debugInfo += ",forced=true";
    } else if ("optional".equalsIgnoreCase(openInTabsProperty)) {
        // Check user preference using OSCAR 19 compatible property name "tab_view"
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        debugInfo += ",optional=true";
        if (loggedInInfo != null && loggedInInfo.getLoggedInProviderNo() != null) {
            debugInfo += ",provider=" + loggedInInfo.getLoggedInProviderNo();
            UserPropertyDAO userPropertyDAO = SpringUtils.getBean(UserPropertyDAO.class);
            UserProperty userPref = userPropertyDAO.getProp(loggedInInfo.getLoggedInProviderNo(), "tab_view");
            if (userPref != null) {
                debugInfo += ",pref=" + userPref.getValue();
                if ("true".equalsIgnoreCase(userPref.getValue())) {
                    openInTabs = true;
                }
            } else {
                debugInfo += ",pref=null";
            }
        } else {
            debugInfo += ",noSession";
        }
    }
    // If "false" or not set, openInTabs remains false
%>
<script type="text/javascript">
    window.oscarOpenInTabs = <%= openInTabs %>;
    console.log("OpenInTabs: <%= openInTabs %>, debug: <%= debugInfo %>");
</script>
