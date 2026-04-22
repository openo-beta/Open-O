//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.DrugProduct;
import ca.openosp.openo.prescription.dispensary.LotBean;

public interface DrugProductDao extends AbstractDao<DrugProduct> {

    public List<DrugProduct> findAvailable();

    public List<DrugProduct> findAvailableByCode(String code);

    public List<Object[]> findAllAvailableUnique();

    public List<Object[]> findAllUnique();

    public List<String> findUniqueDrugProductNames();

    public int getAvailableCount(String lotNumber, Date expiryDate, int amount);

    public List<DrugProduct> getAvailableDrugProducts(String lotNumber, Date expiryDate, int amount);

    public List<LotBean> findDistinctLotsAvailableByCode(String code);

    public DrugProduct findByCodeAndLotNumber(String code, String lotNumber);

    public List<DrugProduct> findByDispensingId(Integer id);

    public List<DrugProduct> findByName(int offset, int limit, String name);

    public List<DrugProduct> findAll(int offset, int limit);

    public List<DrugProduct> findByNameAndLot(int offset, int limit, String name, String lotNumber, Integer location,
                                              boolean availableOnly);

    public Integer findByNameAndLotCount(String name, String lotNumber, Integer location, boolean availableOnly);

    public List<String> findUniqueDrugProductLotsByName(String productName);
}
