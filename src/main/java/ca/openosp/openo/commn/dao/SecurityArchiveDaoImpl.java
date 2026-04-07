//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.commn.model.SecurityArchive;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityArchiveDaoImpl extends AbstractDaoImpl<SecurityArchive> implements SecurityArchiveDao {

    public SecurityArchiveDaoImpl() {
        super(SecurityArchive.class);
    }

    @Override
    public List<SecurityArchive> findBySecurityNo(Integer securityNo) {

        String sqlCommand = "select x from SecurityArchive x where x.securityNo=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, securityNo);

        @SuppressWarnings("unchecked")
        List<SecurityArchive> results = query.getResultList();

        return results;
    }

    @Override
    public List<String> findPreviousPasswordsByProviderNo(String providerNo, int maxResult) {

        String sqlCommand = "select distinct x.password from SecurityArchive x where x.providerNo=?1 order by x.passwordUpdateDate DESC";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, providerNo);
        query.setMaxResults(maxResult);

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        return results;
    }

    @Override
    public Integer archiveRecord(Security s) {
        SecurityArchive sa = new SecurityArchive(s);
        persist(sa);
        return sa.getId();
    }

}
