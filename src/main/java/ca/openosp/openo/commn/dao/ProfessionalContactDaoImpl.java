//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ProfessionalContact;
import org.springframework.stereotype.Repository;

@Repository
public class ProfessionalContactDaoImpl extends AbstractDaoImpl<ProfessionalContact> implements ProfessionalContactDao {

    public ProfessionalContactDaoImpl() {
        super(ProfessionalContact.class);
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    @Override
    public List<ProfessionalContact> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public List<ProfessionalContact> search(String searchMode, String orderBy, String keyword) {
        StringBuilder where = new StringBuilder();
        List<String> paramList = new ArrayList<String>();

        // Validate searchMode to prevent SQL injection
        String validatedSearchMode = validateSearchMode(searchMode);
        
        if (validatedSearchMode.equals("search_name")) {
            String[] temp = keyword.split("\\,\\p{Space}*");
            if (temp.length > 1) {
                where.append("c.lastName like ?1 and c.firstName like ?2");
                paramList.add(temp[0] + "%");
                paramList.add(temp[1] + "%");
            } else {
                where.append("c.lastName like ?1");
                paramList.add(temp[0] + "%");
            }
        } else {
            where.append("c.").append(validatedSearchMode).append(" like ?1");
            paramList.add(keyword + "%");
        }
        
        // Validate and sanitize orderBy to prevent SQL injection
        String validatedOrderBy = validateOrderBy(orderBy);
        
        String sql = "SELECT c from ProfessionalContact c where " + where.toString() + " order by " + validatedOrderBy;

        Query query = entityManager.createQuery(sql);
        for (int x = 0; x < paramList.size(); x++) {
            query.setParameter(x + 1, paramList.get(x));
        }

        @SuppressWarnings("unchecked")
        List<ProfessionalContact> contacts = query.getResultList();
        return contacts;
    }
    
    /**
     * Validates the searchMode parameter to ensure it only contains valid column names
     * @param searchMode the search mode to validate
     * @return the validated search mode
     * @throws IllegalArgumentException if searchMode is invalid
     */
    private String validateSearchMode(String searchMode) {
        if (searchMode == null || searchMode.trim().isEmpty()) {
            return "lastName"; // default to lastName
        }
        
        // Valid searchable fields from Contact and ProfessionalContact entities
        String[] validSearchModes = {
            "search_name", "updateDate", "id", "lastName", "firstName", "address", "address2", 
            "city", "province", "country", "postal", "residencePhone", 
            "cellPhone", "workPhone", "workPhoneExtension", "email", "fax", "note", "deleted",
            "specialty", "cpso", "systemId"
        };
        
        for (String validMode : validSearchModes) {
            if (validMode.equals(searchMode)) {
                return searchMode;
            }
        }
        
        throw new IllegalArgumentException("Invalid search mode: " + searchMode);
    }
    
    /**
     * Validates the orderBy parameter to ensure it only contains valid column names and sort orders
     * @param orderBy the order by clause to validate
     * @return the validated order by clause
     * @throws IllegalArgumentException if orderBy contains invalid column names
     */
    private String validateOrderBy(String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return "c.lastName, c.firstName"; // default ordering
        }
        
        // Valid sortable fields from Contact and ProfessionalContact entities
        String[] validColumns = {
            "updateDate", "id", "lastName", "firstName", "address", "address2", "city", 
            "province", "country", "postal", "residencePhone", "cellPhone", 
            "workPhone", "workPhoneExtension", "email", "fax", "note", "deleted", 
            "specialty", "cpso", "systemId"
        };
        
        StringBuilder validatedOrderBy = new StringBuilder();
        String[] orderByParts = orderBy.split(",");
        
        for (int i = 0; i < orderByParts.length; i++) {
            String part = orderByParts[i].trim();
            if (part.isEmpty()) continue;
            
            // Remove any "c." prefix if present
            if (part.startsWith("c.")) {
                part = part.substring(2);
            }
            
            // Split column and sort order (ASC/DESC)
            String[] columnAndOrder = part.split("\\s+");
            String column = columnAndOrder[0];
            String sortOrder = "";
            
            if (columnAndOrder.length > 1) {
                String order = columnAndOrder[1].toUpperCase();
                if ("ASC".equals(order) || "DESC".equals(order)) {
                    sortOrder = " " + order;
                }
            }
            
            // Validate column name
            boolean isValid = false;
            for (String validColumn : validColumns) {
                if (validColumn.equals(column)) {
                    isValid = true;
                    break;
                }
            }
            
            if (!isValid) {
                throw new IllegalArgumentException("Invalid order by column: " + column);
            }
            
            if (i > 0) {
                validatedOrderBy.append(", ");
            }
            validatedOrderBy.append("c.").append(column).append(sortOrder);
        }
        
        return validatedOrderBy.length() > 0 ? validatedOrderBy.toString() : "c.lastName, c.firstName";
    }
}
