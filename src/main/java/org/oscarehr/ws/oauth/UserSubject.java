package org.oscarehr.ws.oauth;

import java.util.List;

public class UserSubject {

    private String loginName;
    private List<String> roles;

    public UserSubject(String loginName, List<String> roles) {
        this.loginName = loginName;
        this.roles = roles;
    }

    public String getLoginName() {
        return loginName;
    }

    public List<String> getRoles() {
        return roles;
    }
}
