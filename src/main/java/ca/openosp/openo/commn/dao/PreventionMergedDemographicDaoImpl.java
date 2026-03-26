//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.merge.MergedDemographicTemplate;
import ca.openosp.openo.commn.model.Prevention;
import org.springframework.stereotype.Repository;

@Repository("preventionDaoImpl")
public class PreventionMergedDemographicDaoImpl extends PreventionDaoImpl implements PreventionMergedDemographicDao {

    @Override
    public List<Prevention> findByDemographicId(Integer demographicId) {
        List<Prevention> result = super.findByDemographicId(demographicId);
        MergedDemographicTemplate<Prevention> template = new MergedDemographicTemplate<Prevention>() {
            @Override
            protected List<Prevention> findById(Integer demographic_no) {
                return PreventionMergedDemographicDaoImpl.super.findByDemographicId(demographic_no);
            }
        };
        return template.findMerged(demographicId, result);
    }

    @Override
    public List<Prevention> findByDemographicIdAfterDatetime(Integer demographicId, final Date dateTime) {
        List<Prevention> result = super.findByDemographicIdAfterDatetime(demographicId, dateTime);
        MergedDemographicTemplate<Prevention> template = new MergedDemographicTemplate<Prevention>() {
            @Override
            protected List<Prevention> findById(Integer demographic_no) {
                return PreventionMergedDemographicDaoImpl.super.findByDemographicIdAfterDatetime(demographic_no, dateTime);
            }
        };
        return template.findMerged(demographicId, result);
    }

    @Override
    public List<Prevention> findNotDeletedByDemographicId(Integer demographicId) {
        List<Prevention> result = super.findNotDeletedByDemographicId(demographicId);
        MergedDemographicTemplate<Prevention> template = new MergedDemographicTemplate<Prevention>() {
            @Override
            protected List<Prevention> findById(Integer demographic_no) {
                return PreventionMergedDemographicDaoImpl.super.findNotDeletedByDemographicId(demographic_no);
            }
        };
        return template.findMerged(demographicId, result);
    }

    @Override
    public List<Prevention> findByTypeAndDemoNo(final String preventionType, Integer demoNo) {
        List<Prevention> result = super.findByTypeAndDemoNo(preventionType, demoNo);
        MergedDemographicTemplate<Prevention> template = new MergedDemographicTemplate<Prevention>() {
            @Override
            protected List<Prevention> findById(Integer demographic_no) {
                return PreventionMergedDemographicDaoImpl.super.findByTypeAndDemoNo(preventionType, demographic_no);
            }
        };
        return template.findMerged(demoNo, result);
    }
}
