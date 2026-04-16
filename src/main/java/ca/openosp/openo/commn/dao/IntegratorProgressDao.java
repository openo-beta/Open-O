//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.IntegratorProgress;

public interface IntegratorProgressDao extends AbstractDao<IntegratorProgress> {
    List<IntegratorProgress> findCompleted();

    List<IntegratorProgress> findRunning();
}
