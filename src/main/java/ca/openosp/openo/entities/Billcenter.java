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
 * Healthcare billing center entity representing organizational units for billing management.
 * This entity encapsulates the data from the billcenter table, which defines billing centers
 * or cost centers used for organizing and categorizing billing activities within healthcare facilities.
 *
 * Billing centers are used to group billing activities by department, location, or other
 * organizational criteria, facilitating financial reporting and billing management.
 *
 * @see Billactivity
 * @see Billingmaster
 * @since November 1, 2004
 */
public class Billcenter {
    /**
     * Unique code identifying the billing center
     */
    private String billcenterCode;

    /**
     * Descriptive name of the billing center
     */
    private String billcenterDesc;

    /**
     * Default constructor creating an empty Billcenter instance.
     * All fields will be initialized to their default values.
     */
    public Billcenter() {
    }

    /**
     * Full constructor creating a Billcenter instance with specified code and description.
     *
     * @param billcenterCode String the unique billing center code
     * @param billcenterDesc String the descriptive name of the billing center
     */
    public Billcenter(String billcenterCode, String billcenterDesc) {
        this.billcenterCode = billcenterCode;
        this.billcenterDesc = billcenterDesc;
    }

    /**
     * Gets the unique code identifying the billing center.
     *
     * @return String the billing center code, never null (empty string if null)
     */
    public String getBillcenterCode() {
        return (billcenterCode != null ? billcenterCode : "");
    }

    /**
     * Gets the descriptive name of the billing center.
     *
     * @return String the billing center description, never null (empty string if null)
     */
    public String getBillcenterDesc() {
        return (billcenterDesc != null ? billcenterDesc : "");
    }

    /**
     * Sets the unique code identifying the billing center.
     *
     * @param billcenterCode String the billing center code
     */
    public void setBillcenterCode(String billcenterCode) {
        this.billcenterCode = billcenterCode;
    }

    /**
     * Sets the descriptive name of the billing center.
     *
     * @param billcenterDesc String the billing center description
     */
    public void setBillcenterDesc(String billcenterDesc) {
        this.billcenterDesc = billcenterDesc;
    }
}
