//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Map;

import ca.openosp.openo.commn.model.View;

public interface ViewDao extends AbstractDao<View> {
    Map<String, View> getView(String view, String role);

    Map<String, View> getView(String view, String role, String providerNo);

    void saveView(View v);

    void delete(View v);
}
