//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.PharmacyInfo;

public interface PharmacyInfoDao extends AbstractDao<PharmacyInfo> {


    public void addPharmacy(String name, String address, String city, String province, String postalCode, String phone1, String phone2, String fax, String email, String serviceLocationIdentifier, String notes);

    public void updatePharmacy(Integer ID, String name, String address, String city, String province, String postalCode, String phone1, String phone2, String fax, String email, String serviceLocationIdentifier, String notes);

    public void deletePharmacy(Integer ID);

    public List<PharmacyInfo> getPharmacies(List<Integer> idList);

    public PharmacyInfo getPharmacy(Integer ID);

    public PharmacyInfo getPharmacyByRecordID(Integer recordID);

    public List<PharmacyInfo> getAllPharmacies();

    public List<PharmacyInfo> searchPharmacyByNameAddressCity(String name, String city);

    public List<String> searchPharmacyByCity(String city);

    // public PharmacyInfo find(Integer id);
    // public void persist(PharmacyInfo pharmacyInfo);
    // public void merge(PharmacyInfo pharmacyInfo);
    // public List<PharmacyInfo> findAll();
    //public PharmacyInfo saveEntity(PharmacyInfo pharmacyInfo);
}