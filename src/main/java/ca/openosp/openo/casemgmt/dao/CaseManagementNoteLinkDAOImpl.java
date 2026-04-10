//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.List;

import ca.openosp.openo.casemgmt.model.CaseManagementNoteLink;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CaseManagementNoteLinkDAOImpl extends HibernateDaoSupport implements CaseManagementNoteLinkDAO {

    @Override
    public CaseManagementNoteLink getNoteLink(Long id) {
        CaseManagementNoteLink noteLink = this.getHibernateTemplate().get(CaseManagementNoteLink.class, id);
        return noteLink;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CaseManagementNoteLink> getLinkByTableId(Integer tableName, Long tableId) {
        Object[] param = {tableName, tableId};
        String hql = "from CaseManagementNoteLink cLink where cLink.tableName = ?0 and cLink.tableId = ?1 order by cLink.id";
        return (List<CaseManagementNoteLink>) this.getHibernateTemplate().find(hql, param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CaseManagementNoteLink> getLinkByTableId(Integer tableName, Long tableId, String otherId) {
        Object[] param = {tableName, tableId, otherId};
        String hql = "from CaseManagementNoteLink cLink where cLink.tableName = ?0 and cLink.tableId = ?1 and cLink.otherId=?2 order by cLink.id";
        return (List<CaseManagementNoteLink>) this.getHibernateTemplate().find(hql, param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CaseManagementNoteLink> getLinkByTableIdDesc(Integer tableName, Long tableId) {
        Object[] param = {tableName, tableId};
        String hql = "from CaseManagementNoteLink cLink where cLink.tableName = ?0 and cLink.tableId = ?1 order by cLink.id desc";
        return (List<CaseManagementNoteLink>) this.getHibernateTemplate().find(hql, param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CaseManagementNoteLink> getLinkByTableIdDesc(Integer tableName, Long tableId, String otherId) {
        Object[] param = {tableName, tableId, otherId};
        String hql = "from CaseManagementNoteLink cLink where cLink.tableName = ?0 and cLink.tableId = ?1 and cLink.otherId=?2 order by cLink.id desc";
        return (List<CaseManagementNoteLink>) this.getHibernateTemplate().find(hql, param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CaseManagementNoteLink> getLinkByNote(Long noteId) {
        String hql = "from CaseManagementNoteLink cLink where cLink.noteId = ?0 order by cLink.id";
        return (List<CaseManagementNoteLink>) this.getHibernateTemplate().find(hql, noteId);
    }

    @Override
    public CaseManagementNoteLink getLastLinkByTableId(Integer tableName, Long tableId, String otherId) {
        return getLast(getLinkByTableId(tableName, tableId, otherId));
    }

    @Override
    public CaseManagementNoteLink getLastLinkByTableId(Integer tableName, Long tableId) {
        return getLast(getLinkByTableId(tableName, tableId));
    }

    @Override
    public CaseManagementNoteLink getLastLinkByNote(Long noteId) {
        return getLast(getLinkByNote(noteId));
    }

    private CaseManagementNoteLink getLast(List<CaseManagementNoteLink> listLink) {
        if (listLink.isEmpty())
            return null;
        return listLink.get(listLink.size() - 1);
    }

    @Override
    public void save(CaseManagementNoteLink cLink) {
        this.getHibernateTemplate().save(cLink);
    }

    @Override
    public void update(CaseManagementNoteLink cLink) {
        this.getHibernateTemplate().update(cLink);
    }
}
