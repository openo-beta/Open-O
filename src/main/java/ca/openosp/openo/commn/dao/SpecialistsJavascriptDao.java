//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.SpecialistsJavascript;

public interface SpecialistsJavascriptDao extends AbstractDao<SpecialistsJavascript> {
    List<SpecialistsJavascript> findBySetId(String setId);
}
