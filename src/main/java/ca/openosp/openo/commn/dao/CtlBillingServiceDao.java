//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlBillingService;

public interface CtlBillingServiceDao {
    public static final String DEFAULT_STATUS = "A";

    public List<CtlBillingService> findAll();

    /**
     * Gets distinct service type for services with the specific service status
     *
     * @param serviceStatus Status of the service to be retrieved
     * @return Returns list containing arrays of strings, where the first element represents the service type and the second element is the service type name.
     */
    public List<Object[]> getUniqueServiceTypes(String serviceStatus);

    /**
     * Gets distinct service type for services with {@link #DEFAULT_STATUS}
     *
     * @return Returns list containing arrays of strings, where the first element represents the service type code and the second element is the service type name.
     */
    public List<Object[]> getUniqueServiceTypes();

    public List<CtlBillingService> findByServiceTypeId(String serviceTypeId);

    public List<CtlBillingService> findByServiceGroupAndServiceTypeId(String serviceGroup, String serviceTypeId);

    public List<CtlBillingService> findByServiceType(String serviceTypeId);

    public List<CtlBillingService> getServiceTypeList();

    /**
     * Gets all service type names and service types from the {@link CtlBillingService} instances.
     */

    public List<Object[]> getAllServiceTypes();

    /**
     * Gets all {@link CtlBillingService} instance with the specified service group
     *
     * @param serviceGroup Service group to ge services for
     * @return Returns all persistent services found
     */
    public List<CtlBillingService> findByServiceGroup(String serviceGroup);

    /**
     * Gets all {@link CtlBillingService} instance with the specified service group
     *
     * @param serviceGroup Service group to ge services for
     * @return Returns all persistent services found
     */

    public List<CtlBillingService> findByServiceGroupAndServiceType(String serviceGroup, String serviceType);


    public List<Object[]> findUniqueServiceTypesByCode(String serviceCode);

    public List<Object[]> findServiceTypes();

    public List<Object[]> findServiceTypesByStatus(String status);

    public List<Object> findServiceCodesByType(String serviceType);

    void remove(Integer id);

    void persist(CtlBillingService ctlBillingService);
}
 
