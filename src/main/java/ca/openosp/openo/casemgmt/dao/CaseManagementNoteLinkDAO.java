//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.List;

import ca.openosp.openo.casemgmt.model.CaseManagementNoteLink;

public interface CaseManagementNoteLinkDAO {

    public CaseManagementNoteLink getNoteLink(Long id);

    public List<CaseManagementNoteLink> getLinkByTableId(Integer tableName, Long tableId);

    public List<CaseManagementNoteLink> getLinkByTableId(Integer tableName, Long tableId, String otherId);

    public List<CaseManagementNoteLink> getLinkByTableIdDesc(Integer tableName, Long tableId);

    public List<CaseManagementNoteLink> getLinkByTableIdDesc(Integer tableName, Long tableId, String otherId);

    public List<CaseManagementNoteLink> getLinkByNote(Long noteId);

    public CaseManagementNoteLink getLastLinkByTableId(Integer tableName, Long tableId, String otherId);

    public CaseManagementNoteLink getLastLinkByTableId(Integer tableName, Long tableId);

    public CaseManagementNoteLink getLastLinkByNote(Long noteId);

    public void save(CaseManagementNoteLink cLink);

    public void update(CaseManagementNoteLink cLink);
}
