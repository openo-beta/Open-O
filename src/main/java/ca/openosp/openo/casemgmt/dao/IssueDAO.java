//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.List;

import ca.openosp.openo.casemgmt.model.Issue;

import ca.openosp.openo.model.security.Secrole;

public interface IssueDAO {

    public Issue getIssue(Long id);

    public List<Issue> getIssues();

    public List<Issue> findIssueByCode(String[] codes);

    public Issue findIssueByCode(String code);

    public Issue findIssueByTypeAndCode(String type, String code);

    public void saveIssue(Issue issue);

    public void delete(Long issueId);

    public List<Issue> findIssueBySearch(String search);

    public List<Long> getIssueCodeListByRoles(List<Secrole> roles);

    public List<Issue> search(String search, List<Secrole> roles, final int startIndex, final int numToReturn);

    public Integer searchCount(String search, List<Secrole> roles);

    public List searchNoRolesConcerned(String search);

    public List<String> getLocalCodesByCommunityType(String type);

}
