//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.EncounterForm;
import org.springframework.stereotype.Repository;

@Repository
public class EncounterFormDaoImpl extends AbstractDaoImpl<EncounterForm> implements EncounterFormDao {

    public EncounterFormDaoImpl() {
        super(EncounterForm.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EncounterForm> findAll() {
        Query query = entityManager.createQuery("SELECT x FROM " + modelClass.getSimpleName() + " x");
        List<EncounterForm> results = query.getResultList();
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EncounterForm> findAllNotHidden() {
        Query query = entityManager
                .createQuery("SELECT x FROM " + modelClass.getSimpleName() + " x where x.displayOrder!=0");
        List<EncounterForm> results = query.getResultList();
        return results;
    }

    @Override
    public List<EncounterForm> findByFormName(String formName) {
        Query query = entityManager
                .createQuery("select x from " + modelClass.getSimpleName() + " x where x.formName=?1");
        query.setParameter(1, formName);

        @SuppressWarnings("unchecked")
        List<EncounterForm> results = query.getResultList();

        return (results);
    }

    @Override
    public List<EncounterForm> findByFormTable(String formTable) {
        Query query = entityManager
                .createQuery("select x from " + modelClass.getSimpleName() + " x where x.formTable=?1");
        query.setParameter(1, formTable);

        @SuppressWarnings("unchecked")
        List<EncounterForm> results = query.getResultList();

        return (results);
    }

}
