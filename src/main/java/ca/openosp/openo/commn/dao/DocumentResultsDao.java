//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.model.Document;

import ca.openosp.openo.lab.ca.on.LabResultData;

public interface DocumentResultsDao {
    public boolean isSentToValidProvider(String docNo);

    public boolean isSentToProvider(String docNo, String providerNo);

    public ArrayList<LabResultData> populateDocumentResultsDataOfAllProviders(String providerNo, String demographicNo,
                                                                              String status);

    //retrieve documents belonging to a providers
    public ArrayList<LabResultData> populateDocumentResultsDataLinkToProvider(String providerNo, String demographicNo,
                                                                              String status);

    //retrieve all documents from database
    public ArrayList<LabResultData> populateDocumentResultsData(String providerNo, String demographicNo, String status);

    public List<Document> getPhotosByAppointmentNo(int appointmentNo);
}
 