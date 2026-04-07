//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.Icd9Synonym;

public interface Icd9SynonymDao extends AbstractDao<Icd9Synonym> {
    Icd9Synonym findPatientFriendlyTranslationFor(String dxCode);
}
