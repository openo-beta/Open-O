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

package ca.openosp.openo.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ca.openosp.Misc;
import org.hibernate.Session;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.commn.model.Facility;
import ca.openosp.openo.utility.DbConnectionFilter;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ca.openosp.MyDateFormat;
import ca.openosp.OscarProperties;
import ca.openosp.openo.db.DBPreparedHandler;
import ca.openosp.openo.db.DBPreparedHandlerParam;

import ca.openosp.openo.commons.KeyConstants;
import ca.openosp.openo.model.FieldDefValue;
import ca.openosp.openo.model.LookupCodeValue;
import ca.openosp.openo.model.LookupTableDefValue;
import ca.openosp.openo.model.LstOrgcd;
import ca.openosp.openo.model.security.SecProvider;
import ca.openosp.openo.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

/**
 * Implementation of the LookupDao interface.
 *
 * This class provides the concrete implementation for managing lookup tables
 * and codes in the EMR system. It handles complex SQL generation for dynamic
 * lookup table structures, supporting both flat and hierarchical code organizations.
 *
 * The implementation features:
 * - Dynamic SQL generation based on field definitions
 * - Support for tree-structured hierarchical codes
 * - Generic column mapping using indexes (1-17)
 * - Organization code management with parent-child relationships
 * - Stored procedure execution for complex operations
 * - Integration with provider and facility management
 *
 * Column mapping convention:
 * - Index 1: Code
 * - Index 2: Description
 * - Index 3: Active flag
 * - Index 4: Display Order
 * - Index 5: Parent Code
 * - Index 6: Buf1
 * - Index 7: Code Tree
 * - Index 8: Last Update User
 * - Index 9: Last Update Date
 * - Index 10-16: Buf3-Buf9
 * - Index 17: Code CSV
 *
 * @since 2009-01-01
 */
public class LookupDaoImpl extends HibernateDaoSupport implements LookupDao {

    /**
     * Provider DAO for provider-related lookups.
     */
    private ProviderDao providerDao;

    /**
     * Hibernate session factory for database operations.
     */
    public SessionFactory sessionFactory;

    /**
     * Sets the Hibernate session factory.
     *
     * Spring autowired method to inject the session factory.
     *
     * @param sessionFactory SessionFactory Hibernate session factory
     */
    @Autowired
    public void setSessionFactoryOverride(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    /**
     * {@inheritDoc}
     *
     * Delegates to the overloaded method with empty parent code.
     */
    @Override
    public List LoadCodeList(String tableId, boolean activeOnly, String code, String codeDesc) {
        return LoadCodeList(tableId, activeOnly, "", code, codeDesc);
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves a single code by loading the code list and returning
     * the first match.
     */
    @Override
    public LookupCodeValue GetCode(String tableId, String code) {
        if (code == null || "".equals(code))
            return null;
        List lst = LoadCodeList(tableId, false, code, "");
        LookupCodeValue lkv = null;
        if (lst.size() > 0) {
            lkv = (LookupCodeValue) lst.get(0);
        }
        return lkv;
    }

    /**
     * {@inheritDoc}
     *
     * Loads lookup codes with dynamic SQL generation based on field definitions.
     * Handles both flat and hierarchical code structures.
     */
    @Override
    public List LoadCodeList(String tableId, boolean activeOnly, String parentCode, String code, String codeDesc) {
        String pCd = parentCode;
        // User table doesn't support parent codes
        if ("USR".equals(tableId))
            parentCode = null;
        LookupTableDefValue tableDef = GetLookupTableDef(tableId);
        if (tableDef == null)
            return (new ArrayList<LookupCodeValue>());
        List fields = LoadFieldDefList(tableId);
        // Prepare parameters and field names arrays
        DBPreparedHandlerParam[] params = new DBPreparedHandlerParam[100];
        String fieldNames[] = new String[17];
        String sSQL1 = "";
        String sSQL = "select distinct ";
        boolean activeFieldExists = true;

        // Build dynamic SQL based on field definitions
        for (int i = 1; i <= 17; i++) {
            boolean ok = false;
            for (int j = 0; j < fields.size(); j++) {
                FieldDefValue fdef = (FieldDefValue) fields.get(j);
                if (fdef.getGenericIdx() == i) {
                    if (fdef.getFieldSQL().indexOf('(') >= 0) {
                        sSQL += fdef.getFieldSQL() + " " + fdef.getFieldName() + ",";
                        fieldNames[i - 1] = fdef.getFieldName();
                    } else {
                        sSQL += "s." + fdef.getFieldSQL() + ",";
                        fieldNames[i - 1] = fdef.getFieldSQL();
                    }
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                if (i == 3) {
                    activeFieldExists = false;
                    sSQL += " 1 field" + i + ",";
                } else {
                    sSQL += " null field" + i + ",";
                }
                fieldNames[i - 1] = "field" + i;
            }
        }
        sSQL = sSQL.substring(0, sSQL.length() - 1);
        sSQL += " from " + tableDef.getTableName();
        sSQL1 = Misc.replace(sSQL, "s.", "a.") + " a,";
        sSQL += " s where 1=1";
        int i = 0;
        // Filter by active status if requested
        if (activeFieldExists && activeOnly) {
            sSQL += " and " + fieldNames[2] + "=?";
            params[i++] = new DBPreparedHandlerParam(1);
        }
        // Filter by parent code for hierarchical structures
        if (!Utility.IsEmpty(parentCode)) {
            sSQL += " and " + fieldNames[4] + "=?";
            params[i++] = new DBPreparedHandlerParam(parentCode);
        }
        // Filter by code value(s)
        if (!Utility.IsEmpty(code)) {
            // Organization table uses LIKE for hierarchical matching
            if (tableId.equals("ORG")) {
                sSQL += " and " + fieldNames[0] + " like ('%'||";
                String[] codes = code.split(",");
                sSQL += "?";
                params[i++] = new DBPreparedHandlerParam(codes[0]);
                for (int k = 1; k < codes.length; k++) {
                    sSQL += ",?";
                    params[i++] = new DBPreparedHandlerParam(codes[k]);
                }
                sSQL += ")";
            } else {
                sSQL += " and " + fieldNames[0] + " in (";
                String[] codes = code.split(",");
                sSQL += "?";
                params[i++] = new DBPreparedHandlerParam(codes[0]);
                for (int k = 1; k < codes.length; k++) {
                    if (codes[k].equals(""))
                        continue;
                    sSQL += ",?";
                    params[i++] = new DBPreparedHandlerParam(codes[k]);
                }
                sSQL += ")";
            }
        }
        // Filter by description (case-insensitive partial match)
        if (!Utility.IsEmpty(codeDesc)) {
            sSQL += " and upper(" + fieldNames[1] + ") like ?";
            params[i++] = new DBPreparedHandlerParam("%" + codeDesc.toUpperCase() + "%");
        }

        // Handle tree-structured tables with hierarchical relationships
        if (tableDef.isTree()) {
            sSQL = sSQL1 + "(" + sSQL + ") b";
            sSQL += " where b." + fieldNames[6] + " like a." + fieldNames[6] + "||'%'";
        }
        // if (tableDef.isTree())
        // {
        // sSQL += " order by 7,1";
        // } else {
        sSQL += " order by 4,2";
        // }
        DBPreparedHandlerParam[] pars = new DBPreparedHandlerParam[i];
        for (int j = 0; j < i; j++) {
            pars[j] = params[j];
        }

        DBPreparedHandler db = new DBPreparedHandler();
        ArrayList<LookupCodeValue> list = new ArrayList<LookupCodeValue>();

        try {
            ResultSet rs = db.queryResults(sSQL, pars);
            while (rs.next()) {
                LookupCodeValue lv = new LookupCodeValue();
                lv.setPrefix(tableId);
                lv.setCode(rs.getString(1));
                lv.setDescription(Misc.getString(rs, 2));
                lv.setActive(Integer.valueOf("0" + Misc.getString(rs, 3)).intValue() == 1);
                lv.setOrderByIndex(Integer.valueOf("0" + Misc.getString(rs, 4)).intValue());
                lv.setParentCode(Misc.getString(rs, 5));
                lv.setBuf1(Misc.getString(rs, 6));
                lv.setCodeTree(Misc.getString(rs, 7));
                lv.setLastUpdateUser(Misc.getString(rs, 8));
                lv.setLastUpdateDate(MyDateFormat.getCalendar(Misc.getString(rs, 9)));
                lv.setBuf3(Misc.getString(rs, 10));
                lv.setBuf4(Misc.getString(rs, 11));
                lv.setBuf5(Misc.getString(rs, 12));
                lv.setBuf6(Misc.getString(rs, 13));
                lv.setBuf7(Misc.getString(rs, 14));
                lv.setBuf8(Misc.getString(rs, 15));
                lv.setBuf9(Misc.getString(rs, 16));
                lv.setCodecsv(Misc.getString(rs, 17));
                list.add(lv);
            }
            rs.close();
            // filter by programId for user
            if ("USR".equals(tableId) && !Utility.IsEmpty(pCd)) {
                List userLst = providerDao.getActiveProviders(Integer.valueOf(pCd));
                ArrayList<LookupCodeValue> newLst = new ArrayList<LookupCodeValue>();
                for (int n = 0; n < userLst.size(); n++) {
                    SecProvider sp = (SecProvider) userLst.get(n);
                    for (int m = 0; m < list.size(); m++) {
                        LookupCodeValue lv = list.get(m);
                        if (lv.getCode().equals(sp.getProviderNo()))
                            newLst.add(lv);
                    }
                }
                list = newLst;
            }
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves the table definition for a given lookup table ID.
     * This method queries the system metadata to find the configuration
     * for dynamic lookup tables, including table name, hierarchy settings,
     * and other structural information.
     */
    @Override
    public LookupTableDefValue GetLookupTableDef(String tableId) {
        String sSQL = "from LookupTableDefValue s where s.tableId= ?0";
        try {
            return (LookupTableDefValue) getHibernateTemplate().find(sSQL, new Object[]{tableId}).get(0);
        } catch (Exception ex) {
            MiscUtils.getLogger().error("Error", ex);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Loads the field definitions for a specific lookup table.
     * Field definitions define the structure, data types, and mapping
     * of columns in dynamic lookup tables. The results are ordered
     * by field index for consistent column ordering.
     */
    @Override
    public List LoadFieldDefList(String tableId) {
        String sSql = "from FieldDefValue s where s.tableId=?0 order by s.fieldIndex ";
        ArrayList<String> paramList = new ArrayList<String>();
        paramList.add(tableId);
        Object params[] = paramList.toArray(new Object[paramList.size()]);

        return getHibernateTemplate().find(sSql, params);
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves field values for a specific code in a lookup table.
     * This method dynamically builds a SQL query based on the table's field
     * definitions and populates the field definition objects with actual values.
     * Date fields are formatted according to their editability settings,
     * and lookup fields are resolved to their descriptive values.
     */
    @Override
    public List GetCodeFieldValues(LookupTableDefValue tableDef, String code) {
        String tableName = tableDef.getTableName();
        List fs = LoadFieldDefList(tableDef.getTableId());
        String idFieldName = "";

        // Build dynamic SQL based on field definitions
        String sql = "select ";
        for (int i = 0; i < fs.size(); i++) {
            FieldDefValue fdv = (FieldDefValue) fs.get(i);
            if (fdv.getGenericIdx() == 1)
                idFieldName = fdv.getFieldSQL();
            if (i == 0) {
                sql += fdv.getFieldSQL();
            } else {
                sql += "," + fdv.getFieldSQL();
            }
        }
        sql += " from " + tableName + " s";
        sql += " where " + idFieldName + "='" + code + "'";
        DBPreparedHandler db = new DBPreparedHandler();
        try {
            ResultSet rs = db.queryResults(sql);
            if (rs.next()) {
                // Populate field values from result set
                for (int i = 0; i < fs.size(); i++) {
                    FieldDefValue fdv = (FieldDefValue) fs.get(i);
                    String val = Misc.getString(rs, (i + 1));
                    // Format date fields based on editability
                    if ("D".equals(fdv.getFieldType()))
                        if (fdv.isEditable()) {
                            val = MyDateFormat.getStandardDate(MyDateFormat.getCalendarwithTime(val));
                        } else {
                            val = MyDateFormat.getStandardDateTime(MyDateFormat.getCalendarwithTime(val));
                        }
                    fdv.setVal(val);
                }
            }
            rs.close();
            // Resolve lookup field descriptions
            for (int i = 0; i < fs.size(); i++) {
                FieldDefValue fdv = (FieldDefValue) fs.get(i);
                if (!Utility.IsEmpty(fdv.getLookupTable())) {
                    LookupCodeValue lkv = GetCode(fdv.getLookupTable(), fdv.getVal());
                    if (lkv != null)
                        fdv.setValDesc(lkv.getDescription());
                }
            }
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
        }
        return fs;
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves all field values for all codes in a lookup table.
     * This method returns a list of field definition lists, where each
     * inner list represents the field values for one code record.
     * All date fields are formatted with full date-time precision,
     * and lookup fields are resolved to their descriptions.
     */
    @Override
    public List<List> GetCodeFieldValues(LookupTableDefValue tableDef) {
        String tableName = tableDef.getTableName();
        List fs = LoadFieldDefList(tableDef.getTableId());
        ArrayList<List> codes = new ArrayList<List>();

        // Build SQL to select all fields
        String sql = "select ";
        for (int i = 0; i < fs.size(); i++) {
            FieldDefValue fdv = (FieldDefValue) fs.get(i);
            if (i == 0) {
                sql += fdv.getFieldSQL();
            } else {
                sql += "," + fdv.getFieldSQL();
            }
        }
        sql += " from " + tableName;
        DBPreparedHandler db = new DBPreparedHandler();
        try {
            ResultSet rs = db.queryResults(sql);
            while (rs.next()) {
                // Process each row and populate field values
                for (int i = 0; i < fs.size(); i++) {
                    FieldDefValue fdv = (FieldDefValue) fs.get(i);
                    String val = Misc.getString(rs, (i + 1));
                    // Format all date fields with full date-time
                    if ("D".equals(fdv.getFieldType()))
                        val = MyDateFormat.getStandardDateTime(MyDateFormat.getCalendarwithTime(val));
                    fdv.setVal(val);
                    // Resolve lookup descriptions
                    if (!Utility.IsEmpty(fdv.getLookupTable())) {
                        LookupCodeValue lkv = GetCode(fdv.getLookupTable(), val);
                        if (lkv != null)
                            fdv.setValDesc(lkv.getDescription());
                    }
                }
                codes.add(fs);
            }
            rs.close();
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
        }
        return codes;
    }

    /**
     * Generates the next sequential ID for auto-increment fields.
     *
     * This method queries the maximum value of the specified ID field
     * and returns the next sequential number. Used for tables that
     * manage their own sequence generation rather than relying on
     * database auto-increment features.
     *
     * @param idFieldName String the name of the ID field to query
     * @param tableName String the name of the table to query
     * @return int the next available ID value (max + 1)
     * @throws SQLException if database query fails
     */
    private int GetNextId(String idFieldName, String tableName) throws SQLException {
        String sql = "select max(" + idFieldName + ")";
        sql += " from " + tableName;
        DBPreparedHandler db = new DBPreparedHandler();

        ResultSet rs = db.queryResults(sql);
        int id = 0;
        if (rs.next())
            id = rs.getInt(1);
        return id + 1;
    }

    /**
     * {@inheritDoc}
     *
     * Saves a lookup code value using field definition list.
     * This method handles both insert and update operations, and includes
     * special processing for organization tables (OGN, SHL) and properties
     * table (PRP). Organization codes are automatically synchronized to
     * the hierarchical organization structure, while properties are
     * updated in the runtime configuration.
     */
    @Override
    public String SaveCodeValue(boolean isNew, LookupTableDefValue tableDef, List fieldDefList) throws SQLException {
        String id = "";
        if (isNew) {
            id = InsertCodeValue(tableDef, fieldDefList);
        } else {
            id = UpdateCodeValue(tableDef, fieldDefList);
        }
        String tableId = tableDef.getTableId();

        // Handle organization code synchronization
        if ("OGN,SHL".indexOf(tableId) >= 0) {
            SaveAsOrgCode(GetCode(tableId, id), tableId);
        }

        // Handle properties table updates
        if ("PRP".equals(tableId)) {
            OscarProperties prp = OscarProperties.getInstance();
            LookupCodeValue prpCd = GetCode(tableId, id);
            if (prp.getProperty(prpCd.getDescription()) != null)
                prp.remove(prpCd.getDescription());
            prp.setProperty(prpCd.getDescription(), prpCd.getBuf1().toLowerCase());
        }
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * Saves a lookup code value using LookupCodeValue object.
     * This method converts the code value object into field definitions
     * by mapping each property to its corresponding generic index position.
     * The mapping follows the standard column convention documented in
     * the class header. After conversion, delegates to the field-based
     * save method.
     */
    @Override
    public String SaveCodeValue(boolean isNew, LookupCodeValue codeValue) throws SQLException {
        String tableId = codeValue.getPrefix();
        LookupTableDefValue tableDef = GetLookupTableDef(tableId);
        List fieldDefList = this.LoadFieldDefList(tableId);

        // Map code value properties to field definitions based on generic index
        for (int i = 0; i < fieldDefList.size(); i++) {
            FieldDefValue fdv = (FieldDefValue) fieldDefList.get(i);

            switch (fdv.getGenericIdx()) {
                case 1: // Code
                    fdv.setVal(codeValue.getCode());
                    break;
                case 2: // Description
                    fdv.setVal(codeValue.getDescription());
                    break;
                case 3: // Active flag
                    fdv.setVal(codeValue.isActive() ? "1" : "0");
                    break;
                case 4: // Display Order
                    fdv.setVal(String.valueOf(codeValue.getOrderByIndex()));
                    break;
                case 5: // Parent Code
                    fdv.setVal(codeValue.getParentCode());
                    break;
                case 6: // Buf1
                    fdv.setVal(codeValue.getBuf1());
                    break;
                case 7: // Code Tree
                    fdv.setVal(codeValue.getCodeTree());
                    break;
                case 8: // Last Update User
                    fdv.setVal(codeValue.getLastUpdateUser());
                    break;
                case 9: // Last Update Date
                    fdv.setVal(MyDateFormat.getStandardDateTime(codeValue.getLastUpdateDate()));
                    break;
                case 10: // Buf3
                    fdv.setVal(codeValue.getBuf3());
                    break;
                case 11: // Buf4
                    fdv.setVal(codeValue.getBuf4());
                    break;
                case 12: // Buf5
                    fdv.setVal(codeValue.getBuf5());
                    break;
                case 13: // Buf6
                    fdv.setVal(codeValue.getBuf6());
                    break;
                case 14: // Buf7
                    fdv.setVal(codeValue.getBuf7());
                    break;
                case 15: // Buf8
                    fdv.setVal(codeValue.getBuf8());
                    break;
                case 16: // Buf9
                    fdv.setVal(codeValue.getBuf9());
                    break;
                case 17: // Code CSV
                    fdv.setVal(codeValue.getCodecsv());
                    break;
            }
        }
        if (isNew) {
            return InsertCodeValue(tableDef, fieldDefList);
        } else {
            return UpdateCodeValue(tableDef, fieldDefList);
        }
    }

    /**
     * Inserts a new code value record into the lookup table.
     *
     * This method dynamically constructs an INSERT SQL statement based on
     * the field definitions and handles auto-generated IDs, data type
     * conversions, and duplicate checking. The method ensures data integrity
     * by validating that the code doesn't already exist before insertion.
     *
     * @param tableDef LookupTableDefValue the table definition containing metadata
     * @param fieldDefList List<FieldDefValue> the field definitions with values to insert
     * @return String the ID of the inserted record
     * @throws SQLException if the code already exists or database operation fails
     */
    private String InsertCodeValue(LookupTableDefValue tableDef, List fieldDefList) throws SQLException {
        String tableName = tableDef.getTableName();
        String idFieldVal = "";

        DBPreparedHandlerParam[] params = new DBPreparedHandlerParam[fieldDefList.size()];
        String phs = "";
        String sql = "insert into  " + tableName + "(";

        // Build SQL and prepare parameters based on field definitions
        for (int i = 0; i < fieldDefList.size(); i++) {
            FieldDefValue fdv = (FieldDefValue) fieldDefList.get(i);
            sql += fdv.getFieldSQL() + ",";
            phs += "?,";

            // Handle ID field (generic index 1)
            if (fdv.getGenericIdx() == 1) {
                if (fdv.isAuto()) {
                    // Generate next ID for auto-increment fields
                    idFieldVal = String.valueOf(GetNextId(fdv.getFieldSQL(), tableName));
                    fdv.setVal(idFieldVal);
                } else {
                    idFieldVal = fdv.getVal();
                }
            }

            // Set parameter based on field type
            if ("S".equals(fdv.getFieldType())) {
                params[i] = new DBPreparedHandlerParam(fdv.getVal());
            } else if ("D".equals(fdv.getFieldType())) {
                // Convert date strings to SQL Date objects
                params[i] = new DBPreparedHandlerParam(
                        new java.sql.Date(MyDateFormat.getCalendarwithTime(fdv.getVal()).getTime().getTime()));
            } else {
                // Handle integer fields
                params[i] = new DBPreparedHandlerParam(Integer.valueOf(fdv.getVal()).intValue());
            }
        }
        sql = sql.substring(0, sql.length() - 1);
        phs = phs.substring(0, phs.length() - 1);
        sql += ") values (" + phs + ")";

        // Validate code uniqueness before insertion
        LookupCodeValue lkv = GetCode(tableDef.getTableId(), idFieldVal);
        if (lkv != null) {
            throw new SQLException("The Code Already Exists.");
        }

        queryExecuteUpdate(sql, params);

        return idFieldVal;
    }

    /**
     * Updates an existing code value record in the lookup table.
     *
     * This method dynamically constructs an UPDATE SQL statement based on
     * the field definitions and handles data type conversions. Date fields
     * are processed differently based on their editability - editable dates
     * use date-only format while non-editable dates include time information.
     *
     * @param tableDef LookupTableDefValue the table definition containing metadata
     * @param fieldDefList List<FieldDefValue> the field definitions with values to update
     * @return String the ID of the updated record
     * @throws SQLException if database operation fails
     */
    private String UpdateCodeValue(LookupTableDefValue tableDef, List fieldDefList) throws SQLException {
        String tableName = tableDef.getTableName();
        String idFieldName = "";
        String idFieldVal = "";

        DBPreparedHandlerParam[] params = new DBPreparedHandlerParam[fieldDefList.size() + 1];
        String sql = "update " + tableName + " set ";

        // Build UPDATE SQL and prepare parameters
        for (int i = 0; i < fieldDefList.size(); i++) {
            FieldDefValue fdv = (FieldDefValue) fieldDefList.get(i);

            // Identify ID field for WHERE clause
            if (fdv.getGenericIdx() == 1) {
                idFieldName = fdv.getFieldSQL();
                idFieldVal = fdv.getVal();
            }

            sql += fdv.getFieldSQL() + "=?,";

            // Set parameter based on field type
            if ("S".equals(fdv.getFieldType())) {
                params[i] = new DBPreparedHandlerParam(fdv.getVal());
            } else if ("D".equals(fdv.getFieldType())) {
                // Handle date fields based on editability
                if (fdv.isEditable()) {
                    // Editable dates: date-only format
                    params[i] = new DBPreparedHandlerParam(
                            new java.sql.Date(MyDateFormat.getCalendar(fdv.getVal()).getTime().getTime()));
                } else {
                    // Non-editable dates: full date-time format
                    params[i] = new DBPreparedHandlerParam(
                            new java.sql.Date(MyDateFormat.getCalendarwithTime(fdv.getVal()).getTime().getTime()));
                }
            } else {
                // Handle integer fields
                params[i] = new DBPreparedHandlerParam(Integer.valueOf(fdv.getVal()).intValue());
            }
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += " where " + idFieldName + "=?";
        // Use the ID field value as WHERE clause parameter
        params[fieldDefList.size()] = params[0];

        queryExecuteUpdate(sql, params);

        return idFieldVal;
    }

    /**
     * {@inheritDoc}
     *
     * Saves a program as an organization code in the hierarchical structure.
     * This method creates or updates a program entry in the organization
     * lookup table, maintaining the tree structure and CSV relationships.
     * Programs are prefixed with 'P' and linked to their parent facility.
     * Updates to existing programs trigger cascade updates to child nodes.
     */
    @Override
    public void SaveAsOrgCode(Program program) throws SQLException {

        // Generate formatted program ID with padding
        String programId = "0000000" + program.getId().toString();
        programId = "P" + programId.substring(programId.length() - 7);
        String fullCode = "P" + program.getId();

        // Generate formatted facility ID for hierarchy lookup
        String facilityId = "0000000" + String.valueOf(program.getFacilityId());
        facilityId = "F" + facilityId.substring(facilityId.length() - 7);

        // Get parent facility code to build hierarchy
        LookupCodeValue fcd = GetCode("ORG", "F" + program.getFacilityId());
        fullCode = fcd.getBuf1() + fullCode;

        // Check if program code already exists
        boolean isNew = false;
        LookupCodeValue pcd = GetCode("ORG", "P" + program.getId());
        if (pcd == null) {
            isNew = true;
            pcd = new LookupCodeValue();
        }

        // Set program organization code properties
        pcd.setPrefix("ORG");
        pcd.setCode("P" + program.getId());
        pcd.setCodeTree(fcd.getCodeTree() + programId);
        pcd.setCodecsv(fcd.getCodecsv() + "P" + program.getId() + ",");
        pcd.setDescription(program.getName());
        pcd.setBuf1(fullCode);
        pcd.setActive(program.isActive());
        pcd.setOrderByIndex(0);
        pcd.setLastUpdateDate(Calendar.getInstance());
        pcd.setLastUpdateUser(program.getLastUpdateUser());

        // Update hierarchy and status for existing programs
        if (!isNew) {
            this.updateOrgTree(pcd.getCode(), pcd);
            this.updateOrgStatus(pcd.getCode(), pcd);
        }
        this.SaveCodeValue(isNew, pcd);
    }

    /**
     * Updates the organizational tree structure for child nodes.
     *
     * When an organization code changes its hierarchy information
     * (tree code, CSV path, or full code), this method cascades
     * those changes to all child nodes in the organization tree.
     * Uses SQL REPLACE function to update all matching patterns
     * in the hierarchical structure fields.
     *
     * @param orgCd String the organization code being updated
     * @param newCd LookupCodeValue the new code value with updated hierarchy
     */
    private void updateOrgTree(String orgCd, LookupCodeValue newCd) {
        LookupCodeValue oldCd = GetCode("ORG", orgCd);

        // Only update if hierarchy has changed
        if (!oldCd.getCodecsv().equals(newCd.getCodecsv())) {
            String oldFullCode = oldCd.getBuf1();
            String oldTreeCode = oldCd.getCodeTree();
            String oldCsv = oldCd.getCodecsv();

            String newFullCode = newCd.getBuf1();
            String newTreeCode = newCd.getCodeTree();
            String newCsv = newCd.getCodecsv();

            // Update all child nodes with new hierarchy information
            String sql = "update lst_orgcd set fullcode = replace(fullcode, :oldFullCode, :newFullCode), "
                    + "codetree = replace(codetree, :oldTreeCode, :newTreeCode), "
                    + "codecsv = replace(codecsv, :oldCsv, :newCsv) "
                    + "where codecsv like :oldCsvPattern";

            Session session = sessionFactory.getCurrentSession();
            try {
                session.createSQLQuery(sql)
                    .setParameter("oldFullCode", oldFullCode)
                    .setParameter("newFullCode", newFullCode)
                    .setParameter("oldTreeCode", oldTreeCode)
                    .setParameter("newTreeCode", newTreeCode)
                    .setParameter("oldCsv", oldCsv)
                    .setParameter("newCsv", newCsv)
                    .setParameter("oldCsvPattern", oldCsv + "_%")
                    .executeUpdate();
            } finally {
                session.close();
            }
        }
    }

    /**
     * Updates the active status of child organization nodes.
     *
     * When an organization is deactivated, this method ensures
     * that all child nodes in the hierarchy are also deactivated
     * to maintain organizational consistency. Uses the CSV path
     * pattern matching to find all descendant organizations.
     *
     * @param orgCd String the organization code being updated
     * @param newCd LookupCodeValue the new code value with updated status
     */
    private void updateOrgStatus(String orgCd, LookupCodeValue newCd) {
        LookupCodeValue oldCd = GetCode("ORG", orgCd);

        // If organization is being deactivated, deactivate all children
        if (!newCd.isActive()) {
            String oldCsv = oldCd.getCodecsv() + "_%";

            // Find all child organization codes
            List<LstOrgcd> o = (List<LstOrgcd>) this.getHibernateTemplate()
                    .find("FROM LstOrgcd o WHERE o.codecsv like ?0", oldCsv);

            // Deactivate each child organization
            for (LstOrgcd l : o) {
                l.setActiveyn(0);
                this.getHibernateTemplate().update(l);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Determines if one organization is a child of another in the hierarchy.
     * This method checks if org1 is a parent or ancestor of org2 by comparing
     * their full codes. The hierarchical relationship is determined by
     * checking if org1's full code is contained within org2's full code.
     */
    @Override
    public boolean inOrg(String org1, String org2) {
        boolean isInString = false;
        String sql = "From LstOrgcd a where  a.fullcode like %?0";

        // Find organization objects by full code pattern
        LstOrgcd orgObj1 = (LstOrgcd) getHibernateTemplate().find(sql, new Object[]{org1});
        LstOrgcd orgObj2 = (LstOrgcd) getHibernateTemplate().find(sql, new Object[]{org2});

        // Check if org1 is contained within org2's hierarchy
        if (orgObj2.getFullcode().indexOf(orgObj1.getFullcode()) > 0)
            isInString = true;
        return isInString;
    }

    /**
     * {@inheritDoc}
     *
     * Saves a facility as an organization code in the hierarchical structure.
     * This method creates or updates a facility entry in the organization
     * lookup table, maintaining the tree structure and CSV relationships.
     * Facilities are prefixed with 'F' and linked to their parent organization.
     * Updates to existing facilities trigger cascade updates to child nodes.
     */
    @Override
    public void SaveAsOrgCode(Facility facility) throws SQLException {

        // Generate formatted facility ID with padding
        String facilityId = "0000000" + facility.getId().toString();
        facilityId = "F" + facilityId.substring(facilityId.length() - 7);
        String fullCode = "F" + facility.getId();

        // Generate formatted organization ID for hierarchy lookup
        String orgId = "0000000" + String.valueOf(facility.getOrgId());
        orgId = "S" + orgId.substring(orgId.length() - 7);

        // Get parent organization code to build hierarchy
        LookupCodeValue ocd = GetCode("ORG", "S" + facility.getOrgId());
        fullCode = ocd.getBuf1() + fullCode;

        // Check if facility code already exists
        boolean isNew = false;
        LookupCodeValue fcd = GetCode("ORG", "F" + facility.getId());
        if (fcd == null) {
            isNew = true;
            fcd = new LookupCodeValue();
        }

        // Set facility organization code properties
        fcd.setPrefix("ORG");
        fcd.setCode("F" + facility.getId());
        fcd.setCodeTree(ocd.getCodeTree() + facilityId);
        fcd.setCodecsv(ocd.getCodecsv() + "F" + facility.getId() + ",");
        fcd.setDescription(facility.getName());
        fcd.setBuf1(fullCode);
        fcd.setActive(!facility.isDisabled());
        fcd.setOrderByIndex(0);
        fcd.setLastUpdateDate(Calendar.getInstance());

        // Update hierarchy and status for existing facilities
        if (!isNew) {
            this.updateOrgTree(fcd.getCode(), fcd);
            this.updateOrgStatus(fcd.getCode(), fcd);
        }
        this.SaveCodeValue(isNew, fcd);
    }

    /**
     * {@inheritDoc}
     *
     * Saves a lookup code value as an organization code in the hierarchical structure.
     * This method creates or updates organization entries based on lookup table
     * codes, determining the appropriate prefix and parent relationships.
     * Organization tables (OGN) use 'O' prefix with 'R' parents, while
     * shelter tables (SHL) use 'S' prefix with 'O' parents. Updates to existing
     * organization codes trigger cascade updates to child nodes.
     */
    @Override
    public void SaveAsOrgCode(LookupCodeValue orgVal, String tableId) throws SQLException {

        // Determine organization prefix based on table ID
        String orgPrefix = tableId.substring(0, 1);
        String orgPrefixP = "R1";
        // Parent of Organization is R, parent of Shelter is O
        if ("S".equals(orgPrefix))
            orgPrefixP = "O";

        // Generate formatted organization ID with padding
        String orgId = "0000000" + orgVal.getCode();
        orgId = orgPrefix + orgId.substring(orgId.length() - 7);

        String orgCd = orgPrefix + orgVal.getCode();
        String parentCd = orgPrefixP + orgVal.getParentCode();

        // Find parent organization code
        LookupCodeValue pCd = GetCode("ORG", parentCd);
        if (pCd == null)
            return;

        // Check if organization code already exists
        LookupCodeValue ocd = GetCode("ORG", orgCd);
        boolean isNew = false;
        if (ocd == null) {
            isNew = true;
            ocd = new LookupCodeValue();
        }

        // Set organization code properties
        ocd.setPrefix("ORG");
        ocd.setCode(orgCd);
        ocd.setCodeTree(pCd.getCodeTree() + orgId);
        ocd.setCodecsv(pCd.getCodecsv() + orgCd + ",");
        ocd.setDescription(orgVal.getDescription());
        ocd.setBuf1(pCd.getBuf1() + orgCd);
        ocd.setActive(orgVal.isActive());
        ocd.setOrderByIndex(0);
        ocd.setLastUpdateDate(Calendar.getInstance());
        ocd.setLastUpdateUser(orgVal.getLastUpdateUser());

        // Update hierarchy and status for existing organization codes
        if (!isNew) {
            this.updateOrgTree(ocd.getCode(), ocd);
            this.updateOrgStatus(ocd.getCode(), ocd);
        }
        this.SaveCodeValue(isNew, ocd);
    }

    /**
     * {@inheritDoc}
     *
     * Executes a stored procedure with the provided parameters.
     * This method provides a simple interface for executing database
     * stored procedures, handling parameter passing and error management.
     * Used for complex database operations that are better implemented
     * as stored procedures rather than dynamic SQL.
     */
    @Override
    public void runProcedure(String procName, String[] params) throws SQLException {
        DBPreparedHandler db = new DBPreparedHandler();
        db.procExecute(procName, params);
    }

    /**
     * {@inheritDoc}
     *
     * Counts the number of active clients in an organization hierarchy.
     * This method checks both admitted clients (admission table) and
     * clients in program queues. It uses the organizational hierarchy
     * CSV structure to find all programs within the specified organization
     * and its sub-organizations. Returns the count from admissions if
     * any exist, otherwise returns the count from program queues.
     */
    @Override
    public int getCountOfActiveClient(String orgCd) throws SQLException {
        // Count admitted clients in organization hierarchy
        String sql = "select count(*) from admission where admission_status='" + KeyConstants.INTAKE_STATUS_ADMITTED
                + "' and  'P' || program_id in (" + " select code from lst_orgcd  where codecsv like '%' || '" + orgCd
                + ",' || '%')";

        // Count clients in program queues within organization hierarchy
        String sql1 = "select count(*) from program_queue where  'P' || program_id in ("
                + " select code from lst_orgcd  where codecsv like '%' || '" + orgCd + ",' || '%')";

        DBPreparedHandler db = new DBPreparedHandler();

        // First check admitted clients
        ResultSet rs = db.queryResults(sql);
        int id = 0;
        if (rs.next())
            id = rs.getInt(1);

        // If admitted clients exist, return that count
        if (id > 0)
            return id;

        // Otherwise check program queue
        rs.close();
        rs = db.queryResults(sql1);
        if (rs.next())
            id = rs.getInt(1);
        rs.close();
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the provider DAO for provider-related lookups.
     * This method allows dependency injection of the provider DAO,
     * which is used specifically for filtering user lookup codes
     * based on program membership and provider access controls.
     */
    @Override
    public void setProviderDao(ProviderDao providerDao) {
        this.providerDao = providerDao;
    }

    /**
     * Executes a parameterized SQL update statement.
     *
     * This method handles the low-level execution of prepared statements
     * with proper parameter binding based on parameter types. It supports
     * string, date, integer, and timestamp parameters, with null handling.
     * Used internally by insert and update operations to ensure proper
     * SQL parameter binding and prevent SQL injection vulnerabilities.
     *
     * @param preparedSQL String the parameterized SQL statement
     * @param params DBPreparedHandlerParam[] array of parameters with types
     * @return int the number of rows affected by the update
     * @throws SQLException if the SQL execution fails
     */
    private int queryExecuteUpdate(String preparedSQL, DBPreparedHandlerParam[] params) throws SQLException {
        PreparedStatement preparedStmt = DbConnectionFilter.getThreadLocalDbConnection().prepareStatement(preparedSQL);

        // Bind parameters based on their types
        for (int i = 0; i < params.length; i++) {
            DBPreparedHandlerParam param = params[i];

            if (param == null)
                preparedStmt.setObject(i + 1, null);
            else if (DBPreparedHandlerParam.PARAM_STRING.equals(param.getParamType())) {
                preparedStmt.setString(i + 1, param.getStringValue());
            } else if (DBPreparedHandlerParam.PARAM_DATE.equals(param.getParamType())) {
                preparedStmt.setDate(i + 1, param.getDateValue());
            } else if (DBPreparedHandlerParam.PARAM_INT.equals(param.getParamType())) {
                preparedStmt.setInt(i + 1, param.getIntValue());
            } else if (DBPreparedHandlerParam.PARAM_TIMESTAMP.equals(param.getParamType())) {
                preparedStmt.setTimestamp(i + 1, param.getTimestampValue());
            }
        }
        return (preparedStmt.executeUpdate());
    }

}
