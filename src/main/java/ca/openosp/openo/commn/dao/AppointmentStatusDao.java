//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.math.BigInteger;
import java.util.List;

import ca.openosp.openo.commn.model.AppointmentStatus;

public interface AppointmentStatusDao extends AbstractDao<AppointmentStatus> {

    public List<AppointmentStatus> findAll();

    public List<AppointmentStatus> findActive();

    public AppointmentStatus findByStatus(String status);

    /**
     * Saves several statuses together, so either every change lands or none does.
     *
     * @param statuses List<AppointmentStatus> the changed statuses to merge
     * @since 2026-09-21
     */
    public void mergeAll(List<AppointmentStatus> statuses);

    public void changeStatus(int ID, int iActive);

    public int checkStatusUsuage(List<AppointmentStatus> allStatus);
}
