//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.model.EForm;

public interface EFormDao extends AbstractDao<EForm> {

    public enum EFormSortOrder {
        DATE, NAME, SUBJECT, FILE_NAME;
    }

    public EForm findByName(String name);

    public List<EForm> findByNameSimilar(String name);

    public EForm findById(Integer formId);

    public List<EForm> findAll(Boolean current);

    public boolean isIndivicaRTLEnabled();

    public void setIndivicaRTLEnabled(boolean enabled);

    public List<EForm> findByStatus(boolean status);

    public List<EForm> findByStatus(boolean status, EFormSortOrder sortOrder);

    public Integer findMaxIdForActiveForm(String formName);

    public Long countFormsOtherThanSpecified(String formName, Integer id);

    public List<EForm> getEfromInGroupByGroupName(String groupName);
}
