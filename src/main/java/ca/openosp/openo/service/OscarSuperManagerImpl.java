//CHECKSTYLE:OFF

package ca.openosp.openo.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.openosp.openo.dao.OscarSuperDao;

public class OscarSuperManagerImpl implements OscarSuperManager {

    private Map<String, OscarSuperDao> oscarDaoMap = new TreeMap<String, OscarSuperDao>();
    private OscarSuperDao providerSuperDao;

    @Override
    public void setProviderSuperDao(OscarSuperDao providerDao) {
        this.providerSuperDao = providerDao;
    }

    @Override
    public void init() {
        oscarDaoMap.put("providerDao", providerSuperDao);

        for (String daoName : oscarDaoMap.keySet()) {
            if (oscarDaoMap.get(daoName) == null) {
                throw new IllegalStateException(
                        "Dao with specified name has not been injected into OscarSuperManager: " + daoName);
            }
        }
    }

    @Override
    public List<Map<String, Object>> find(String daoName, String queryName, Object[] params) {
        return getDao(daoName).executeSelectQuery(queryName, params);
    }

    @Override
    public List<Object> populate(String daoName, String queryName, Object[] params) {
        return getDao(daoName).executeRowMappedSelectQuery(queryName, params);
    }

    protected OscarSuperDao getDao(String daoName) {
        OscarSuperDao oscarSuperDao = oscarDaoMap.get(daoName);
        if (oscarSuperDao != null) {
            return oscarSuperDao;
        }
        throw new IllegalArgumentException("OscarSuperManager contains no daos with specified name: " + daoName);
    }
}
