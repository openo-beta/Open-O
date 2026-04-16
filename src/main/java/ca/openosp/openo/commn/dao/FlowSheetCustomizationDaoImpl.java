//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.FlowSheetCustomization;
import org.springframework.stereotype.Repository;

@Repository
public class FlowSheetCustomizationDaoImpl extends AbstractDaoImpl<FlowSheetCustomization> implements FlowSheetCustomizationDao {

    // Static JPQL for findHigherLevelCustomization
    private static final String FIND_HIGHER_LEVEL_FOR_PATIENT =
        "SELECT fd FROM FlowSheetCustomization fd " +
        "WHERE fd.flowsheet=?1 AND fd.measurement=?2 AND fd.action=?3 AND fd.archived=0 " +
        "AND ((fd.providerNo='' AND fd.demographicNo='0') " +
        "OR (fd.providerNo=?4 AND fd.demographicNo='0')) " +
        "ORDER BY fd.providerNo ASC";

    private static final String FIND_HIGHER_LEVEL_FOR_PROVIDER =
        "SELECT fd FROM FlowSheetCustomization fd " +
        "WHERE fd.flowsheet=?1 AND fd.measurement=?2 AND fd.action=?3 AND fd.archived=0 " +
        "AND (fd.providerNo='' AND fd.demographicNo='0') " +
        "ORDER BY fd.providerNo ASC";

    public FlowSheetCustomizationDaoImpl() {
        super(FlowSheetCustomization.class);
    }

    /**
     * Helper to execute scope-specific queries with consistent structure.
     */
    private List<FlowSheetCustomization> getCustomizationsByScope(
            String flowsheet, String whereClause, Consumer<Query> paramBinder) {
        String jpql = "SELECT fd FROM FlowSheetCustomization fd " +
                      "WHERE fd.flowsheet=?1 AND fd.archived=0 " + whereClause;
        Query query = entityManager.createQuery(jpql);
        query.setParameter(1, flowsheet);
        paramBinder.accept(query);

        @SuppressWarnings("unchecked")
        List<FlowSheetCustomization> list = query.getResultList();
        return list;
    }

    @Override
    public FlowSheetCustomization getFlowSheetCustomization(Integer id) {
        return this.find(id);
    }

    @Override
    public List<FlowSheetCustomization> getFlowSheetCustomizations(String flowsheet, String provider, Integer demographic) {
        // Returns customizations across all scope levels for this flowsheet.
        Query query = entityManager.createQuery(
            "SELECT fd FROM FlowSheetCustomization fd WHERE fd.flowsheet=?1 AND fd.archived=0 " +
            "AND ((fd.providerNo='' AND fd.demographicNo='0') " +
            "OR (fd.providerNo=?2 AND fd.demographicNo='0') " +
            "OR (fd.demographicNo=?3)) " +
            "ORDER BY fd.demographicNo ASC, fd.providerNo ASC");
        query.setParameter(1, flowsheet);
        query.setParameter(2, provider);
        query.setParameter(3, String.valueOf(demographic));

        @SuppressWarnings("unchecked")
        List<FlowSheetCustomization> list = query.getResultList();
        return list;
    }

    @Override
    public List<FlowSheetCustomization> getFlowSheetCustomizations(String flowsheet, String provider) {
        // Include both clinic-level (providerNo='') and provider-level customizations
        Query query = entityManager.createQuery("SELECT fd FROM FlowSheetCustomization fd WHERE fd.flowsheet=?1 and fd.archived=0 and (fd.providerNo = '' or (fd.providerNo = ?2 and fd.demographicNo = 0)) order by fd.providerNo, fd.demographicNo");
        query.setParameter(1, flowsheet);
        query.setParameter(2, provider);

        @SuppressWarnings("unchecked")
        List<FlowSheetCustomization> list = query.getResultList();
        return list;
    }

    @Override
    public List<FlowSheetCustomization> getFlowSheetCustomizations(String flowsheet) {
        Query query = entityManager.createQuery("SELECT fd FROM FlowSheetCustomization fd WHERE fd.flowsheet=?1 and fd.archived=0 and fd.providerNo = ''  and fd.demographicNo = 0");
        query.setParameter(1, flowsheet);

        @SuppressWarnings("unchecked")
        List<FlowSheetCustomization> list = query.getResultList();
        return list;
    }

    @Override
    public List<FlowSheetCustomization> getClinicLevelCustomizations(String flowsheet) {
        // Clinic level: providerNo='' AND demographicNo='0'
        return getCustomizationsByScope(flowsheet,
            "AND fd.providerNo='' AND fd.demographicNo='0'",
            q -> { });
    }

    @Override
    public List<FlowSheetCustomization> getProviderLevelCustomizations(String flowsheet, String providerNo) {
        // Provider level: specific providerNo AND demographicNo='0'
        return getCustomizationsByScope(flowsheet,
            "AND fd.providerNo=?2 AND fd.demographicNo='0'",
            q -> q.setParameter(2, providerNo));
    }

    @Override
    public List<FlowSheetCustomization> getPatientLevelCustomizations(String flowsheet, String demographicNo) {
        // Patient level: specific demographicNo (not "0")
        return getCustomizationsByScope(flowsheet,
            "AND fd.demographicNo=?2",
            q -> q.setParameter(2, demographicNo));
    }

    @Override
    public FlowSheetCustomization findHigherLevelCustomization(
            String flowsheet, String measurement, String action,
            String providerNo, String demographicNo) {

        boolean isPatientScope = demographicNo != null && !"0".equals(demographicNo);
        boolean isProviderScope = !isPatientScope && providerNo != null && !providerNo.isEmpty();

        // Clinic scope: nothing is higher
        if (!isPatientScope && !isProviderScope) {
            return null;
        }

        String jpql = isPatientScope ? FIND_HIGHER_LEVEL_FOR_PATIENT : FIND_HIGHER_LEVEL_FOR_PROVIDER;

        Query query = entityManager.createQuery(jpql);
        query.setParameter(1, flowsheet);
        query.setParameter(2, measurement);
        query.setParameter(3, action);
        if (isPatientScope) {
            query.setParameter(4, providerNo);
        }
        query.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<FlowSheetCustomization> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
