//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.dao.DrugProductDao;
import ca.openosp.openo.commn.dao.DrugProductTemplateDao;
import ca.openosp.openo.commn.dao.ProductLocationDao;
import ca.openosp.openo.commn.model.DrugProduct;
import ca.openosp.openo.commn.model.DrugProductTemplate;
import ca.openosp.openo.commn.model.ProductLocation;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.openosp.openo.log.LogAction;

@Service
public class DrugProductManagerImpl implements DrugProductManager {

    @Autowired
    private DrugProductDao drugProductDao;

    @Autowired
    private ProductLocationDao productLocationDao;

    @Autowired
    private DrugProductTemplateDao drugProductTemplateDao;

    @Override
    public void saveDrugProduct(LoggedInInfo loggedInInfo, DrugProduct drugProduct) {
        drugProductDao.persist(drugProduct);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.saveDrugProduct", "id=" + drugProduct.getId());

    }

    @Override
    public void updateDrugProduct(LoggedInInfo loggedInInfo, DrugProduct drugProduct) {
        drugProductDao.merge(drugProduct);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.saveDrugProduct", "id=" + drugProduct.getId());

    }

    @Override
    public DrugProduct getDrugProduct(LoggedInInfo loggedInInfo, Integer id) {
        DrugProduct result = drugProductDao.find(id);

        if (result != null) {
            //--- log action ---
            LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getDrugProduct", "id=" + result.getId());
        }

        return result;
    }

    @Override
    public List<DrugProduct> getAllDrugProducts(LoggedInInfo loggedInInfo, Integer offset, Integer limit) {
        List<DrugProduct> results = drugProductDao.findAll(offset, limit);

        //--- log action ---
        if (results.size() > 0) {
            String resultIds = DrugProduct.getIdsAsStringList(results);
            LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getAllDrugProducts", "ids returned=" + resultIds);
        }

        return results;
    }

    @Override
    public List<DrugProduct> getAllDrugProductsByName(LoggedInInfo loggedInInfo, Integer offset, Integer limit, String productName) {
        List<DrugProduct> results = drugProductDao.findByName(offset, limit, productName);

        //--- log action ---
        if (results.size() > 0) {
            String resultIds = DrugProduct.getIdsAsStringList(results);
            LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getAllDrugProductsByName", "ids returned=" + resultIds);
        }

        return results;
    }

    @Override
    public List<DrugProduct> getAllDrugProductsByNameAndLot(LoggedInInfo loggedInInfo, Integer offset, Integer limit, String productName, String lotNumber, Integer location, boolean availableOnly) {
        List<DrugProduct> results = drugProductDao.findByNameAndLot(offset, limit, productName, lotNumber, location, availableOnly);

        //--- log action ---
        if (results.size() > 0) {
            String resultIds = DrugProduct.getIdsAsStringList(results);
            LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getAllDrugProductsByName", "ids returned=" + resultIds);
        }

        return results;
    }

    @Override
    public Integer getAllDrugProductsByNameAndLotCount(LoggedInInfo loggedInInfo, String productName, String lotNumber, Integer location, boolean availableOnly) {
        Integer result = drugProductDao.findByNameAndLotCount(productName, lotNumber, location, availableOnly);

        LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getAllDrugProductsByNameAndLotCount", "");


        return result;
    }

    @Override
    public List<DrugProduct> getAllDrugProductsGroupedByCode(LoggedInInfo loggedInInfo, Integer offset, Integer limit) {
        List<DrugProduct> results = drugProductDao.findAll(offset, limit);

        //--- log action ---
        if (results.size() > 0) {
            String resultIds = DrugProduct.getIdsAsStringList(results);
            LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getAllDrugProducts", "ids returned=" + resultIds);
        }

        return results;
    }

    @Override
    public List<String> findUniqueDrugProductNames(LoggedInInfo loggedInInfo) {
        List<String> results = drugProductDao.findUniqueDrugProductNames();

        //--- log action ---
        if (results.size() > 0) {
            LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getUniqueDrugProductNames", "");
        }

        return results;
    }

    @Override
    public List<String> findUniqueDrugProductLotsByName(LoggedInInfo loggedInInfo, String productName) {
        if (productName == null || "".equals(productName)) {
            return new ArrayList<String>();
        }
        List<String> results = drugProductDao.findUniqueDrugProductLotsByName(productName);

        //--- log action ---
        if (results.size() > 0) {
            LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.getUniqueDrugProductNames", "");
        }

        return results;
    }

    @Override
    public void deleteDrugProduct(LoggedInInfo loggedInInfo, Integer drugProductId) {
        DrugProduct drugProduct = drugProductDao.find(drugProductId);
        if (drugProduct != null && drugProduct.getDispensingEvent() != null) {
            throw new RuntimeException("Cannot delete a dispensed drug");
        }

        drugProductDao.remove(drugProductId);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "DrugProductManager.deleteDrugProduct", "id=" + drugProductId);
    }

    @Override
    public List<ProductLocation> getProductLocations() {
        return productLocationDao.findAll(0, ProductLocationDao.MAX_LIST_RETURN_SIZE);
    }

    @Override
    public List<DrugProductTemplate> getDrugProductTemplates() {
        return drugProductTemplateDao.findAll(0, DrugProductTemplateDao.MAX_LIST_RETURN_SIZE);
    }
}
