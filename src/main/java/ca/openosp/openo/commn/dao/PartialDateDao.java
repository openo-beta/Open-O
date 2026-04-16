//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;

import ca.openosp.openo.commn.model.PartialDate;

public interface PartialDateDao extends AbstractDao<PartialDate> {
    PartialDate getPartialDate(Integer tableName, Integer tableId, Integer fieldName);

    String getDatePartial(Date fieldDate, Integer tableName, Integer tableId, Integer fieldName);

    String getDatePartial(String fieldDate, Integer tableName, Integer tableId, Integer fieldName);

    String getDatePartial(Date partialDate, String format);

    String getDatePartial(String dateString, String format);

    void setPartialDate(String fieldDate, Integer tableName, Integer tableId, Integer fieldName);

    void setPartialDate(Integer tableName, Integer tableId, Integer fieldName, String format);

    String getFormat(Integer tableName, Integer tableId, Integer fieldName);

    String getFormat(String dateValue);

    String getFullDate(String partialDate);

    Date StringToDate(String partialDate);
}
