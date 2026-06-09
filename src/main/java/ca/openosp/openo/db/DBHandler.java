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
import java.sql.Types;

import ca.openosp.openo.utility.DbConnectionFilter;

/**
 * @deprecated Use JPA instead, no new code should be written against this class.
 */
@Deprecated
public final class DBHandler {

    private DBHandler() {
        // not intented for instantiation
    }

	private static void bindParams(PreparedStatement ps, Object... params) throws SQLException {
		for (int i = 0; i < params.length; i++) {
			Object p = params[i];
			if (p == null) {
				ps.setNull(i+1, Types.NULL);
			} else {
				ps.setObject(i+1, p);
			}
		}
	}

	public static ResultSet GetPreSQL(String sql, Object... params) throws SQLException {
		PreparedStatement ps = DbConnectionFilter
			.getThreadLocalDbConnection()
			.prepareStatement(sql);
		bindParams(ps, params);
		return ps.executeQuery();
	}

	/**
	 * Parameterized query with updatable ResultSet support.
	 * Used by form record classes that insert/update rows via ResultSet cursor operations.
	 */
	public static ResultSet GetPreSQLUpdatable(String sql, Object... params) throws SQLException {
		PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection()
			.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		bindParams(ps, params);
		return ps.executeQuery();
	}

}
