//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package org.oscarehr.common.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.oscarehr.common.model.BillingONCHeader1;
import org.oscarehr.common.model.BillingONExt;
import org.oscarehr.common.model.BillingONPayment;
import org.oscarehr.common.model.BillingPaymentType;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author mweston4
 */
public interface BillingONExtDao extends AbstractDao<BillingONExt> {

    String KEY_PAYMENT = "payment";
    String KEY_REFUND = "refund";
    String KEY_DISCOUNT = "discount";
    String KEY_CREDIT = "credit";
    String KEY_PAY_DATE = "payDate";
    String KEY_PAY_METHOD = "payMethod";
    String KEY_TOTAL = "total";
    String KEY_GST = "gst";

    List<BillingONExt> find(String key, String value);

    List<BillingONExt> findByBillingNoAndKey(Integer billingNo, String key);

    List<BillingONExt> findByBillingNoAndPaymentIdAndKey(Integer billingNo, Integer paymentId, String key);

    String getPayMethodDesc(BillingONExt bExt);

    BigDecimal getPayment(BillingONPayment paymentRecord);

    BigDecimal getRefund(BillingONPayment paymentRecord);

    BillingONExt getRemitTo(BillingONCHeader1 bCh1);

    BillingONExt getBillTo(BillingONCHeader1 bCh1);

    BillingONExt getBillToInactive(BillingONCHeader1 bCh1);

    BillingONExt getDueDate(BillingONCHeader1 bCh1);

    BillingONExt getUseBillTo(BillingONCHeader1 bCh1);

    List<BillingONExt> find(Integer billingNo, String key, Date start, Date end);

    List<BillingONExt> findByBillingNoAndPaymentNo(int billingNo, int paymentId);

    List<BillingONExt> getClaimExtItems(int billingNo);

    List<BillingONExt> getBillingExtItems(String billingNo);

    List<BillingONExt> getInactiveBillingExtItems(String billingNo);

    BigDecimal getAccountVal(int billingNo, String key);

    BillingONExt getClaimExtItem(Integer billingNo, Integer demographicNo, String keyVal);

    void setExtItem(int billingNo, int demographicNo, String keyVal, String value, Date dateTime, char status);

    // public static boolean isNumberKey(String key);
    boolean isNumberKey(String key);
}
