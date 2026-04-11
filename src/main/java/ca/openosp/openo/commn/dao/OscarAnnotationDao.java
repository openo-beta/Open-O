//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.OscarAnnotation;

public interface OscarAnnotationDao extends AbstractDao<OscarAnnotation> {
    OscarAnnotation getAnnotations(String demoNo, String tableName, Long tableId);

    void save(OscarAnnotation anno);

    int getNumberOfNotes(String demoNo, String tableName, Long tableId);
}
