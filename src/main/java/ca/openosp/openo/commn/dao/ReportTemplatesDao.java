//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ReportTemplates;

public interface ReportTemplatesDao extends AbstractDao<ReportTemplates> {
    List<ReportTemplates> findAll();

    List<ReportTemplates> findActive();

    ReportTemplates findByUuid(String uuid);
}
