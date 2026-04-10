//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.RBTGroup;
import org.springframework.stereotype.Repository;

@Repository
public class RBTGroupDaoImpl extends AbstractDaoImpl<RBTGroup> implements RBTGroupDao {

    public RBTGroupDaoImpl() {
        super(RBTGroup.class);
    }

    /**
     * Deletes groups with the specified name and, optionally, template ID.
     *
     * @param groupName  Name of the group to delete
     * @param templateId ID of the template for the group to be deleted. In case
     *                   this value is set to null, only the group name is used for
     *                   deletion selection
     * @return Returns the number of the deleted groups
     */
    @Override
    public int deleteByNameAndTemplateId(String groupName, Integer templateId) {
        StringBuilder buf = new StringBuilder("DELETE FROM " + modelClass.getSimpleName() + " g WHERE g.groupName = ?1");
        if (templateId != null) {
            buf.append(" AND g.templateId = ?2");
        }

        Query query = entityManager.createQuery(buf.toString());
        query.setParameter(1, groupName);
        if (templateId != null) {
            query.setParameter(2, templateId);
        }

        return query.executeUpdate();
    }

    /**
     * Deletes groups with the specified name.
     *
     * @param groupName The name of the group to delete
     * @return Returns the number of the deleted groups
     */

    @Override
    public int deleteByName(String groupName) {
        return deleteByNameAndTemplateId(groupName, null);
    }

    /**
     * Retrieves a group from the database
     *
     * @param groupName The name of the group to retrieve
     * @return Returns a list of RBTGroup objects
     */
    @Override
    public List<RBTGroup> getByGroupName(String groupName) {
        String sql = "select tg from RBTGroup tg where tg.groupName=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, groupName);

        @SuppressWarnings("unchecked")
        List<RBTGroup> results = query.getResultList();

        return results;
    }

    /**
     * Retrieves a list of group names from the database
     *
     * @return Returns a list of names of groups
     */
    @Override
    public List<String> getGroupNames() {
        String sql = "select distinct tg.groupName from RBTGroup tg";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        if (results == null) {
            results = Collections.emptyList();
        }

        return results;
    }

}
