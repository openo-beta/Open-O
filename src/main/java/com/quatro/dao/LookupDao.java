//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 * Contributors:
 * <Quatro Group Software Systems inc.>  <OSCAR Team>
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package com.quatro.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.common.model.Facility;
import org.oscarehr.util.DbConnectionFilter;
import org.oscarehr.util.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import oscar.MyDateFormat;
import oscar.OscarProperties;
import oscar.oscarDB.DBPreparedHandler;
import oscar.oscarDB.DBPreparedHandlerParam;

import com.quatro.common.KeyConstants;
import com.quatro.model.FieldDefValue;
import com.quatro.model.LookupCodeValue;
import com.quatro.model.LookupTableDefValue;
import com.quatro.model.LstOrgcd;
import com.quatro.model.security.SecProvider;
import com.quatro.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

public interface LookupDao {

    List LoadCodeList(String tableId, boolean activeOnly, String code, String codeDesc);

    LookupCodeValue GetCode(String tableId, String code);

    List LoadCodeList(String tableId, boolean activeOnly, String parentCode, String code, String codeDesc);

    LookupTableDefValue GetLookupTableDef(String tableId);

    List LoadFieldDefList(String tableId);

    List GetCodeFieldValues(LookupTableDefValue tableDef, String code);

    List<List> GetCodeFieldValues(LookupTableDefValue tableDef);

    String SaveCodeValue(boolean isNew, LookupTableDefValue tableDef, List fieldDefList) throws SQLException;

    String SaveCodeValue(boolean isNew, LookupCodeValue codeValue) throws SQLException;

    void SaveAsOrgCode(Program program) throws SQLException;

    boolean inOrg(String org1, String org2);

    void SaveAsOrgCode(Facility facility) throws SQLException;

    void SaveAsOrgCode(LookupCodeValue orgVal, String tableId) throws SQLException;

    void runProcedure(String procName, String[] params) throws SQLException;

    int getCountOfActiveClient(String orgCd) throws SQLException;

    void setProviderDao(ProviderDao providerDao);

}
