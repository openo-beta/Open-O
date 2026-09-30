//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

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

    /**
     * Whether any appointment, on any date, has the status. Signed and verified appointments count,
     * and codes are compared case-sensitively, since h and H are different statuses.
     *
     * @param statusCode String the status code
     * @return boolean true if at least one appointment has the status; false for a null or empty code
     * @since 2026-09-30
     */
    public boolean isInUse(String statusCode);
}
