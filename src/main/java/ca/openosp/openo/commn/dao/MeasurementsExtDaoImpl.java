//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;
import java.util.HashMap;

import ca.openosp.openo.commn.model.MeasurementsExt;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementsExtDaoImpl extends AbstractDaoImpl<MeasurementsExt> implements MeasurementsExtDao {

    public MeasurementsExtDaoImpl() {
        super(MeasurementsExt.class);
    }

    @Override
    public List<MeasurementsExt> getMeasurementsExtByMeasurementId(Integer measurementId) {
        String queryStr = "select m FROM MeasurementsExt m WHERE m.measurementId = ?1";
        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, measurementId);

        @SuppressWarnings("unchecked")
        List<MeasurementsExt> rs = q.getResultList();

        return rs;
    }

    @Override
    public HashMap<String, MeasurementsExt> getMeasurementsExtMapByMeasurementId(Integer measurementId) {
        HashMap<String, MeasurementsExt> measurementsExtHashMap = new HashMap<String, MeasurementsExt>();
        List<MeasurementsExt> rs = getMeasurementsExtByMeasurementId(measurementId);

        for (MeasurementsExt measurementsExt : rs) {
            measurementsExtHashMap.put(measurementsExt.getKeyVal(), measurementsExt);
        }

        return measurementsExtHashMap;
    }

    @Override
    public List<MeasurementsExt> getMeasurementsExtListByMeasurementIdList(List<Integer> measurementIdList) {
        String queryStr = "select m FROM MeasurementsExt m WHERE m.measurementId IN (?1) order by m.measurementId";
        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, measurementIdList);
        return q.getResultList();
    }

    @Override
    public MeasurementsExt getMeasurementsExtByMeasurementIdAndKeyVal(Integer measurementId, String keyVal) {
        String queryStr = "select m FROM MeasurementsExt m WHERE m.measurementId = ?1 AND m.keyVal = ?2";
        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, measurementId);
        q.setParameter(2, keyVal);

        @SuppressWarnings("unchecked")
        List<MeasurementsExt> rs = q.getResultList();

        if (rs.isEmpty()) {
            return null;
        }
        return rs.get(0);
    }

    @Override
    public Integer getMeasurementIdByKeyValue(String key, String value) {
        String queryStr = "select m FROM MeasurementsExt m WHERE m.keyVal=?1 AND m.val=?2";
        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, key);
        q.setParameter(2, value);

        @SuppressWarnings("unchecked")
        List<MeasurementsExt> rs = q.getResultList();

        if (rs.size() > 0) return rs.get(0).getMeasurementId();
        return null;
    }

    @Override
    public List<MeasurementsExt> findByKeyValue(String key, String value) {
        String queryStr = "select m FROM MeasurementsExt m WHERE m.keyVal=?1 AND m.val=?2";
        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, key);
        q.setParameter(2, value);

        @SuppressWarnings("unchecked")
        List<MeasurementsExt> rs = q.getResultList();

        return rs;
    }

    @Override
    public Integer getMeasurementIdByLabNoAndTestName(String labNo, String testName) {
        //TODO: consider replacing this with a simpler query.   This approach was taken because a self join such as the following was resulting in hibernate errors and in the interests in time
        //a procedural approach was taken
        /*
         * SELECT DISTINCT a.measurementId
         * FROM MeasurementsExt a
         * JOIN MeasurementsExt b ON a.measurementId = b.measurementId
         * WHERE a.keyval = 'lab_no' and a.val = ?1
         * AND b.keyval = 'name' and b.val = ?2;
         */

        String queryStr = "SELECT distinct m.measurementId FROM MeasurementsExt m " + "WHERE m.keyVal = 'lab_no' and m.val = ?1";

        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, labNo);

        @SuppressWarnings("unchecked")
        List<Integer> rs = q.getResultList();

        if (!rs.isEmpty()) {
            String mIds = "";
            for (int i = 0; i < rs.size(); i++) {
                mIds += rs.get(i);
                if (i <= rs.size() - 2) {
                    mIds += ",";
                }
            }
            queryStr = "SELECT distinct m.measurementId FROM MeasurementsExt m " + "WHERE m.measurementId in (" + mIds + ") and m.keyVal = 'name' and m.val = ?1";

            q = entityManager.createQuery(queryStr);
            q.setParameter(1, testName);

            @SuppressWarnings("unchecked")
            List<Integer> rs2 = q.getResultList();

            if (!rs2.isEmpty()) {
                return rs2.get(0);
            }
            return null;
        }
        return null;
    }

    @Override
    public List<Integer> findUnmappedMeasuremntIds(List<String> excludeList) {
        String queryStr = "SELECT MAX(m.measurementId) FROM MeasurementsExt m WHERE m.keyVal LIKE 'identifier' AND m.val NOT IN (?1)";
        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, excludeList);
        return q.getResultList();
    }
}
