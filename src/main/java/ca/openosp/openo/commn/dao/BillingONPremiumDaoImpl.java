//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.io.*;

import ca.openosp.openo.commn.model.BillingONPremium;
import org.springframework.stereotype.Repository;

import java.util.List;

import ca.openosp.openo.commn.model.RaHeader;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.OscarProperties;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.util.DateUtils;

import java.util.Locale;
import javax.persistence.Query;
import java.util.Date;

import ca.openosp.openo.commn.model.Provider;

/**
 * @author mweston4
 */
@Repository
public class BillingONPremiumDaoImpl extends AbstractDaoImpl<BillingONPremium> implements BillingONPremiumDao {

    public BillingONPremiumDaoImpl() {
        super(BillingONPremium.class);
    }

    public List<BillingONPremium> getActiveRAPremiumsByPayDate(Date startDate, Date endDate, Locale locale) {
        String sql = "select bPrem from BillingONPremium bPrem where payDate >= ?1 and payDate < ?2 and status=?3";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        query.setParameter(3, true);

        @SuppressWarnings("unchecked")
        List<BillingONPremium> results = query.getResultList();
        return results;
    }

    public List<BillingONPremium> getActiveRAPremiumsByProvider(Provider p, Date startDate, Date endDate, Locale locale) {
        String sql = "select bPrem from BillingONPremium bPrem where payDate >= ?1 and payDate < ?2 and status=?3 and providerNo=?4";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        query.setParameter(3, true);
        query.setParameter(4, p.getProviderNo());

        @SuppressWarnings("unchecked")
        List<BillingONPremium> results = query.getResultList();
        return results;
    }

    public List<BillingONPremium> getRAPremiumsByRaHeaderNo(Integer raHeaderNo) {
        String sql = "select bPrem from BillingONPremium bPrem where raHeaderNo=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, raHeaderNo);

        @SuppressWarnings("unchecked")
        List<BillingONPremium> results = query.getResultList();
        return results;
    }

    public void parseAndSaveRAPremiums(LoggedInInfo loggedInInfo, Integer raHeaderNo, Locale locale) {

        String filepath = OscarProperties.getInstance().getProperty("DOCUMENT_DIR").trim();
        RaHeaderDao raHeaderDao = (RaHeaderDao) SpringUtils.getBean(RaHeaderDao.class);
        RaHeader raHeader = raHeaderDao.find(raHeaderNo);

        String filename = raHeader.getFilename();
        FileInputStream file = null;
        InputStreamReader reader = null;
        BufferedReader input = null;
        StringBuilder msgText = new StringBuilder();

        try {
            file = new FileInputStream(filepath + filename);
            reader = new InputStreamReader(file);
            input = new BufferedReader(reader);

            String nextline;
            while ((nextline = input.readLine()) != null) {

                if (nextline.startsWith("HR8")) {
                    msgText = msgText.append(nextline.substring(3, 73)).append("\n");
                }
            }
        } catch (java.io.FileNotFoundException e) {
            MiscUtils.getLogger().warn("File not found:" + filepath + filename);
        } catch (java.io.IOException e) {
            MiscUtils.getLogger().warn("Unexpected error", e);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (java.io.IOException e) {
                MiscUtils.getLogger().warn("Unexpected error", e);
            }
        }

        StringReader strReader = new StringReader(msgText.toString());
        input = new BufferedReader(strReader);

        String msgLine = "";
        try {
            while ((msgLine = input.readLine()) != null) {

                if (msgLine.matches("(\\*){70}")) {

                    if ((msgLine = input.readLine()) != null && msgLine.trim().equals("PREMIUM PAYMENTS")) {

                        BillingONPremium premium = new BillingONPremium();

                        if ((msgLine = input.readLine()) != null && msgLine.trim().startsWith("FOR PAYMENT:")) {

                            String payDateStr = msgLine.substring(12, 70).trim();
                            try {
                                java.util.Date payDate = DateUtils.parseDate(payDateStr, locale);
                                premium.setPayDate(payDate);

                                if ((msgLine = input.readLine()) != null && msgLine.trim().startsWith("PROVIDER NUMBER:")) {

                                    String providerOHIPNo = msgLine.substring(16, 70).trim();
                                    premium.setProviderOHIPNo(providerOHIPNo);

                                    while ((msgLine = input.readLine()) != null) {

                                        if (msgLine.startsWith("TOTAL MONTHLY PREMIUM PAYMENT")) {

                                            String amountPay = msgLine.substring(29, 70).trim();
                                            amountPay = amountPay.substring(1, amountPay.length());
                                            amountPay = amountPay.replace(",", "");

                                            premium.setAmountPay(amountPay);

                                            premium.setRAHeaderNo(raHeaderNo);
                                            premium.setCreator(loggedInInfo.getLoggedInProviderNo());
                                            premium.setCreateDate(new java.util.Date());

                                            //now that all values are filled, we can persist the object to the DB
                                            this.persist(premium);
                                            break;
                                        }
                                    }
                                }
                            } catch (java.text.ParseException e) {
                                MiscUtils.getLogger().warn("Cannot parse MOH PayDate", e);
                            }
                        }
                    }
                }
            }
        } catch (java.io.IOException e) {
            MiscUtils.getLogger().warn("Unexpected error", e);
        } finally {
            if (strReader != null)
                strReader.close();
            try {
                if (input != null)
                    input.close();
            } catch (java.io.IOException e) {
                MiscUtils.getLogger().warn("Unexpected error", e);
            }
        }
    }
}
