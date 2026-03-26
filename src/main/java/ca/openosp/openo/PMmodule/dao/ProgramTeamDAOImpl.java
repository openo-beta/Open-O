//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import ca.openosp.openo.PMmodule.model.ProgramTeam;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

public class ProgramTeamDAOImpl extends HibernateDaoSupport implements ProgramTeamDAO {

    private Logger log = MiscUtils.getLogger();
    public SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactoryOverride(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.openosp.openo.daos.PMmodule.ProgramTeamDAO#teamExists(java.lang.Integer)
     */
    @Override
    public boolean teamExists(Integer teamId) {
        boolean exists = getHibernateTemplate().get(ProgramTeam.class, teamId) != null;
        log.debug("teamExists: " + exists);

        return exists;
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.openosp.openo.daos.PMmodule.ProgramTeamDAO#teamNameExists(java.lang.Integer, java.lang.String)
     */
    @Override
    public boolean teamNameExists(Integer programId, String teamName) {
        if (programId == null || programId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        if (teamName == null || teamName.length() <= 0) {
            throw new IllegalArgumentException();
        }
        // Session session = getSession();
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("select pt.id from ProgramTeam pt where pt.programId = ?1 and pt.name = ?2" );
        query.setParameter(1, programId.longValue());
        query.setParameter(2, teamName);

        List teams = new ArrayList();
        try {
            teams = query.list();
        } finally {
            // this.releaseSession(session);
            session.close();
        }

        if (log.isDebugEnabled()) {
            log.debug("teamNameExists: programId = " + programId + ", teamName = " + teamName + ", result = " + !teams.isEmpty());
        }

        return !teams.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.openosp.openo.daos.PMmodule.ProgramTeamDAO#getProgramTeam(java.lang.Integer)
     */
    @Override
    public ProgramTeam getProgramTeam(Integer id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        ProgramTeam result = this.getHibernateTemplate().get(ProgramTeam.class, id);

        if (log.isDebugEnabled()) {
            log.debug("getProgramTeam: id=" + id + ",found=" + (result != null));
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.openosp.openo.daos.PMmodule.ProgramTeamDAO#getProgramTeams(java.lang.Integer)
     */
    @Override
    public List<ProgramTeam> getProgramTeams(Integer programId) {
        if (programId == null || programId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        String sSQL = "from ProgramTeam tp where tp.programId = ?0";
        List<ProgramTeam> results = (List<ProgramTeam>) this.getHibernateTemplate().find(sSQL, programId);

        if (log.isDebugEnabled()) {
            log.debug("getProgramTeams: programId=" + programId + ",# of results=" + results.size());
        }

        return results;
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.openosp.openo.daos.PMmodule.ProgramTeamDAO#saveProgramTeam(ca.openosp.openo.model.PMmodule.ProgramTeam)
     */
    @Override
    public void saveProgramTeam(ProgramTeam team) {
        if (team == null) {
            throw new IllegalArgumentException();
        }

        this.getHibernateTemplate().saveOrUpdate(team);

        if (log.isDebugEnabled()) {
            log.debug("saveProgramTeam: id=" + team.getId());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.openosp.openo.daos.PMmodule.ProgramTeamDAO#deleteProgramTeam(java.lang.Integer)
     */
    @Override
    public void deleteProgramTeam(Integer id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        this.getHibernateTemplate().delete(getProgramTeam(id));

        if (log.isDebugEnabled()) {
            log.debug("deleteProgramTeam: id=" + id);
        }
    }

}
 