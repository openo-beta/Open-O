//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AbstractCodeSystemModel;
import ca.openosp.openo.commn.model.Ichppccode;

public interface IchppccodeDao extends AbstractDao<Ichppccode> {
    List<Ichppccode> findAll();

    List<Ichppccode> getIchppccodeCode(String term);

    List<Ichppccode> getIchppccode(String query);

    List<Ichppccode> searchCode(String term);

    Ichppccode findByCode(String code);

    AbstractCodeSystemModel<?> findByCodingSystem(String codingSystem);

    List<Ichppccode> search_research_code(String code, String code1, String code2, String desc, String desc1, String desc2);
}
