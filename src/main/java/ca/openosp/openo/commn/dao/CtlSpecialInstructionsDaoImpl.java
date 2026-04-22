

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ca.openosp.openo.commn.model.CtlSpecialInstructions;
import org.springframework.stereotype.Repository;

@Repository
public class CtlSpecialInstructionsDaoImpl extends AbstractDaoImpl<CtlSpecialInstructions> implements CtlSpecialInstructionsDao {

    public CtlSpecialInstructionsDaoImpl() {
        super(CtlSpecialInstructions.class);
    }

    @Override
    public List<CtlSpecialInstructions> findAll() {

        String sql = "select x from CtlSpecialInstructions x";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<CtlSpecialInstructions> results = query.getResultList();
        return results;
    }

	/**
	 * Finds a list of descriptions from the CtlSpecialInstructions table that match a given query string.
	 * The search is case-insensitive and uses a "like" operator with wildcards.
	 *
	 * @param descQuery The query string to search for within the descriptions.
	 *                  The search will look for descriptions that contain this string.
	 * @return A list of strings representing the matching descriptions.
	 */
	@Override
	public List<String> findDescriptionsMatching(String descQuery) {
		String query = "SELECT x.description FROM CtlSpecialInstructions x WHERE " +
				"x.description IS NOT NULL AND x.description LIKE :searchString";

		TypedQuery<String> typedQuery = super.entityManager.createQuery(query, String.class);
		typedQuery.setParameter("searchString", "%" + descQuery + "%");

		return typedQuery.getResultList();
	}
}
