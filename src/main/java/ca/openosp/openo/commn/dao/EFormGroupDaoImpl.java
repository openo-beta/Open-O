//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.EFormGroup;
import org.springframework.stereotype.Repository;

@Repository
public class EFormGroupDaoImpl extends AbstractDaoImpl<EFormGroup> implements EFormGroupDao {

    public EFormGroupDaoImpl() {
        super(EFormGroup.class);
    }

    /**
     * Deletes groups with the specified name and, optionally, form ID.
     *
     * @param groupName Name of the group to delete
     * @param formId    ID of the form for the group to be deleted. In case this
     *                  value is set to null, only the group name is used for
     *                  deletion selection
     * @return Returns the number of the deleted groups
     */
    @Override
    public int deleteByNameAndFormId(String groupName, Integer formId) {
        StringBuilder buf = new StringBuilder(
                "DELETE FROM " + modelClass.getSimpleName() + " g WHERE g.groupName = ?1");
        if (formId != null)
            buf.append(" AND g.formId = ?2");

        Query query = entityManager.createQuery(buf.toString());
        query.setParameter(1, groupName);
        if (formId != null)
            query.setParameter(2, formId);

        return query.executeUpdate();
    }

    @Override
    public int deleteByName(String groupName) {
        return deleteByNameAndFormId(groupName, null);
    }

    @Override
    public List<EFormGroup> getByGroupName(String groupName) {
        String sql = "select eg from EFormGroup eg where eg.groupName=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, groupName);

        @SuppressWarnings("unchecked")
        List<EFormGroup> results = query.getResultList();

        return results;
    }

    @Override
    public List<String> getGroupNames() {
        String sql = "select distinct eg.groupName from EFormGroup eg";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        return results;
    }

}
