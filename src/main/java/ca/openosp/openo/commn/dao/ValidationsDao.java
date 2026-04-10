//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Validations;

public interface ValidationsDao extends AbstractDao<Validations> {

    public List<Validations> findAll();

    public List<Validations> findByAll(String regularExpParam, Double minValueParam, Double maxValueParam,
                                       Integer minLengthParam, Integer maxLengthParam, Boolean isNumericParam,
                                       Boolean isDateParam);

    public List<Validations> findByName(String name);

    public List<Object[]> findValidationsBy(Integer demo, String type, Integer validationId);
}
