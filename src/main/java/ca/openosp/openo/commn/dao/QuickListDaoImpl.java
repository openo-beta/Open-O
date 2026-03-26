//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.NativeSql;
import ca.openosp.openo.commn.model.QuickList;
import ca.openosp.openo.utility.MiscUtils;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

@Repository
public class QuickListDaoImpl extends AbstractDaoImpl<QuickList> implements QuickListDao {
    private static final Logger logger = MiscUtils.getLogger();

    public QuickListDaoImpl() {
        super(QuickList.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<QuickList> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> findDistinct() {
        Query query = entityManager.createQuery("select distinct ql.quickListName from QuickList ql");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<QuickList> findByNameResearchCodeAndCodingSystem(String quickListName, String researchCode, String codingSystem) {
        Query query = entityManager.createQuery("from QuickList q where q.quickListName = ?1 AND q.dxResearchCode = ?2 AND q.codingSystem = ?3");
        query.setParameter(1, quickListName);
        query.setParameter(2, researchCode);
        query.setParameter(3, codingSystem);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<QuickList> findByCodingSystem(String codingSystem) {
        String csQuery = "";
        if (codingSystem != null) {
            csQuery = " WHERE ql.codingSystem = ?1";
        }
        Query query = entityManager.createQuery("select ql from QuickList ql " + csQuery + " GROUP BY ql.quickListName");
        if (codingSystem != null) {
            query.setParameter(1, codingSystem);
        }
        return query.getResultList();
    }

    @NativeSql
    @SuppressWarnings("unchecked")
    @Override
    public List<Object[]> findResearchCodeAndCodingSystemDescriptionByCodingSystem(String codingSystem, String quickListName) {
        try {
            // Validate codingSystem to prevent SQL injection - only allow known valid table names
            if (codingSystem == null || codingSystem.isEmpty()) {
                logger.error("no coding system provided");
                return new ArrayList<Object[]>();
            }
            
            // Validate codingSystem against allowed values to prevent SQL injection
            // Using the same enum validation as in getDescription() method
            try {
                // This will throw IllegalArgumentException if codingSystem is not valid
                AbstractCodeSystemDaoImpl.codingSystem.valueOf(codingSystem);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid coding system provided: " + codingSystem);
                return new ArrayList<>();
            }
            
            // Build SQL with validated table name and parameterized values for user input
            String sql = "Select q.dxResearchCode, c.description FROM quickList q, " + codingSystem 
                       + " c where q.codingSystem = ?1"
                       + " and q.quickListName = ?2 AND c." + codingSystem
                       + " = q.dxResearchCode order by c.description";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, codingSystem);
            query.setParameter(2, quickListName);
            return query.getResultList();
        } catch (Exception e) {
            // TODO replace when test ignores are merged
            return new ArrayList<Object[]>();
        }

    }
}
