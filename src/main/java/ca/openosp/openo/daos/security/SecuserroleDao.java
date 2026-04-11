//CHECKSTYLE:OFF


package ca.openosp.openo.daos.security;

import java.util.List;

import ca.openosp.openo.PMmodule.web.formbean.StaffForm;

import ca.openosp.openo.model.security.Secuserrole;

/**
 * A data access object (DAO) providing persistence and search support for
 * Secuserrole entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 *
 * @author MyEclipse Persistence Tools
 * @see Secuserrole
 */

public interface SecuserroleDao {
    public static final String PROVIDER_NO = "providerNo";
    public static final String ROLE_NAME = "roleName";
    public static final String ORGCD = "orgcd";
    public static final String ACTIVEYN = "activeyn";

    public void saveAll(List list);

    public void save(Secuserrole transientInstance);

    public void updateRoleName(Integer id, String roleName);

    public void delete(Secuserrole persistentInstance);

    public int deleteByOrgcd(String orgcd);

    public int deleteByProviderNo(String providerNo);

    public int deleteById(Integer id);

    public int update(Secuserrole instance);

    public Secuserrole findById(java.lang.Integer id);

    public List findByExample(Secuserrole instance);

    public List findByProperty(String propertyName, Object value);

    public List findByProviderNo(Object providerNo);

    public List findByRoleName(Object roleName);

    public List findByOrgcd(Object orgcd, boolean activeOnly);

    public List searchByCriteria(StaffForm staffForm);

    public List findByActiveyn(Object activeyn);

    public List findAll();

    public Secuserrole merge(Secuserrole detachedInstance);

    public void attachDirty(Secuserrole instance);

    public void attachClean(Secuserrole instance);
}
