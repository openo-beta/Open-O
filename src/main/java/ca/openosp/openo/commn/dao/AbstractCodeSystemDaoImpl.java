//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AbstractCodeSystemModel;

public abstract class AbstractCodeSystemDaoImpl<T extends AbstractCodeSystemModel<?>> extends AbstractDaoImpl<T> implements AbstractCodeSystemDao<T> {

    public AbstractCodeSystemDaoImpl(Class<T> modelClass) {
        super(modelClass);
    }

    public abstract List<T> searchCode(String term);

    public abstract T findByCode(String code);

    public abstract AbstractCodeSystemModel<?> findByCodingSystem(String codingSystem);

}
