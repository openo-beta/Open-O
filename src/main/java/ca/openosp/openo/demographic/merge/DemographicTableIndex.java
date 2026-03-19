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

package ca.openosp.openo.demographic.merge;

/**
 * Describes a single database table to the demographic merge copy engine.
 * <p>
 * The copy engine uses the fields in this class to build copy queries dynamically
 * at runtime — no table names or column names are hardcoded in the engine itself.
 *
 * @since 2026-03-19
 */
public class DemographicTableIndex {

    private String tableName;
    private String demographicColumn;
    private String pkColumn;
    private String pkType;
    private String additionalWhereClause;

    /**
     * Constructs a table index with no additional WHERE clause.
     *
     * @param tableName          String the name of the database table
     * @param demographicColumn  String the column name that holds the demographic_no value
     * @param pkColumn           String the primary key column name
     * @param pkType             String the primary key type (e.g. "int", "bigint")
     */
    public DemographicTableIndex(String tableName, String demographicColumn, String pkColumn, String pkType) {
        this.tableName = tableName;
        this.demographicColumn = demographicColumn;
        this.pkColumn = pkColumn;
        this.pkType = pkType;
    }

    /**
     * Constructs a table index with an additional WHERE clause fragment.
     *
     * @param tableName              String the name of the database table
     * @param demographicColumn      String the column name that holds the demographic_no value
     * @param pkColumn               String the primary key column name
     * @param pkType                 String the primary key type (e.g. "int", "bigint")
     * @param additionalWhereClause  String extra SQL fragment appended to the WHERE clause (e.g. "module = 'demographic'")
     */
    public DemographicTableIndex(String tableName, String demographicColumn, String pkColumn, String pkType, String additionalWhereClause) {
        this.tableName = tableName;
        this.demographicColumn = demographicColumn;
        this.pkColumn = pkColumn;
        this.pkType = pkType;
        this.additionalWhereClause = additionalWhereClause;
    }

    /**
     * @return String the database table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName String the database table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return String the column name that stores demographic_no for this table
     */
    public String getDemographicColumn() {
        return demographicColumn;
    }

    /**
     * @param demographicColumn String the column name that stores demographic_no for this table
     */
    public void setDemographicColumn(String demographicColumn) {
        this.demographicColumn = demographicColumn;
    }

    /**
     * @return String the primary key column name
     */
    public String getPkColumn() {
        return pkColumn;
    }

    /**
     * @param pkColumn String the primary key column name
     */
    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    /**
     * @return String the primary key type (e.g. "int", "bigint")
     */
    public String getPkType() {
        return pkType;
    }

    /**
     * @param pkType String the primary key type (e.g. "int", "bigint")
     */
    public void setPkType(String pkType) {
        this.pkType = pkType;
    }

    /**
     * @return String optional extra SQL fragment appended to the WHERE clause, or null if not set
     */
    public String getAdditionalWhereClause() {
        return additionalWhereClause;
    }

    /**
     * @param additionalWhereClause String optional extra SQL fragment appended to the WHERE clause
     */
    public void setAdditionalWhereClause(String additionalWhereClause) {
        this.additionalWhereClause = additionalWhereClause;
    }
}
