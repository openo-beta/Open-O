//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.math.BigInteger;
import java.util.List;

import ca.openosp.openo.commn.model.AppointmentStatus;

public interface AppointmentStatusDao extends AbstractDao<AppointmentStatus> {

    public List<AppointmentStatus> findAll();

    public List<AppointmentStatus> findActive();

    public AppointmentStatus findByStatus(String status);

    public void modifyStatus(int ID, String strDesc, String strColor);

    public void changeStatus(int ID, int iActive);

    public int checkStatusUsuage(List<AppointmentStatus> allStatus);
}
