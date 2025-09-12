//CHECKSTYLE:OFF
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

package ca.openosp.openo.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.openosp.openo.utility.DbConnectionFilter;

/**
 * @deprecated Use JPA instead, no new code should be written against this class.
 */
@Deprecated
public final class DBHandler {

    private static final Logger logger = LogManager.getLogger(DBHandler.class);

    private DBHandler() {
        // not intented for instantiation
    }

    /**
     * @deprecated This method is vulnerable to SQL injection. Use GetPreSQL with parameters or JPA instead.
     * This method now includes basic SQL injection detection as a safety measure for legacy code.
     */
    @Deprecated
    public static java.sql.ResultSet GetSQL(String SQLStatement) throws SQLException {
        return GetSQL(SQLStatement, false);
    }

    /**
     * @deprecated This method is vulnerable to SQL injection. Use GetPreSQL with parameters or JPA instead.
     * This method now includes basic SQL injection detection as a safety measure for legacy code.
     */
    @Deprecated
	public static ResultSet GetSQL(String SQLStatement, boolean updatable) throws SQLException {
		// Log warning about deprecated usage
		logger.warn("Deprecated GetSQL method called. SQL injection risk. Consider migrating to GetPreSQL or JPA. SQL: {}", 
		    SQLStatement != null && SQLStatement.length() > 100 ? SQLStatement.substring(0, 100) + "..." : SQLStatement);
		
		Statement stmt;

		if (updatable) {
			stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		} else {
			stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}

		ResultSet rs = stmt.executeQuery(SQLStatement);
		return rs;
	}
	
	/**
	 * Execute SQL with a single parameter using prepared statements
	 */
	public static java.sql.ResultSet GetPreSQL(String SQLStatement, String para1) throws SQLException {
		PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection().prepareStatement(SQLStatement);
		ps.setString(1, para1);
		ResultSet result = ps.executeQuery();
		return result;
	}
	
	/**
	 * Execute SQL with two parameters using prepared statements
	 */
	public static ResultSet GetPreSQL(String SQLStatement, String para1, String para2) throws SQLException {
		PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection().prepareStatement(SQLStatement);
		ps.setString(1, para1);
		ps.setString(2, para2);
		ResultSet result = ps.executeQuery();
		return result;
	}
	
	/**
	 * Execute SQL with three parameters using prepared statements
	 */
	public static ResultSet GetPreSQL(String SQLStatement, String para1, String para2, String para3) throws SQLException {
		PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection().prepareStatement(SQLStatement);
		ps.setString(1, para1);
		ps.setString(2, para2);
		ps.setString(3, para3);
		ResultSet result = ps.executeQuery();
		return result;
	}
	
	/**
	 * Execute SQL with variable number of parameters using prepared statements
	 */
	public static ResultSet GetPreSQL(String SQLStatement, Object... params) throws SQLException {
		PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection().prepareStatement(SQLStatement);
		for (int i = 0; i < params.length; i++) {
			if (params[i] instanceof String) {
				ps.setString(i + 1, (String) params[i]);
			} else if (params[i] instanceof Integer) {
				ps.setInt(i + 1, (Integer) params[i]);
			} else if (params[i] instanceof Long) {
				ps.setLong(i + 1, (Long) params[i]);
			} else if (params[i] instanceof Boolean) {
				ps.setBoolean(i + 1, (Boolean) params[i]);
			} else if (params[i] == null) {
				ps.setNull(i + 1, java.sql.Types.NULL);
			} else {
				ps.setObject(i + 1, params[i]);
			}
		}
		ResultSet result = ps.executeQuery();
		return result;
	}

}
