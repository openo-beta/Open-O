//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DataExport;

public interface DataExportDao extends AbstractDao<DataExport> {

    public static final String ROURKE = "Rourke";
    public static final String CIHI_OMD4 = "CIHI_OMD4";
    public static final String CIHI_PHC_VRS = "CIHI_PHC_VRS";

    public List<DataExport> findAll();

    public List<DataExport> findAllByType(String type);

}
