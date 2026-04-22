//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.commn.model.ProviderDefaultProgram;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderDefaultProgramDaoImpl extends AbstractDaoImpl<ProviderDefaultProgram>
        implements ProviderDefaultProgramDao {

    public ProviderDefaultProgramDaoImpl() {
        super(ProviderDefaultProgram.class);
    }

    @Override
    public List<ProviderDefaultProgram> getProgramByProviderNo(String providerNo) {
        String q = "SELECT pdp FROM ProviderDefaultProgram pdp WHERE pdp.providerNo=?1";
        Query query = entityManager.createQuery(q);
        query.setParameter(1, providerNo);
        @SuppressWarnings("unchecked")
        List<ProviderDefaultProgram> results = query.getResultList();
        return results;
    }

    @Override
    public void setDefaultProgram(String providerNo, int programId) {
        List<ProviderDefaultProgram> rs = getProgramByProviderNo(providerNo);
        ProviderDefaultProgram pdp = null;
        if (rs.size() == 0) {
            pdp = new ProviderDefaultProgram();
            pdp.setProviderNo(providerNo);
            pdp.setSign(false);
            pdp.setProgramId(programId);
            merge(pdp);
        } else {
            pdp = rs.get(0);
            pdp.setProgramId(programId);
            persist(pdp);
        }
    }

    @Override
    public List<ProviderDefaultProgram> getProviderSig(String providerNo) {
        List<ProviderDefaultProgram> rs = getProgramByProviderNo(providerNo);
        return rs;
    }

    @Override
    public void saveProviderDefaultProgram(ProviderDefaultProgram pdp) {
        if (pdp.getId() == null || pdp.getId().intValue() == 0) {
            persist(pdp);
        } else {
            merge(pdp);
        }
    }

    @Override
    public void toggleSig(String providerNo) {
        List<ProviderDefaultProgram> rs = this.getProgramByProviderNo(providerNo);
        for (ProviderDefaultProgram pdp : rs) {
            pdp.setSign(!pdp.isSign());
            merge(pdp);
        }
    }

    @Override
    public List<Program> findProgramsByProvider(String providerNo) {
        String sql = "FROM Program p WHERE p.id IN (SELECT pdp.programId FROM ProviderDefaultProgram pdp WHERE pdp.providerNo = ?1)";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<Program> results = query.getResultList();
        return results;

    }

    @Override
    public List<Program> findProgramsByFacilityId(Integer facilityId) {
        String sql = "from Program p where p.id in (select distinct pg.id from Program pg ,ProgramProvider pp where pp.ProgramId=pg.id and pg.facilityId=?1)";
        Query query;
        try {
            query = entityManager.createQuery(sql);
            query.setParameter(1, facilityId);
        } catch (Exception e) {
            MiscUtils.getLogger().error(e.getMessage(), e);
            return new ArrayList<Program>();
        }

        @SuppressWarnings("unchecked")
        List<Program> results = query.getResultList();
        return results;
    }
}
