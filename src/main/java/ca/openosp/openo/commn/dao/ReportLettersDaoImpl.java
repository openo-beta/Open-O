//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportLetters;
import org.springframework.stereotype.Repository;

@Repository
public class ReportLettersDaoImpl extends AbstractDaoImpl<ReportLetters> implements ReportLettersDao {

    public ReportLettersDaoImpl() {
        super(ReportLetters.class);
    }

    @Override
    public List<ReportLetters> findCurrent() {
        Query q = entityManager.createQuery("select l from ReportLetters l WHERE l.archive=?1 ORDER BY l.dateTime,l.reportName");
        q.setParameter(1, "0");

        @SuppressWarnings("unchecked")
        List<ReportLetters> results = q.getResultList();

        return results;
    }
}
