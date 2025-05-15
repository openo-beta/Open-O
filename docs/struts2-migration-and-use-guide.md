# Struts 2 Migration Pattern Guide and New Action Documentation, Open-O
## Overview

This guide covers the approaches used in both migrating old Struts1 logic to Struts2, as well as creating new Struts2 actions, for the Open-O project.  Choices were made on this approach to make the migration easiest given previous Struts1 usage methods, and align for future upgrades into Struts6 easier.  

The goal of this guide is to standardize work done on this regarding the structure of Java actions, JSP views, and JS behavior to ensure compatibility with Struts 2, while supporting maintainability, localization, and safe session usage across the project. 

This guide should be updated when errors, omissions, or new design standards are implemented by the project.  The initial version of this documented has been created by professor Michael Yingbull of Conestoga College ITAL (Waterloo, Ontario) as part of applied research work involving this codebase on behalf of Magenta Health.  It includes work and notes done on this effort by research assistant David Dong, refactoring and additions through the use of AI, and those of any and all that provide future contributions (pull requests welcome). 

## 1. Action Class Structure
### 1.1 Required Imports

    import com.opensymphony.xwork2.ActionSupport;
    import javax.servlet.http.HttpServletRequest; import
    javax.servlet.http.HttpSession; import
    org.apache.struts2.interceptor.ServletRequestAware;

### 1.2 Base Class and Interfaces

    public class MyNewAction extends ActionSupport implements ServletRequestAware {
        private HttpServletRequest request;
        public void setServletRequest(HttpServletRequest request) {
            this.request = request;
        }
            public String execute() throws Exception {
            String method = request.getParameter("method");
            if ("someMethod".equals(method)) return someMethod();
            return SUCCESS;
        }
            public String someMethod() {
            // logic here
            return SUCCESS;
        }
    }

### 1.3 Session Handling

Use the request to get session-scoped attributes, and null-check everything:

    HttpSession session = request.getSession();
    MyFormBean bean = (MyFormBean) session.getAttribute("mySessionKey"); 
    if (bean == null) { bean = new MyFormBean();
        session.setAttribute("mySessionKey", bean);
    }

## 2. JSP Migration using JSTL 

### 2.1 Taglib Declarations

    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

### 2.2 Example Struts2-related Form
### Example Form

    <fmt:setBundle basename="oscarResources"/>
    <c:set var="formKey" value="mySessionFormKey" />
    <c:set var="sessionFrm" value="${sessionScope[formKey]}" />

    <form method="post" action="MyNewAction.do">
        <c:forEach var="checkbox" items="${sessionFrm.issueCheckList}">
            <input type="checkbox"
                name="issue_id"
                value="${checkbox.issue.issue_id}"
                <c:if test="${checkbox.checked == 'on'}">checked</c:if> />
            <c:out value="${checkbox.issue.description}" />
        </c:forEach>

        <input type="hidden" name="encType" value="${sessionScope.encType}" />
        <input type="submit" value="Save" />
    </form>

### 2.3 Be sure to use JSTL <c:forEach> and EL instead of \<html:> or \<bean:>

    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> <c:set var="formKey" value="mySessionFormKey" />
    <c:set var="sessionFrm" value="${sessionScope[formKey]}" />checked> 

## 3. JavaScript Synchronization (if dynamic rendering)

### 3.1 When checkboxes or dynamic fields are rendered in JS, be sure to inject them into the form

    function injectHiddenField(fieldName, fieldValue, formId) {
        const form = document.getElementById(formId);
        if (form) {
            let input = document.createElement("input");
            input.type = "hidden";
            input.name = fieldName;
            input.value = fieldValue;
            form.appendChild(input);
        }
    }

    injectHiddenField("encType", "SocHistory", "noteForm");

## 🔹 4. Session-to-Form Injection for Contextual Params (encType)

In Java

        String encType = request.getParameter("encType");
        if (encType != null) {
            session.setAttribute("encType", encType);
        }
In JSP

        <input type="hidden" name="encType" value="${sessionScope.encType}" />

## 5. Tag Migration Reference Table


|Legacy Taglib    |Replace With                     |Example                                                             |
|-----------------|---------------------------------|--------------------------------------------------------------------|
|`\<bean:write />`|`\<c:out value="${...}"/>`       |`\<c:out value="${note.note}" />`                                   |
|`\<bean:message />`|`\<fmt:message />`               |`\<fmt:setBundle basename="oscarResources"/>\<fmt:message key="label"/>`|
|`\<html:checkbox />`|`\<input type="checkbox" />`     |With JSTL `\<c:if>` for `checked`                                   |
|`\<html:text />` |`\<input type="text" />`         |Plain HTML with `${}` binding                                       |
|`\<html:form />` |`\<form method="post" action="...">`|Use standard HTML `\<form>`                                         |
|`\<html:submit />`|`\<input type="submit" />`       |Plain HTML submit button                                            |

## 6. Dispatcher Method Pattern (Struts 1 "Compatibility")

    public String execute() {
        String method = request.getParameter("method");
        if ("save".equals(method)) return save();
        if ("delete".equals(method)) return delete();
        return SUCCESS;
    }

## 7. Security Notes


|Concern       |Mitigation                                                  |
|--------------|------------------------------------------------------------|
|XSS           |Use `\<c:out>` to escape dynamic text                       |
|Input Handling|Sanitize and validate all request parameters in Java        |
|Session Access|Always null-check session-scoped attributes before accessing|

## 8. Migration Process Summary

- Replace Struts 1 tags with JSTL + HTML
- Follow ActionSupport pattern with ServletRequestAware
- Use `\<c:forEach>` and `\<c:if>` to iterate and conditionally display content
- Localize content using `\<fmt:message>`
- Inject context dynamically (e.g., `encType`) via session-safe EL or JavaScript


## Appendix: Migration Tools & Scripts

Notes on migration tools and scripts used in this process to help migrate existing code referencing Struts1. 

### 1. Replace <bean:message> with JSTL

    grep -rl '<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>' src | xargs sed -i 's|<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>|<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>|g'

### 2. Replace <bean:write>

    find src/ -type f -name "*.jsp" -exec sed -i 's/<bean:write name="\([^"\]*\)" \/>/<c:out value="${}" \/>/g' {} +
    find src -type f -name "*.jsp" -exec sed -i 's/<bean:write name="([^"]*)" property="([^"]*)"/>/<c:out value="${.}"/>/g' {} +

### 3. Replace <html:password>

    <!-- before -->
    <html:password property="password"/>



