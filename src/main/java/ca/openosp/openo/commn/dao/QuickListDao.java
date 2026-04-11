//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.QuickList;

public interface QuickListDao extends AbstractDao<QuickList> {
    List<QuickList> findAll();

    List<Object> findDistinct();

    List<QuickList> findByNameResearchCodeAndCodingSystem(String quickListName, String researchCode, String codingSystem);

    List<QuickList> findByCodingSystem(String codingSystem);

    List<Object[]> findResearchCodeAndCodingSystemDescriptionByCodingSystem(String codingSystem, String quickListName);
}