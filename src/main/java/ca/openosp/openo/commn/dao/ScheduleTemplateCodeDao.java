//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ScheduleTemplateCode;

public interface ScheduleTemplateCodeDao extends AbstractDao<ScheduleTemplateCode> {
    List<ScheduleTemplateCode> findAll();

    ScheduleTemplateCode getByCode(char code);

    List<ScheduleTemplateCode> findTemplateCodes();

    ScheduleTemplateCode findByCode(String code);
}
