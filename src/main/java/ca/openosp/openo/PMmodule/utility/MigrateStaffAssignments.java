//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.PMmodule.utility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.DbConnectionFilter;
import ca.openosp.openo.utility.MiscUtils;

public class MigrateStaffAssignments {
    private static final Logger log = MiscUtils.getLogger();
    protected int nurseRoleId = 0;
    protected int doctorRoleId = 0;

    public MigrateStaffAssignments() {
    }

    public void run() throws Exception {
        nurseRoleId = (int) this.getRoleId("Nurse");
        doctorRoleId = (int) this.getRoleId("Doctor");

        Statement stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement();
        stmt.execute("SELECT * FROM provider_role_program");
        ResultSet rs = stmt.getResultSet();

        while (rs.next()) {
            long providerNo = rs.getInt("provider_no");
            long programId = rs.getInt("program_id");
            long group_id = rs.getInt("group_id");
            if (programExists(programId) && providerExists(providerNo)) {

                this.addProgramProvider(programId, providerNo, group_id);
            }


        }
        rs.close();

    }

    public boolean programExists(long programId) throws Exception {
        try (PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection()
                .prepareStatement("SELECT count(*) as num FROM program where program_id = ?")) {
            ps.setLong(1, programId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("num") > 0;
            }
        }
    }

    public boolean providerExists(long providerNo) throws Exception {
        try (PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection()
                .prepareStatement("SELECT count(*) as num FROM provider where provider_no = ?")) {
            ps.setLong(1, providerNo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("num") > 0;
            }
        }
    }

    public long getRoleId(String name) throws Exception {
        try (PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection()
                .prepareStatement("SELECT * FROM caisi_role where name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("role_id");
                } else {
                    throw new Exception("Role not defined: " + name);
                }
            }
        }
    }

    public void addProgramProvider(long programId, long providerNo, long groupNo) throws Exception {
        long roleId = groupNo == 9 ? nurseRoleId : doctorRoleId;
        try (PreparedStatement ps = DbConnectionFilter.getThreadLocalDbConnection()
                .prepareStatement("insert into program_provider (program_id,provider_no,role_id,team_id) values (?,?,?,?)")) {
            ps.setLong(1, programId);
            ps.setLong(2, providerNo);
            ps.setLong(3, roleId);
            ps.setLong(4, 0);
            ps.executeUpdate();
        }
    }

    public static void main(String args[]) throws Exception {
        new MigrateStaffAssignments().run();
    }
}
