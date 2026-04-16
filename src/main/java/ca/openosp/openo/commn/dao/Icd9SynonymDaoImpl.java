//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Icd9Synonym;
import org.springframework.stereotype.Repository;

/*
 * Author: Dennis Warren
 * Company: Colcamex Resources
 * Date: November 2014
 * For: UBC Pharmacy Clinic and McMaster Department of Family Medicine
 */
@Repository
public class Icd9SynonymDaoImpl extends AbstractDaoImpl<Icd9Synonym> implements Icd9SynonymDao {

    public Icd9SynonymDaoImpl() {
        super(Icd9Synonym.class);
    }

    @Override
    public Icd9Synonym findPatientFriendlyTranslationFor(String dxCode) {
        String queryString = "SELECT x FROM Icd9Synonym x WHERE x.dxCode LIKE ?1";
        Query query = entityManager.createQuery(queryString);
        query.setParameter(1, dxCode);

        return super.getSingleResultOrNull(query);
    }

}
