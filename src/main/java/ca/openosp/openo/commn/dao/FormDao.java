//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Form;

public interface FormDao extends AbstractDao<Form> {
    List<Form> findByDemographicNo(Integer demographicNo);

    Form search_form_no(Integer demographicNo, String formName);

    List<Form> findAllGroupByDemographicNo();
}
