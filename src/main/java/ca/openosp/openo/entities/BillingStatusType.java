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
 * Healthcare billing status type entity representing standardized billing status codes and their descriptions.
 * This entity encapsulates the various status codes used by provincial healthcare billing systems
 * (such as MSP in British Columbia) to track the lifecycle and processing state of billing claims.
 *
 * Each BillingStatusType record defines a specific status code with its display name and sort order,
 * enabling consistent status tracking across the billing system. Common statuses include submitted,
 * paid, rejected, under review, and various error conditions.
 *
 * @see Billingmaster
 * @see BillHistory
 * @since November 1, 2004
 */
public class BillingStatusType {

    /**
     * Single character provincial billing status code (e.g., 'O' for submitted, 'P' for paid)
     */
    private String billingstatus;

    /**
     * Human-readable description of the billing status for display in user interfaces
     */
    private String displayName;

    /**
     * Sort order number indicating the display sequence of this status in lists
     */
    private Integer sortOrder;

    /**
     * Extended display name providing additional detail about the billing status
     */
    private String displayNameExt;

    /**
     * Default constructor creating an empty BillingStatusType instance.
     * All fields will be initialized to their default values.
     */
    public BillingStatusType() {
    }

    /**
     * Sets the provincial billing status code.
     *
     * @param billingstatus String the single character status code
     */
    public void setBillingstatus(String billingstatus) {
        this.billingstatus = billingstatus;
    }

    /**
     * Sets the human-readable description of the billing status.
     *
     * @param displayName String the display name for user interfaces
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the sort order number for display sequence.
     *
     * @param sortOrder Integer the sort order (lower numbers appear first)
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Sets the extended display name providing additional status detail.
     *
     * @param displayNameExt String the extended display name
     */
    public void setDisplayNameExt(String displayNameExt) {
        this.displayNameExt = displayNameExt;
    }

    /**
     * Gets the provincial billing status code.
     *
     * @return String the single character status code
     */
    public String getBillingstatus() {
        return billingstatus;
    }

    /**
     * Gets the human-readable description of the billing status.
     *
     * @return String the display name for user interfaces
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the sort order number for display sequence.
     *
     * @return Integer the sort order (lower numbers appear first), may be null
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * Gets the extended display name providing additional status detail.
     *
     * @return String the extended display name, may be null
     */
    public String getDisplayNameExt() {
        return displayNameExt;
    }
}
