//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.DataExport;

import org.springframework.stereotype.Repository;

@Repository
public class DataExportDaoImpl extends AbstractDaoImpl<DataExport> implements DataExportDao {

    public DataExportDaoImpl() {
        super(DataExport.class);
    }

    public List<DataExport> findAll() {
        Query query = entityManager.createQuery("select de from DataExport de order by de.daterun");
        @SuppressWarnings("unchecked")
        List<DataExport> list = query.getResultList();
        return list;
    }

    public List<DataExport> findAllByType(String type) {
        Query query = entityManager.createQuery("select de from DataExport de where de.type = ?1 order by de.daterun");
        query = query.setParameter(1, type);
        @SuppressWarnings("unchecked")
        List<DataExport> list = query.getResultList();
        return list;
    }

}
