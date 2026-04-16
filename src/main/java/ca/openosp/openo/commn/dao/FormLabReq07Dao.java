//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.FormLabReq07;

public interface FormLabReq07Dao extends AbstractDao<FormLabReq07> {
    List<FormLabReq07> findCreatinine();

    List<FormLabReq07> findAcr();
}
