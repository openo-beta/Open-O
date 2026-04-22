//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.login.DBHelp;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface PregnancyFormsDao {

    public static Integer getLatestFormIdByPregnancy(Integer episodeId) {
        String sql = "SELECT id from formONAREnhancedRecord WHERE episodeId=" + episodeId + " ORDER BY formEdited DESC";
        ResultSet rs = DBHelp.searchDBRecord(sql);
        try {
            if (rs.next()) {
                Integer id = rs.getInt("id");
                return id;
            }
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
            return 0;
        }
        return 0;
    }

    public static Integer getLatestFormIdByDemographicNo(Integer demographicNo) {
        String sql = "SELECT id from formONAREnhancedRecord WHERE demographic_no=" + demographicNo + " ORDER BY formEdited DESC";
        ResultSet rs = DBHelp.searchDBRecord(sql);
        try {
            if (rs.next()) {
                Integer id = rs.getInt("id");
                return id;
            }
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
            return 0;
        }
        return 0;
    }

    public static Integer getLatestAR2005FormIdByDemographicNo(Integer demographicNo) {
        String sql = "SELECT id from formONAR WHERE demographic_no=" + demographicNo + " ORDER BY formEdited DESC";
        ResultSet rs = DBHelp.searchDBRecord(sql);
        try {
            if (rs.next()) {
                Integer id = rs.getInt("id");
                return id;
            }
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
            return 0;
        }
        return 0;
    }
}
