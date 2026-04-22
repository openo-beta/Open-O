//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlFrequency;

public interface CtlFrequencyDao extends AbstractDao<CtlFrequency> {
    List<CtlFrequency> findAll();
}
