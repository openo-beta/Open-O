//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.TicklerTextSuggest;
import org.springframework.stereotype.Repository;

@Repository
public class TicklerTextSuggestDaoImpl extends AbstractDaoImpl<TicklerTextSuggest> implements TicklerTextSuggestDao {

    public TicklerTextSuggestDaoImpl() {
        super(TicklerTextSuggest.class);
    }

    @Override
    public List<TicklerTextSuggest> getActiveTicklerTextSuggests() {
        Query query = entityManager.createQuery("SELECT tTextSuggest from TicklerTextSuggest tTextSuggest WHERE tTextSuggest.active = ?1 order by tTextSuggest.suggestedText");
        query.setParameter(1, true);

        @SuppressWarnings("unchecked")
        List<TicklerTextSuggest> results = query.getResultList();
        return results;
    }

    @Override
    public List<TicklerTextSuggest> getInactiveTicklerTextSuggests() {
        Query query = entityManager.createQuery("SELECT tTextSuggest from TicklerTextSuggest tTextSuggest WHERE tTextSuggest.active = ?1 order by tTextSuggest.suggestedText");
        query.setParameter(1, false);

        @SuppressWarnings("unchecked")
        List<TicklerTextSuggest> results = query.getResultList();
        return results;
    }
}
