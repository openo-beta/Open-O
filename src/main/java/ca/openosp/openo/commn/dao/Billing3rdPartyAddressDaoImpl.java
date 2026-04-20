//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import ca.openosp.openo.billing.CA.ON.model.Billing3rdPartyAddress;
import ca.openosp.openo.commn.NativeSql;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class Billing3rdPartyAddressDaoImpl extends AbstractDaoImpl<Billing3rdPartyAddress> implements Billing3rdPartyAddressDao {

    public Billing3rdPartyAddressDaoImpl() {
        super(Billing3rdPartyAddress.class);
    }

    public List<Billing3rdPartyAddress> findAll() {
        Query q = entityManager.createQuery("select b from Billing3rdPartyAddress b");
        List<Billing3rdPartyAddress> results = q.getResultList();
        return results;
    }

    public List<Billing3rdPartyAddress> findByCompanyName(String companyName) {
        Query q = entityManager.createQuery("select b from Billing3rdPartyAddress b where b.companyName = ?1");
        q.setParameter(1, companyName);
        List<Billing3rdPartyAddress> results = q.getResultList();
        return results;
    }

    @NativeSql("billing_on_3rdPartyAddress")
    public List<Billing3rdPartyAddress> findAddresses(String searchModeParam, String orderByParam, String keyword, String limit1, String limit2) {
        String search_mode = searchModeParam == null ? "search_name" : searchModeParam;
        String orderBy = orderByParam == null ? "company_name" : orderByParam;
        String where = "";
        Map<String, Object> params = new HashMap<String, Object>();
        if ("search_name".equals(search_mode)) {
            if (keyword == null) {
                keyword = "";
            }

            String[] temp = keyword.split("\\,\\p{Space}*");
            if (temp.length > 1) {
                where = "company_name like :compName0 and company_name like :compName1";
                params.put("compName0", temp[0] + "%");
                params.put("compName1", temp[1] + "%");
            } else {
                where = "company_name like :compName0";
                params.put("compName0", temp[0] + "%");
            }
        } else {
            where = search_mode + " like :searchMode";
            params.put("searchMode", keyword + "%");
        }

        String strLimit1 = "0";
        String strLimit2 = "20";
        if (limit1 != null)
            strLimit1 = limit1;
        if (limit2 != null)
            strLimit2 = limit2;
        String sql = "select * from billing_on_3rdPartyAddress where " + where + " order by " + orderBy + " limit "
                + strLimit1 + "," + strLimit2;

        try {
            Query q = entityManager.createNativeQuery(sql, modelClass);
            for (Entry<String, Object> o : params.entrySet()) {
                q.setParameter(o.getKey(), o.getValue());
            }
            return q.getResultList();
        } catch (Exception e) {
            MiscUtils.getLogger().error("error", e);
            return new ArrayList<Billing3rdPartyAddress>();
        }
    }
}
