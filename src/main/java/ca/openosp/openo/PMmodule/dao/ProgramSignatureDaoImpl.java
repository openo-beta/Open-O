//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.model.ProgramSignature;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class ProgramSignatureDaoImpl extends HibernateDaoSupport implements ProgramSignatureDao {

    private static final Logger log = MiscUtils.getLogger();

    //get the creator of the program
    public ProgramSignature getProgramFirstSignature(Integer programId) {
        ProgramSignature programSignature = null;
        if (programId == null || programId.intValue() <= 0) {
            return null;
        }
        String sSQL = "FROM ProgramSignature ps where ps.programId = ?0 ORDER BY ps.updateDate ASC";
        List ps = getHibernateTemplate().find(sSQL, programId);

        if (!ps.isEmpty()) {
            programSignature = (ProgramSignature) ps.get(0);
        }

        if (log.isDebugEnabled()) {
            log.debug("getProgramFirstSignature: " + ((programSignature != null) ? String.valueOf(programSignature.getId()) : "null"));
        }

        return programSignature;
    }

    public List<ProgramSignature> getProgramSignatures(Integer programId) {
        if (programId == null || programId.intValue() <= 0) {
            return null;
        }

        String sSQL = "FROM ProgramSignature ps WHERE ps.programId = ?0 ORDER BY ps.updateDate ASC";
        List rs = getHibernateTemplate().find(sSQL, programId);

        if (log.isDebugEnabled()) {
            log.debug("getProgramSignatures: # of programs: " + rs.size());
        }
        return rs;

    }

    public void saveProgramSignature(ProgramSignature programSignature) {
        if (programSignature == null) {
            throw new IllegalArgumentException();
        }
        programSignature.setUpdateDate(new Date());
        getHibernateTemplate().saveOrUpdate(programSignature);
        getHibernateTemplate().flush();

        if (log.isDebugEnabled()) {
            log.debug("saveAdmission: id= " + programSignature.getId());
        }
    }
}