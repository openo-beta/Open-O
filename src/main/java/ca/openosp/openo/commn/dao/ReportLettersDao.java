//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ReportLetters;

public interface ReportLettersDao extends AbstractDao<ReportLetters> {
    List<ReportLetters> findCurrent();
}
