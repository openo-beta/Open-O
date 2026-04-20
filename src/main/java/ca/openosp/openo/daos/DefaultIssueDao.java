//CHECKSTYLE:OFF

package ca.openosp.openo.daos;

import ca.openosp.openo.commn.dao.AbstractDao;
import ca.openosp.openo.model.DefaultIssue;

import java.util.List;

public interface DefaultIssueDao extends AbstractDao<DefaultIssue> {

    public DefaultIssue findDefaultIssue(Integer id);

    public DefaultIssue getLastestDefaultIssue();

    public List<DefaultIssue> findAll();

    public void saveDefaultIssue(DefaultIssue issue);

    public String[] findAllDefaultIssueIds();
}
