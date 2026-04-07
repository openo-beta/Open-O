//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.documentManager.EDocUtil.EDocSort;

public interface DocumentMergeDemographicDAO extends DocumentDao {
    List<Object[]> findDocuments(String module, String moduleid, String docType, boolean includePublic, boolean includeDeleted, boolean includeActive, EDocSort sort, Date since);

    List<Document> findByDemographicId(String demoNo);
}
