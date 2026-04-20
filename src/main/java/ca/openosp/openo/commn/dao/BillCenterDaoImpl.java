//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.BillCenter;
import org.springframework.stereotype.Repository;

@Repository
public class BillCenterDaoImpl extends AbstractDaoImpl<BillCenter> implements BillCenterDao {

    public BillCenterDaoImpl() {
        super(BillCenter.class);
    }

    public List<BillCenter> findAll() {
        Query query = entityManager.createQuery("SELECT b FROM BillCenter b");

        @SuppressWarnings("unchecked")
        List<BillCenter> results = query.getResultList();
        return results;
    }

    public List<BillCenter> findByBillCenterDesc(String descr) {
        Query query = entityManager.createQuery("SELECT b FROM BillCenter b WHERE b.billCenterDesc like ?1");
        query.setParameter(1, descr);
        @SuppressWarnings("unchecked")
        List<BillCenter> results = query.getResultList();
        return results;
    }
}
