//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.FormeCARES;

import java.util.Date;
import java.util.List;

public interface FormeCARESDao extends AbstractDao<FormeCARES> {
    List<FormeCARES> findAllByFormCreatedDateDemographicNo(Date createDate, int demographicNo);

    List<FormeCARES> findAllIncompleteByDemographicNumber(int demographicNo);
}
