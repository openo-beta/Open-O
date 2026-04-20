//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.IssueGroup;

public interface IssueGroupDao extends AbstractDao<IssueGroup> {
    List<IssueGroup> findAll();
}
