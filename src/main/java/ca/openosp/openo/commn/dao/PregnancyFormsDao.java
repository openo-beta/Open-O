//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.db.DBHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface PregnancyFormsDao {

    public static Integer getLatestFormIdByPregnancy(Integer episodeId) {
        String sql = "SELECT id from formONAREnhancedRecord WHERE episodeId = ? ORDER BY formEdited DESC";
        try {
            ResultSet rs = DBHandler.GetPreSQL(sql, episodeId);
            if (rs.next()) {
                Integer id = rs.getInt("id");
                rs.close();
                return id;
            }
            rs.close();
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
            return 0;
        }
        return 0;
    }

    public static Integer getLatestFormIdByDemographicNo(Integer demographicNo) {
        String sql = "SELECT id from formONAREnhancedRecord WHERE demographic_no = ? ORDER BY formEdited DESC";
        try {
            ResultSet rs = DBHandler.GetPreSQL(sql, demographicNo);
            if (rs.next()) {
                Integer id = rs.getInt("id");
                rs.close();
                return id;
            }
            rs.close();
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
            return 0;
        }
        return 0;
    }

    public static Integer getLatestAR2005FormIdByDemographicNo(Integer demographicNo) {
        String sql = "SELECT id from formONAR WHERE demographic_no = ? ORDER BY formEdited DESC";
        try {
            ResultSet rs = DBHandler.GetPreSQL(sql, demographicNo);
            if (rs.next()) {
                Integer id = rs.getInt("id");
                rs.close();
                return id;
            }
            rs.close();
        } catch (SQLException e) {
            MiscUtils.getLogger().error("Error", e);
            return 0;
        }
        return 0;
    }
}
