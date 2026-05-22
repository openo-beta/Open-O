//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.time.DateFormatUtils;
import ca.openosp.openo.commn.model.EForm;
import ca.openosp.openo.commn.model.EFormReportTool;
import ca.openosp.openo.commn.model.EFormValue;
import ca.openosp.openo.util.SqlUtils;
import org.springframework.stereotype.Repository;

@Repository
public class EFormReportToolDaoImpl extends AbstractDaoImpl<EFormReportTool> implements EFormReportToolDao {

    public EFormReportToolDaoImpl() {
        super(EFormReportTool.class);
    }

    @SuppressWarnings("unchecked")
    public void markLatest(Integer eformReportToolId) {
        EFormReportTool eft = find(eformReportToolId);
        if (eft != null) {
            String table = SqlUtils.validateTableName(eft.getTableName());
            //get all distinct demographicNos
            Query q = entityManager.createNativeQuery("select distinct demographicNo from " + table);
            List<Integer> demoNos = q.getResultList();
            for (Integer demoNo : demoNos) {
                Query q2 = entityManager.createNativeQuery("select id from " + table + " where demographicNo = ?1 order by dateFormCreated desc,fdid desc");
                q2.setParameter(1, demoNo);
                q2.setMaxResults(1);
                List<Integer> idList = q2.getResultList();

                if (!idList.isEmpty()) {
                    //update the first result
                    Query q3 = entityManager.createNativeQuery("update " + table + " set eft_latest=1 where id = ?1");
                    q3.setParameter(1, idList.get(0));
                    q3.executeUpdate();
                }
            }

            eft.setLatestMarked(true);
            merge(eft);
        }
    }

    public void addNew(EFormReportTool eformReportTool, EForm eform, List<String> fields, String providerNo) {
        //generate the create table statement
        String tableName = "ERT_" + SqlUtils.validateColumnName(eformReportTool.getName())
                + (new BigInteger(130, new SecureRandom()).toString(8).substring(0, 8));
        SqlUtils.validateTableName(tableName);
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (");
        sql.append("id int (10) NOT NULL auto_increment primary key,");
        sql.append("fdid int (10) NOT NULL, ");
        sql.append("demographicNo int (10) NOT NULL, ");
        sql.append("dateFormCreated datetime NOT NULL, ");
        sql.append("providerNo varchar(6) NOT NULL, ");
        sql.append("eft_latest tinyint(1) NOT NULL, ");
        sql.append("dateCreated timestamp NOT NULL ");
        for (String field : fields) {
            SqlUtils.validateColumnName(field);
            sql.append(",`" + field + "` text");
        }
        sql.append(")");

        //commit the table
        Query q = entityManager.createNativeQuery(sql.toString());
        q.executeUpdate();

        //save the EformReportTool
        eformReportTool.setDateLastPopulated(null);
        eformReportTool.setId(null);
        eformReportTool.setTableName(tableName);
        eformReportTool.setProviderNo(providerNo);
        eformReportTool.setLatestMarked(false);
        persist(eformReportTool);

    }

    public void populateReportTableItem(EFormReportTool eft, List<EFormValue> values, Integer fdid, Integer demographicNo, Date dateFormCreated, String providerNo) {
        String table = SqlUtils.validateTableName(eft.getTableName());

        // Build column list with validated column names
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(table);
        sb.append(" (fdid, demographicNo, dateFormCreated, providerNo, eft_latest, dateCreated");
        for (EFormValue v : values) {
            SqlUtils.validateColumnName(v.getVarName());
            sb.append(", `" + v.getVarName() + "`");
        }
        sb.append(") VALUES (?1, ?2, ?3, ?4, 0, now()");
        for (int i = 0; i < values.size(); i++) {
            sb.append(", ?" + (i + 5));
        }
        sb.append(")");

        Query q = entityManager.createNativeQuery(sb.toString());
        q.setParameter(1, fdid);
        q.setParameter(2, demographicNo);
        q.setParameter(3, DateFormatUtils.format(dateFormCreated, "yyyy-MM-dd HH:mm:ss"));
        q.setParameter(4, providerNo);
        for (int i = 0; i < values.size(); i++) {
            q.setParameter(i + 5, values.get(i).getVarValue());
        }
        q.executeUpdate();
    }

    public void deleteAllData(EFormReportTool eft) {
        if (eft != null) {
            String table = SqlUtils.validateTableName(eft.getTableName());
            Query q = entityManager.createNativeQuery("delete from " + table);
            q.executeUpdate();
        }
    }

    public void drop(EFormReportTool eft) {
        if (eft != null) {
            String table = SqlUtils.validateTableName(eft.getTableName());
            Query q = entityManager.createNativeQuery("drop table " + table);
            q.executeUpdate();
        }
    }

    public Integer getNumRecords(EFormReportTool eformReportTool) {
        if (eformReportTool != null) {
            String table = SqlUtils.validateTableName(eformReportTool.getTableName());
            Query q = entityManager.createNativeQuery("select count(*) from " + table);
            List<BigInteger> results = q.getResultList();
            if (!results.isEmpty()) {
                return results.get(0).intValue();
            }
        }
        return null;
    }

}
