//CHECKSTYLE:OFF


package ca.openosp.openo.billing.CA.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.billing.CA.model.BillActivity;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class BillActivityDaoImpl extends AbstractDaoImpl<BillActivity> implements BillActivityDao {

    public BillActivityDaoImpl() {
        super(BillActivity.class);
    }

    @Override
    public List<BillActivity> findCurrentByMonthCodeAndGroupNo(String monthCode, String groupNo, Date updateDateTime) {
        Query q = entityManager.createQuery(
                "SELECT b FROM BillActivity b WHERE b.monthCode=?1 AND b.groupNo=?2 AND b.updateDateTime > ?3 AND b.status != 'D' ORDER BY b.batchCount");
        q.setParameter(1, monthCode);
        q.setParameter(2, groupNo);
        q.setParameter(3, updateDateTime);

        @SuppressWarnings("unchecked")
        List<BillActivity> results = q.getResultList();

        return results;
    }

    @Override
    public List<BillActivity> findCurrentByDateRange(Date startDate, Date endDate) {
        Query q = entityManager.createQuery(
                "SELECT b FROM BillActivity b WHERE b.updateDateTime >= ?1 AND  b.updateDateTime <= ?2 AND b.status != 'D' ORDER BY b.id DESC");
        q.setParameter(1, startDate);
        q.setParameter(2, endDate);

        @SuppressWarnings("unchecked")
        List<BillActivity> results = q.getResultList();

        return results;
    }
}
