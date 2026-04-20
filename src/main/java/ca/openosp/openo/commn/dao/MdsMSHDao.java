//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.MdsMSH;

public interface MdsMSHDao extends AbstractDao<MdsMSH> {
    List<Object[]> findLabsByAccessionNumAndId(Integer id, String controlId);

    List<Object[]> findMdsSementDataById(Integer id);

    List<Integer> getLabResultsSince(Integer demographicNo, Date updateDate);
}
