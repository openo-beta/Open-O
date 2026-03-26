//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.openosp.openo.commn.dao.DrugDao;
import ca.openosp.openo.commn.dao.DrugDispensingDao;
import ca.openosp.openo.commn.dao.DrugProductDao;
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.commn.model.DrugDispensing;
import ca.openosp.openo.commn.model.DrugProduct;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DrugDispensingManagerImpl implements DrugDispensingManager {

    @Autowired
    private DrugDispensingDao drugDispensingDao;

    @Autowired
    private DrugProductDao drugProductDao;

    @Autowired
    private DrugDao drugDao;


    /**
     * Return the dispensing status as a string.
     * <p>
     * there's some extra code in here to be used later to get info about doses remaining/available/dispensed
     *
     * @param drugId
     * @return
     */
    @Override
    public String getStatus(Integer drugId) {

        Drug drug = drugDao.find(drugId);

        if (drug == null) {
            return null;
        }

        Integer totalDosesAvailable = null;
        String strTotalDosesAvailable = "<Unknown>";

        try {
            int quantity = Integer.parseInt(drug.getQuantity());
            totalDosesAvailable = quantity + (quantity * drug.getRepeat());
            strTotalDosesAvailable = totalDosesAvailable.toString();
        } catch (NumberFormatException e) {
            MiscUtils.getLogger().error("Error", e);
            return null;
        }

        List<DrugDispensing> dispensingEvents = drugDispensingDao.findByDrugId(drugId);

        Map<Integer, Integer> productAmounts = new HashMap<Integer, Integer>();

        for (DrugDispensing dd : dispensingEvents) {
            int totalAmountForDD = 0;

            List<DrugProduct> dps = drugProductDao.findByDispensingId(dd.getId());
            for (int x = 0; x < dps.size(); x++) {
                DrugProduct dp = dps.get(x);
                totalAmountForDD += dp.getAmount();
            }

            productAmounts.put(dd.getId(), totalAmountForDD);
        }

        int totalDosesDispensed = 0;
        int totalDispensingEvents = dispensingEvents.size();
        int totalQuantitiesDispensed = 0;

        for (DrugDispensing dd : dispensingEvents) {
            totalDosesDispensed += productAmounts.get(dd.getId());
            totalQuantitiesDispensed += dd.getQuantity();
        }

        Integer totalDosesRemaining = (totalDosesAvailable == null) ? null : Integer.valueOf(totalDosesAvailable - totalDosesDispensed);
        String strTotalDosesRemaining = (totalDosesRemaining == null) ? "<Unknown>" : String.valueOf(totalDosesRemaining);

        String status = null;
        if (totalDosesRemaining != null && totalDosesRemaining > 0) {
            status = "Active";
        } else {
            status = "Filled";
        }
        if (status.equals("Active") && drug.isExpired()) {
            status = "Expired";
        }

        return status;
    }
}
