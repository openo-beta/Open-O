//CHECKSTYLE:OFF


package ca.openosp.openo.daos.security;

import java.util.List;

import ca.openosp.openo.model.security.SecProvider;

/**
 * @author JZhang
 */

public interface SecProviderDao {

    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String PROVIDER_TYPE = "providerType";
    public static final String SPECIALTY = "specialty";
    public static final String TEAM = "team";
    public static final String SEX = "sex";
    public static final String ADDRESS = "address";
    public static final String PHONE = "phone";
    public static final String WORK_PHONE = "workPhone";
    public static final String OHIP_NO = "ohipNo";
    public static final String RMA_NO = "rmaNo";
    public static final String BILLING_NO = "billingNo";
    public static final String HSO_NO = "hsoNo";
    public static final String STATUS = "status";
    public static final String COMMENTS = "comments";
    public static final String PROVIDER_ACTIVITY = "providerActivity";

    public void save(SecProvider transientInstance);

    public void saveOrUpdate(SecProvider transientInstance);

    public void delete(SecProvider persistentInstance);

    public SecProvider findById(java.lang.String id);

    public SecProvider findById(java.lang.String id, String status);

    public List findByExample(SecProviderDao instance);

    public List findByProperty(String propertyName, Object value);

    public List findByLastName(Object lastName);

    public List findByFirstName(Object firstName);

    public List findByProviderType(Object providerType);

    public List findBySpecialty(Object specialty);

    public List findByTeam(Object team);

    public List findBySex(Object sex);

    public List findByAddress(Object address);

    public List findByPhone(Object phone);

    public List findByWorkPhone(Object workPhone);

    public List findByOhipNo(Object ohipNo);

    public List findByRmaNo(Object rmaNo);

    public List findByBillingNo(Object billingNo);

    public List findByHsoNo(Object hsoNo);

    public List findByStatus(Object status);

    public List findByComments(Object comments);

    public List findByProviderActivity(Object providerActivity);

    public List findAll();

    public SecProviderDao merge(SecProviderDao detachedInstance);

    public void attachDirty(SecProviderDao instance);

    public void attachClean(SecProviderDao instance);
}
