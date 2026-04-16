//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Dashboard;

public interface DashboardDao extends AbstractDao<Dashboard> {
    List<Dashboard> getActiveDashboards();

    List<Dashboard> getDashboardsByStatus(boolean status);

    List<Dashboard> getDashboards();
}
