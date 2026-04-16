//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.EncounterTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EncounterTemplateDaoImpl extends AbstractDaoImpl<EncounterTemplate> implements EncounterTemplateDao {

    public EncounterTemplateDaoImpl() {
        super(EncounterTemplate.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EncounterTemplate> findAll() {
        Query query = entityManager.createQuery("SELECT x FROM " + modelClass.getSimpleName() + " x");
        List<EncounterTemplate> results = query.getResultList();
        return results;
    }

    @Override
    public List<EncounterTemplate> findByName(String name) {
        Query query = entityManager.createQuery("select x from EncounterTemplate x where x.encounterTemplateName like ?1");
        query.setParameter(1, name);

        @SuppressWarnings("unchecked")
        List<EncounterTemplate> results = query.getResultList();

        return results;
    }

    @Override
    public List<EncounterTemplate> findByName(String name, Integer startIndex, Integer itemsToReturn) {
        Query query = entityManager.createQuery("select x from EncounterTemplate x where x.encounterTemplateName like ?1");
        query.setParameter(1, name);

        query.setFirstResult(startIndex);
        query.setMaxResults(itemsToReturn);

        @SuppressWarnings("unchecked")
        List<EncounterTemplate> results = query.getResultList();

        return results;
    }
}
