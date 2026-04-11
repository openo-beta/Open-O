//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.IntegratorControl;

import java.util.List;

public interface IntegratorControlDao extends AbstractDao<IntegratorControl> {
    public static final String REMOVE_DEMO_ID_CTRL = "RemoveDemographicIdentity";
    public static final String UPDATE_INTERVAL_CTRL = "UpdateInterval";
    public static final String INTERVAL_HR = "h";

    List<IntegratorControl> getAllByFacilityId(Integer facilityId);

    boolean readRemoveDemographicIdentity(Integer facilityId);

    void saveRemoveDemographicIdentity(Integer facilityId, boolean removeDemoId);

    Integer readUpdateInterval(Integer facilityId);

    void saveUpdateInterval(Integer facilityId, Integer updateInterval);

    void save(IntegratorControl ic);
}
