//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.casemgmt.model.CaseManagementNoteExt;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class CaseManagementNoteExtDAOImpl extends HibernateDaoSupport implements CaseManagementNoteExtDAO {

    @Override
    public CaseManagementNoteExt getNoteExt(Long id) {
        CaseManagementNoteExt noteExt = this.getHibernateTemplate().get(CaseManagementNoteExt.class, id);
        return noteExt;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CaseManagementNoteExt> getExtByNote(Long noteId) {
        String hql = "from CaseManagementNoteExt cExt where cExt.noteId = ?0 order by cExt.id desc";
        return (List<CaseManagementNoteExt>) this.getHibernateTemplate().find(hql, noteId);
    }

    @Override
    public List getExtByKeyVal(String keyVal) {
        String hql = "from CaseManagementNoteExt cExt where cExt.keyVal = ?0";
        return this.getHibernateTemplate().find(hql, keyVal);
    }

    @Override
    public List getExtByValue(String keyVal, String value) {
        Object[] param = {keyVal, value};
        String hql = "from CaseManagementNoteExt cExt where cExt.keyVal = ?0 and cExt.value like ?1";
        return this.getHibernateTemplate().find(hql, param);
    }

    @Override
    public List getExtBeforeDate(String keyVal, Date dateValue) {
        Object[] param = {keyVal, dateValue};
        String hql = "from CaseManagementNoteExt cExt where cExt.keyVal = ?0 and cExt.dateValue <= ?1";
        return this.getHibernateTemplate().find(hql, param);
    }

    @Override
    public List getExtAfterDate(String keyVal, Date dateValue) {
        Object[] param = {keyVal, dateValue};
        String hql = "from CaseManagementNoteExt cExt where cExt.keyVal = ?0 and cExt.dateValue >= ?1";
        return this.getHibernateTemplate().find(hql, param);
    }

    @Override
    public void save(CaseManagementNoteExt cExt) {
        this.getHibernateTemplate().save(cExt);
    }

    @Override
    public void update(CaseManagementNoteExt cExt) {
        this.getHibernateTemplate().update(cExt);
    }
}
