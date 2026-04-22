//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.TicklerTextSuggest;

public interface TicklerTextSuggestDao extends AbstractDao<TicklerTextSuggest> {

    public List<TicklerTextSuggest> getActiveTicklerTextSuggests();

    public List<TicklerTextSuggest> getInactiveTicklerTextSuggests();
}
