//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.merge.MergedDemographicTemplate;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.documentManager.EDocUtil.EDocSort;
import ca.openosp.openo.util.ConversionUtils;

public class DocumentMergeDemographicDAOImpl extends DocumentDaoImpl implements DocumentMergeDemographicDAO {

    @Override
    public List<Object[]> findDocuments(final String module, String moduleid, final String docType,
                                        final boolean includePublic, final boolean includeDeleted, final boolean includeActive, final EDocSort sort,
                                        final Date since) {
        List<Object[]> result = super.findDocuments(module, moduleid, docType, includePublic, includeDeleted,
                includeActive, sort, null);
        MergedDemographicTemplate<Object[]> template = new MergedDemographicTemplate<Object[]>() {
            @Override
            protected List<Object[]> findById(Integer demographic_no) {
                return DocumentMergeDemographicDAOImpl.super.findDocuments(module, demographic_no.toString(), docType,
                        includePublic, includeDeleted, includeActive, sort, since);
            }
        };
        return template.findMerged(ConversionUtils.fromIntString(moduleid), result);
    }

    @Override
    public List<Document> findByDemographicId(String demoNo) {
        List<Document> result = super.findByDemographicId(demoNo);
        MergedDemographicTemplate<Document> template = new MergedDemographicTemplate<Document>() {
            @Override
            protected List<Document> findById(Integer demographic_no) {
                return DocumentMergeDemographicDAOImpl.super.findByDemographicId(demographic_no.toString());
            }
        };
        return template.findMerged(ConversionUtils.fromIntString(demoNo), result);
    }

}
