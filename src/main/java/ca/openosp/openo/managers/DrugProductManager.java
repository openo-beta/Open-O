//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.model.DrugProduct;
import ca.openosp.openo.commn.model.DrugProductTemplate;
import ca.openosp.openo.commn.model.ProductLocation;
import ca.openosp.openo.utility.LoggedInInfo;

public interface DrugProductManager {

    public void saveDrugProduct(LoggedInInfo loggedInInfo, DrugProduct drugProduct);

    public void updateDrugProduct(LoggedInInfo loggedInInfo, DrugProduct drugProduct);

    public DrugProduct getDrugProduct(LoggedInInfo loggedInInfo, Integer id);

    public List<DrugProduct> getAllDrugProducts(LoggedInInfo loggedInInfo, Integer offset, Integer limit);

    public List<DrugProduct> getAllDrugProductsByName(LoggedInInfo loggedInInfo, Integer offset, Integer limit,
                                                      String productName);

    public List<DrugProduct> getAllDrugProductsByNameAndLot(LoggedInInfo loggedInInfo, Integer offset, Integer limit,
                                                            String productName, String lotNumber, Integer location, boolean availableOnly);

    public Integer getAllDrugProductsByNameAndLotCount(LoggedInInfo loggedInInfo, String productName, String lotNumber,
                                                       Integer location, boolean availableOnly);

    public List<DrugProduct> getAllDrugProductsGroupedByCode(LoggedInInfo loggedInInfo, Integer offset, Integer limit);

    public List<String> findUniqueDrugProductNames(LoggedInInfo loggedInInfo);

    public List<String> findUniqueDrugProductLotsByName(LoggedInInfo loggedInInfo, String productName);

    public void deleteDrugProduct(LoggedInInfo loggedInInfo, Integer drugProductId);

    public List<ProductLocation> getProductLocations();

    public List<DrugProductTemplate> getDrugProductTemplates();
}
