//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CaseManagementTmpSave;
import org.springframework.stereotype.Repository;

@Repository
public class CaseManagementTmpSaveDaoImpl extends AbstractDaoImpl<CaseManagementTmpSave> implements CaseManagementTmpSaveDao {

    private static final String NOTE_TAG_REGEXP = "^\\[[[:digit:]]{2}-[[:alpha:]]{3}-[[:digit:]]{4} \\.\\: [^]]*\\][[:space:]]+$";

    public CaseManagementTmpSaveDaoImpl() {
        super(CaseManagementTmpSave.class);
    }

    @Override
    public void remove(String providerNo, Integer demographicNo, Integer programId) {
        Query query = entityManager.createQuery("SELECT x FROM CaseManagementTmpSave x WHERE x.providerNo = ?1 and x.demographicNo=?2 and x.programId = ?3");
        query.setParameter(1, providerNo);
        query.setParameter(2, demographicNo);
        query.setParameter(3, programId);

        @SuppressWarnings("unchecked")
        List<CaseManagementTmpSave> results = query.getResultList();

        for (CaseManagementTmpSave result : results) {
            remove(result);
        }
    }

    @Override
    public CaseManagementTmpSave find(String providerNo, Integer demographicNo, Integer programId) {
        Query query = entityManager.createQuery("SELECT x FROM CaseManagementTmpSave x WHERE x.providerNo = ?1 and x.demographicNo=?2 and x.programId = ?3 order by x.updateDate DESC");
        query.setParameter(1, providerNo);
        query.setParameter(2, demographicNo);
        query.setParameter(3, programId);

        return this.getSingleResultOrNull(query);
    }

    @Override
    public CaseManagementTmpSave find(String providerNo, Integer demographicNo, Integer programId, Date date) {
        Query query = entityManager.createQuery("SELECT x FROM CaseManagementTmpSave x WHERE x.providerNo = ?1 and x.demographicNo=?2 and x.programId = ?3 and x.updateDate >= ?4 order by x.updateDate DESC");
        query.setParameter(1, providerNo);
        query.setParameter(2, demographicNo);
        query.setParameter(3, programId);
        query.setParameter(4, date);

        return this.getSingleResultOrNull(query);
    }

    @Override
    public boolean noteHasContent(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT * FROM casemgmt_tmpsave x WHERE x.id = ?1 and x.note  NOT REGEXP ?2 order by x.update_date DESC", CaseManagementTmpSave.class);

        query.setParameter(1, id);
        query.setParameter(2, NOTE_TAG_REGEXP);

        return (this.getSingleResultOrNull(query) != null);
    }
}
