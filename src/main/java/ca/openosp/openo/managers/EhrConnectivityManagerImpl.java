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
import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.OMDGatewayTransactionLog;
import ca.openosp.openo.commn.model.SystemPreferences;
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
    public SystemPreferences saveConfig(Enum<?> key, String value) {
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
    public List<OMDGatewayTransactionLog> getRecentLogs(String providerNo, String externalSystem, int maxRows) {
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
    public List<OMDGatewayTransactionLog> findLogsByProviderNo(String providerNo) {
        return transactionLogDao.findByProviderNo(providerNo);
    }

    @Override
    public List<OMDGatewayTransactionLog> findLogsByExternalSystem(String externalSystem) {
        return transactionLogDao.findByExternalSystem(externalSystem);
    }
}
