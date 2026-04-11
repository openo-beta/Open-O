//CHECKSTYLE:OFF

package ca.openosp.openo.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.openosp.openo.dao.OscarSuperDao;

public interface OscarSuperManager {
    void setProviderSuperDao(OscarSuperDao providerDao);

    void init();

    List<Map<String, Object>> find(String daoName, String queryName, Object[] params);

    List<Object> populate(String daoName, String queryName, Object[] params);
}
