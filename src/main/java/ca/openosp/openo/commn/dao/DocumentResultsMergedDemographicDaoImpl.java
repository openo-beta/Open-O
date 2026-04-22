//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.merge.MergedDemographicTemplate;
import org.springframework.stereotype.Repository;
import ca.openosp.openo.lab.ca.on.LabResultData;
import ca.openosp.openo.util.ConversionUtils;

@Repository("documentResultsDao")
public class DocumentResultsMergedDemographicDaoImpl extends DocumentResultsDaoImpl implements DocumentResultsMergedDemographicDao {

    @Override
    public ArrayList<LabResultData> populateDocumentResultsDataOfAllProviders(final String providerNo, String demographicNo, final String status) {
        ArrayList<LabResultData> result = super.populateDocumentResultsDataOfAllProviders(providerNo, demographicNo, status);
        if (false)
            return result;

        MergedDemographicTemplate<LabResultData> template = new MergedDemographicTemplate<LabResultData>() {
            @Override
            protected List<LabResultData> findById(Integer demographic_no) {
                return DocumentResultsMergedDemographicDaoImpl.super.populateDocumentResultsDataOfAllProviders(providerNo, demographic_no.toString(), status);
            }
        };
        return (ArrayList<LabResultData>) template.findMerged(ConversionUtils.fromIntString(demographicNo), result);
    }

    @Override
    public ArrayList<LabResultData> populateDocumentResultsDataLinkToProvider(final String providerNo, String demographicNo, final String status) {
        ArrayList<LabResultData> result = super.populateDocumentResultsDataLinkToProvider(providerNo, demographicNo, status);
        if (false)
            return result;

        MergedDemographicTemplate<LabResultData> template = new MergedDemographicTemplate<LabResultData>() {
            @Override
            protected List<LabResultData> findById(Integer demographic_no) {
                return DocumentResultsMergedDemographicDaoImpl.super.populateDocumentResultsDataLinkToProvider(providerNo, demographic_no.toString(), status);
            }
        };
        return (ArrayList<LabResultData>) template.findMerged(ConversionUtils.fromIntString(demographicNo), result);
    }

    @Override
    public ArrayList<LabResultData> populateDocumentResultsData(final String providerNo, String demographicNo, final String status) {
        ArrayList<LabResultData> result = super.populateDocumentResultsData(providerNo, demographicNo, status);
        if (false)
            return result;
        MergedDemographicTemplate<LabResultData> template = new MergedDemographicTemplate<LabResultData>() {
            @Override
            protected List<LabResultData> findById(Integer demographic_no) {
                return DocumentResultsMergedDemographicDaoImpl.super.populateDocumentResultsData(providerNo, demographic_no.toString(), status);
            }
        };
        return (ArrayList<LabResultData>) template.findMerged(ConversionUtils.fromIntString(demographicNo), result);
    }
}
