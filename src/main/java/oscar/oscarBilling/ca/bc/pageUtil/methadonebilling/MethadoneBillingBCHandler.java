//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */

package oscar.oscarBilling.ca.bc.pageUtil.methadonebilling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;


import org.apache.logging.log4j.Logger;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.common.dao.DemographicDao;
import org.oscarehr.common.dao.DiagnosticCodeDao;
import org.oscarehr.common.model.Billing;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.DiagnosticCode;
import org.oscarehr.common.model.Provider;
import oscar.util.UtilDateUtilities;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import net.sf.json.JSONObject;
import oscar.OscarProperties;
import oscar.entities.Billingmaster;
import oscar.oscarBilling.ca.bc.data.BillingHistoryDAO;
import oscar.oscarBilling.ca.bc.data.BillingNote;
import oscar.oscarBilling.ca.bc.data.BillingmasterDAO;
import oscar.oscarBilling.ca.bc.pageUtil.BillingBillingManager;
import oscar.oscarBilling.ca.bc.pageUtil.BillingBillingManager.BillingItem;
import oscar.oscarBilling.ca.bc.pageUtil.BillingSessionBean;;

/**
 * @author OSCARprn by Treatment - support@oscarprn.com
 * @Company OSCARprn by Treatment
 * @Date Nov 30, 2012
 * @Filename MethadoneBillingBCHandler.java
 * @Comment Copy Right OSCARprn by Treatment
 */
public class MethadoneBillingBCHandler {

    // full logging to be added later. too pressed for time.
    private static final Logger log = MiscUtils.getLogger();

    // default attributes for MSP billing.
    // create new attributes for dynamic form input.
    public static final String PAYMENT_TYPE_NAME = "ELECTRONIC";
    public static final String PAYMENT_TYPE = "6";
    public static final Integer APPOINTMENT_NO = 0;
    public static final String PAYMENT_MODE = "0";
    public static final String CLAIM_CODE = "C02";
    public static final String ANATOMICAL_AREA = "00";
    public static final String NEW_PROGRAM = "00";
    public static final String BILL_REGION = "BC";
    public static final String SUBMISSION_CODE = "0";
    public static final String CORRESPONDENCE_CODE = "0";
    public static final String BILLING_PROV = "BC";
    public static final String MVA_CLAIM_CODE = "N";
    public static final String DEPENDENT_CODE = "00";
    public static final String INTERNAL_COMMENT = "MSP Billing done by methadone billing method.";
    public static final String AFTERHOUR_CODE = "0";
    public static final String ADMISSION_DATE = "0000-00-00";
    public static final String BILLINGVISIT = "A";

    public static final String BILLING_UNIT = "1";
    public static final String HALF_BILLING = "0.5";

    private Date today;
    private DemographicDao demographicDao;
    private ProviderDao providerDao;
    private Properties oscarProperties;
    private BillingBillingManager bmanager;
    private Billing billing;
    private BillingmasterDAO billingmasterDAO;
    private BillingHistoryDAO billingHistoryDAO;
    private DiagnosticCodeDao diagnosticCodeDao;
    private Billingmaster billingmaster;
    private int numberSaved;

    /**
     * Default constructor.
     */
    public MethadoneBillingBCHandler() {

        this.today = new Date();
        demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
        providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
        oscarProperties = OscarProperties.getInstance();
        bmanager = new BillingBillingManager();
        billing = new Billing();
        billingmasterDAO = (BillingmasterDAO) SpringUtils.getBean(BillingmasterDAO.class);
        billingHistoryDAO = new BillingHistoryDAO();
        diagnosticCodeDao = (DiagnosticCodeDao) SpringUtils.getBean(DiagnosticCodeDao.class);

    }

    /**
     * @return Demographic Data Access Object
     */
    public DemographicDao getDemographicDao() {
        return demographicDao;
    }

    /**
     * @return Provider Data Access Object
     */
    public ProviderDao getProviderDao() {
        return providerDao;
    }

    /**
     * @return Oscar Properties Object
     */
    public Properties getOscarProperties() {
        return oscarProperties;
    }


    /**
     * The number of invoices saved in the last
     * session.
     *
     * @return int
     */
    public int getNumberSaved() {
        return numberSaved;
    }

    /**
     * set the number of invoices saved in this session.
     *
     * @param numberSaved
     */


    /**
     * Again method borrowed from BillingSaveBilling2Action.
     *
     * @param billingid
     * @param billingAccountStatus
     * @param dataCenterId
     * @param billedAmount
     * @param paymentMode
     * @param bean
     * @param billingUnit
     * @param serviceCode
     * @return
     */


    /**
     * UTILITY METHOD -REALLY SHOULDN'T BE IN HERE...
     * But I made it static - unlike others.
     *
     * @param s
     * @return
     */
    public static String convertDate8Char(String s) {
        String sdate = "00000000", syear = "", smonth = "", sday = "";
        log.debug("s=" + s);
        if (s != null) {

            if (s.indexOf("-") != -1) {

                syear = s.substring(0, s.indexOf("-"));
                s = s.substring(s.indexOf("-") + 1);
                smonth = s.substring(0, s.indexOf("-"));
                if (smonth.length() == 1) {
                    smonth = "0" + smonth;
                }
                s = s.substring(s.indexOf("-") + 1);
                sday = s;
                if (sday.length() == 1) {
                    sday = "0" + sday;
                }

                log.debug("Year" + syear + " Month" + smonth + " Day" + sday);
                sdate = syear + smonth + sday;

            } else {
                sdate = s;
            }
            log.debug("sdate:" + sdate);
        } else {
            sdate = "00000000";

        }
        return sdate;
    }


}
