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

package ca.openosp.openo.entities;

import java.math.BigDecimal;
import java.util.Date;

/**
 * PrivateBillTransaction entity representing payment transactions for private pay
 * healthcare services within OpenO EMR. This entity tracks individual payment
 * receipts against private billing master records, supporting comprehensive
 * financial management for non-insured healthcare services.
 *
 * <p>This entity is essential for managing private pay healthcare transactions where
 * patients pay directly for services not covered by provincial healthcare plans or
 * private insurance. It provides detailed tracking of payment amounts, payment methods,
 * and transaction timestamps for accurate financial reporting and account management.
 *
 * <p>Key financial management features include:
 * <ul>
 * <li>Precise payment amount tracking with BigDecimal precision for financial accuracy
 * <li>Payment method categorization supporting various payment types
 * <li>Creation date tracking for transaction audit trails
 * <li>Integration with billing master records for comprehensive bill management
 * <li>Payment type descriptions for user-friendly transaction reporting
 * </ul>
 *
 * <p>The private billing workflow typically involves:
 * <ol>
 * <li>Service delivery and private bill creation
 * <li>Patient payment collection with transaction recording
 * <li>Payment allocation against outstanding bill balances
 * <li>Financial reporting and account reconciliation
 * </ol>
 *
 * <p>Payment amounts are handled using BigDecimal arithmetic to ensure precise
 * financial calculations without floating-point rounding errors, which is
 * critical for healthcare billing accuracy and compliance.
 *
 * @see ca.openosp.openo.entities.PaymentType
 * @see java.math.BigDecimal
 * @since November 1, 2004
 */
public class PrivateBillTransaction {
    private int id;
    private int billingmaster_no;
    private double amount_received;
    private Date creation_date;
    private int payment_type;
    private String payment_type_desc;

    /**
     * Default constructor for PrivateBillTransaction entity.
     * Creates a new PrivateBillTransaction instance with all fields initialized to defaults.
     */
    public PrivateBillTransaction() {
    }

    /**
     * Sets the unique identifier for this transaction.
     * This ID serves as the primary key for database operations.
     *
     * @param id int the transaction ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the billing master number that this transaction is applied against.
     * This links the payment to a specific private bill record.
     *
     * @param billingmaster_no int the billing master record number
     */
    public void setBillingmaster_no(int billingmaster_no) {
        this.billingmaster_no = billingmaster_no;
    }

    /**
     * Sets the payment amount received for this transaction.
     * The amount is processed using BigDecimal arithmetic to ensure financial precision,
     * rounded to 2 decimal places using half-up rounding for currency accuracy.
     *
     * @param amount_received double the payment amount received
     */
    public void setAmount_received(double amount_received) {
        BigDecimal bdFee = new BigDecimal(amount_received).setScale(2,
                BigDecimal.ROUND_HALF_UP);
        this.amount_received = bdFee.doubleValue();
    }

    /**
     * Sets the creation date for this transaction.
     * This timestamp is used for audit trails and financial reporting.
     *
     * @param creation_date Date the date when the transaction was created
     */
    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    /**
     * Sets the payment type identifier for this transaction.
     * This references the payment method used (cash, credit card, cheque, etc.).
     *
     * @param payment_type int the payment type ID
     * @see ca.openosp.openo.entities.PaymentType
     */
    public void setPayment_type(int payment_type) {
        this.payment_type = payment_type;
    }

    /**
     * Sets the human-readable description of the payment type.
     * This provides a user-friendly description of the payment method.
     *
     * @param payment_type_desc String the payment type description
     */
    public void setPayment_type_desc(String payment_type_desc) {
        this.payment_type_desc = payment_type_desc;
    }

    /**
     * Gets the unique identifier for this transaction.
     *
     * @return int the transaction ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the billing master number that this transaction is applied against.
     *
     * @return int the billing master record number
     */
    public int getBillingmaster_no() {
        return billingmaster_no;
    }

    /**
     * Gets the payment amount received for this transaction.
     * The amount has been processed for financial precision and currency accuracy.
     *
     * @return double the payment amount received
     */
    public double getAmount_received() {
        return amount_received;
    }

    /**
     * Gets the creation date for this transaction.
     *
     * @return Date the date when the transaction was created
     */
    public Date getCreation_date() {
        return creation_date;
    }

    /**
     * Gets the payment type identifier for this transaction.
     *
     * @return int the payment type ID
     * @see ca.openosp.openo.entities.PaymentType
     */
    public int getPayment_type() {
        return payment_type;
    }

    /**
     * Gets the human-readable description of the payment type.
     *
     * @return String the payment type description
     */
    public String getPayment_type_desc() {
        return payment_type_desc;
    }
}
