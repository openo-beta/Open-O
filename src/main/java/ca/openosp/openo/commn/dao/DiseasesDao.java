//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Diseases;

public interface DiseasesDao extends AbstractDao<Diseases> {
    List<Diseases> findByDemographicNo(int demographicNo);

    List<Diseases> findByIcd9(String icd9);
}
