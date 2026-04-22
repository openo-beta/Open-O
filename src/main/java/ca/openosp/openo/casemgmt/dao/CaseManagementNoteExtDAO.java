//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.casemgmt.model.CaseManagementNoteExt;

public interface CaseManagementNoteExtDAO {

    public CaseManagementNoteExt getNoteExt(Long id);

    public List<CaseManagementNoteExt> getExtByNote(Long noteId);

    public List getExtByKeyVal(String keyVal);

    public List getExtByValue(String keyVal, String value);

    public List getExtBeforeDate(String keyVal, Date dateValue);

    public List getExtAfterDate(String keyVal, Date dateValue);

    public void save(CaseManagementNoteExt cExt);

    public void update(CaseManagementNoteExt cExt);
}
