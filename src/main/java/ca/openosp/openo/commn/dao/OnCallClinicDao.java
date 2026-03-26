//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;

import ca.openosp.openo.commn.model.OnCallClinic;

public interface OnCallClinicDao extends AbstractDao<OnCallClinic> {
    OnCallClinic findByDate(Date date);
}
