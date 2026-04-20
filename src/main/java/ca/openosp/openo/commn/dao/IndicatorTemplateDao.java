//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.IndicatorTemplate;

public interface IndicatorTemplateDao extends AbstractDao<IndicatorTemplate> {

    public List<IndicatorTemplate> getActiveIndicatorTemplates();

    public List<IndicatorTemplate> getIndicatorTemplatesByStatus(boolean status);

    public List<IndicatorTemplate> getIndicatorTemplates();

    public List<IndicatorTemplate> getNotSharedIndicatorTemplates();

    public List<IndicatorTemplate> getSharedIndicatorTemplates();

    public List<IndicatorTemplate> getIndicatorTemplatesByDashboardId(int id);

    public List<String> getIndicatorCategories();

    public List<String> getIndicatorSubCategories();

    public List<IndicatorTemplate> getIndicatorTemplatesByCategory(String category);

    public List<IndicatorTemplate> getIndicatorTemplatesBySubcategory(String subCategory);
}
