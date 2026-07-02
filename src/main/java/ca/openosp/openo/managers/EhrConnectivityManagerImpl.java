/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.managers;

import ca.openosp.openo.commn.dao.OMDGatewayTransactionLogDao;
import ca.openosp.openo.commn.dao.SecurityDao;
import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.OMDGatewayTransactionLog;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.integration.oneId.OneIdSession;
import ca.openosp.openo.integration.oneId.OneIdSessionDao;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Default {@code EhrConnectivityManager} backed by {@code SystemPreferencesDao} and
 * {@code OMDGatewayTransactionLogDao}. Blank or whitespace-only configuration values are treated as
 * absent for the default-returning reads.
 *
 * @since 2026-07-01
 */
@Service
public class EhrConnectivityManagerImpl implements EhrConnectivityManager {

    @Autowired
    private SystemPreferencesDao systemPreferencesDao;

    @Autowired
    private OMDGatewayTransactionLogDao transactionLogDao;

    @Autowired
    private SecurityDao securityDao;

    @Autowired
    private OneIdSessionDao oneIdSessionDao;

    @Autowired
    private SecurityInfoManager securityInfoManager;

    @Override
    public SystemPreferences getConfig(Enum<?> key) {
        return systemPreferencesDao.findPreferenceByName(key);
    }

    @Override
    public String getConfigValue(Enum<?> key) {
        SystemPreferences pref = getConfig(key);
        return pref == null ? null : pref.getValue();
    }

    @Override
    public String getConfigValue(Enum<?> key, String defaultValue) {
        String value = getConfigValue(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    @Override
    public boolean getConfigFlag(Enum<?> key, boolean defaultValue) {
        String value = getConfigValue(key);
        return (value != null && !value.trim().isEmpty()) ? "true".equals(value.trim()) : defaultValue;
    }

    @Override
    public SystemPreferences saveConfig(LoggedInInfo loggedInInfo, Enum<?> key, String value) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
        SystemPreferences pref = systemPreferencesDao.findPreferenceByName(key);
        if (pref == null) {
            pref = new SystemPreferences(key.name(), value);
            systemPreferencesDao.persist(pref);
        } else {
            pref.setValue(value);
            pref.setUpdateDate(new Date());
            systemPreferencesDao.merge(pref);
        }
        return pref;
    }

    @Override
    public List<OMDGatewayTransactionLog> getRecentLogs(LoggedInInfo loggedInInfo, String providerNo, String externalSystem, int maxRows) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
        List<OMDGatewayTransactionLog> logs;
        if (providerNo != null) {
            logs = transactionLogDao.findByProviderNo(providerNo);
        } else if (externalSystem != null) {
            logs = transactionLogDao.findByExternalSystem(externalSystem);
        } else {
            logs = transactionLogDao.getAll();
        }
        if (logs.size() > maxRows) {
            logs = logs.subList(0, maxRows);
        }
        return logs;
    }

    @Override
    public List<OMDGatewayTransactionLog> findLogsByProviderNo(LoggedInInfo loggedInInfo, String providerNo) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
        return transactionLogDao.findByProviderNo(providerNo);
    }

    @Override
    public List<OMDGatewayTransactionLog> findLogsByExternalSystem(LoggedInInfo loggedInInfo, String externalSystem) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
        return transactionLogDao.findByExternalSystem(externalSystem);
    }

    @Override
    public List<Security> findProvidersByOneId(String subject) {
        return securityDao.findByOneIdKey(subject);
    }

    @Override
    public void saveOneIdSession(OneIdSession oneIdSession) {
        if (oneIdSessionDao.find(oneIdSession.getProviderNo()) == null) {
            oneIdSessionDao.persist(oneIdSession);
        } else {
            oneIdSessionDao.merge(oneIdSession);
        }
    }

    @Override
    public void removeOneIdSession(String providerNo) {
        OneIdSession existing = oneIdSessionDao.find(providerNo);
        if (existing != null) {
            oneIdSessionDao.remove(existing);
        }
    }

    private void checkPrivilege(LoggedInInfo loggedInInfo, String privilege) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin.ehrConnectivity", privilege, null)) {
            throw new RuntimeException("missing required sec object (_admin.ehrConnectivity)");
        }
    }
}
