//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DxAssociation;

public interface DxDao extends AbstractDao<DxAssociation> {

    List<DxAssociation> findAllAssociations();

    int removeAssociations();

    DxAssociation findAssociation(String codeType, String code);

    List<Object[]> findCodingSystemDescription(String codingSystem, String code);

    List<Object[]> findCodingSystemDescription(String codingSystem, String[] keywords);

    String getCodeDescription(String codingSystem, String code);
}
