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

/**
 * PaymentType entity representing different payment methods available for healthcare
 * billing and private pay transactions within OpenO EMR. This entity defines the
 * various payment types that can be associated with billing transactions, supporting
 * both insured and private pay healthcare services.
 *
 * <p>Payment types in healthcare systems typically include provincial healthcare coverage,
 * private insurance, direct patient payment, Workers' Compensation Board (WCB) coverage,
 * and other specialized funding sources. This entity enables proper categorization and
 * tracking of payment sources for accurate billing and financial reporting.
 *
 * <p>Common healthcare payment types include:
 * <ul>
 * <li>Provincial health insurance (MSP, OHIP, etc.)
 * <li>Private insurance coverage
 * <li>Direct patient payment (cash, credit, debit)
 * <li>Workers' Compensation Board (WCB) claims
 * <li>Third-party insurance coverage
 * <li>Research/study funding
 * </ul>
 *
 * @see ca.openosp.openo.entities.PrivateBillTransaction
 * @see ca.openosp.openo.entities.WCB
 * @since June 11, 2005
 */
public class PaymentType {
    private String id;
    private String paymentType;

    /**
     * Default constructor for PaymentType entity.
     * Creates a new PaymentType instance with all fields initialized to null.
     */
    public PaymentType() {
    }

    /**
     * Gets the unique identifier for this payment type.
     * This ID is used as the primary key for database operations and references
     * from billing transactions.
     *
     * @return String the payment type ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this payment type.
     *
     * @param id String the payment type ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the descriptive name of the payment type.
     * This field contains the human-readable description of the payment method,
     * such as "Cash", "Credit Card", "MSP", "WCB", "Private Insurance", etc.
     *
     * @return String the payment type description
     */
    public String getPaymentType() {
        return paymentType;
    }

    /**
     * Sets the descriptive name of the payment type.
     * This should be a clear, user-friendly description of the payment method
     * that will be displayed in billing interfaces and reports.
     *
     * @param paymentType String the payment type description
     */
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

}
