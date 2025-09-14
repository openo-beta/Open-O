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
 * Healthcare Common Order (HL7 ORC) segment entity.
 *
 * This entity represents the HL7 v2.x ORC (Common Order) segment, which contains information
 * that is common to all orders. The ORC segment provides order control and status information
 * and is typically paired with order-specific segments like OBR for laboratory orders.
 *
 * Key clinical information stored includes:
 * - Order control codes (NW=New order, CA=Cancel, etc.)
 * - Order numbers from both placer and filler systems
 * - Order status and response flags
 * - Provider information and entry details
 * - Transaction timestamps and verification data
 * - Order effective dates and timing information
 *
 * This entity follows HL7 v2.x messaging standards and is essential for order management
 * in healthcare information systems, providing order lifecycle tracking and control.
 *
 * @see <a href="http://www.hl7.org/implement/standards/product_brief.cfm?product_id=185">HL7 v2.x Standard</a>
 * @see Hl7Obr for related observation requests
 * @since November 1, 2004
 */
public class Hl7Orc {
    /**
     * auto_increment
     */
    private int orcId;
    private int pidId;
    private String orderControl;
    private String placerOrderNumber1;
    private String fillerOrderNumber;
    private String placerOrderNumber2;
    private String orderStatus;
    private String responseFlag;
    private String quantityTiming;
    private String parent;
    private String dateTimeOfTransaction;
    private String enteredBy;
    private String verifiedBy;
    private String orderingProvider;
    private String entererLocation;
    private String callbackPhoneNumber;
    private String orderEffectiveDateTime;
    private String orderControlCodeReason;
    private String enteringOrganization;
    private String enteringDevice;
    private String actionBy;

    /**
     * Default constructor for HL7 ORC (Common Order) segment entity.
     * Initializes all fields to their default values.
     */
    public Hl7Orc() {
    }

    /**
     * Complete constructor for HL7 ORC (Common Order) segment entity.
     * Creates a fully initialized ORC segment with all HL7 standard fields.
     *
     * @param orcId                  int unique identifier for this ORC record
     * @param pidId                  int patient identifier linking to PID segment
     * @param orderControl           String order control code (NW=New, CA=Cancel) (ORC.1)
     * @param placerOrderNumber1     String placer order number (ORC.2)
     * @param fillerOrderNumber      String filler order number (ORC.3)
     * @param placerOrderNumber2     String placer group number (ORC.4)
     * @param orderStatus            String order status (A=Active, C=Cancelled) (ORC.5)
     * @param responseFlag           String response flag (ORC.6)
     * @param quantityTiming         String quantity/timing information (ORC.7)
     * @param parent                 String parent order reference (ORC.8)
     * @param dateTimeOfTransaction  String transaction date/time (ORC.9)
     * @param enteredBy              String person who entered the order (ORC.10)
     * @param verifiedBy             String person who verified the order (ORC.11)
     * @param orderingProvider       String ordering provider information (ORC.12)
     * @param entererLocation        String location where order was entered (ORC.13)
     * @param callbackPhoneNumber    String callback phone number (ORC.14)
     * @param orderEffectiveDateTime String order effective date/time (ORC.15)
     * @param orderControlCodeReason String reason for order control code (ORC.16)
     * @param enteringOrganization   String entering organization (ORC.17)
     * @param enteringDevice         String entering device (ORC.18)
     * @param actionBy               String action by person/entity (ORC.19)
     */
    public Hl7Orc(int orcId, int pidId, String orderControl,
                  String placerOrderNumber1, String fillerOrderNumber,
                  String placerOrderNumber2, String orderStatus,
                  String responseFlag, String quantityTiming, String parent,
                  String dateTimeOfTransaction, String enteredBy,
                  String verifiedBy, String orderingProvider,
                  String entererLocation, String callbackPhoneNumber,
                  String orderEffectiveDateTime,
                  String orderControlCodeReason, String enteringOrganization,
                  String enteringDevice, String actionBy) {
        this.orcId = orcId;
        this.pidId = pidId;
        this.orderControl = orderControl;
        this.placerOrderNumber1 = placerOrderNumber1;
        this.fillerOrderNumber = fillerOrderNumber;
        this.placerOrderNumber2 = placerOrderNumber2;
        this.orderStatus = orderStatus;
        this.responseFlag = responseFlag;
        this.quantityTiming = quantityTiming;
        this.parent = parent;
        this.dateTimeOfTransaction = dateTimeOfTransaction;
        this.enteredBy = enteredBy;
        this.verifiedBy = verifiedBy;
        this.orderingProvider = orderingProvider;
        this.entererLocation = entererLocation;
        this.callbackPhoneNumber = callbackPhoneNumber;
        this.orderEffectiveDateTime = orderEffectiveDateTime;
        this.orderControlCodeReason = orderControlCodeReason;
        this.enteringOrganization = enteringOrganization;
        this.enteringDevice = enteringDevice;
        this.actionBy = actionBy;
    }

    /**
     * Gets the unique ORC record identifier.
     * This is the database primary key for this ORC segment record.
     *
     * @return int the unique ORC record identifier
     */
    public int getOrcId() {
        return orcId;
    }

    /**
     * Gets the patient identifier linking this ORC to the corresponding PID segment.
     * This creates the relationship between the common order and the patient.
     *
     * @return int the patient identifier from the related PID segment
     */
    public int getPidId() {
        return pidId;
    }

    /**
     * Gets the order control code (HL7 field ORC.1).
     * Specifies the action to be taken: NW=New order, CA=Cancel order, OC=Order changed.
     *
     * @return String the order control code, empty string if null
     */
    public String getOrderControl() {
        return (orderControl != null ? orderControl : "");
    }

    /**
     * Gets the placer order number (HL7 field ORC.2).
     * The order number assigned by the entity placing the order (typically the EMR).
     *
     * @return String the placer order number, empty string if null
     */
    public String getPlacerOrderNumber1() {
        return (placerOrderNumber1 != null ? placerOrderNumber1 : "");
    }

    /**
     * Gets the filler order number (HL7 field ORC.3).
     * The order number assigned by the entity fulfilling the order (typically the lab).
     *
     * @return String the filler order number, empty string if null
     */
    public String getFillerOrderNumber() {
        return (fillerOrderNumber != null ? fillerOrderNumber : "");
    }

    /**
     * Gets the placerOrderNumber2
     *
     * @return String placerOrderNumber2
     */
    public String getPlacerOrderNumber2() {
        return (placerOrderNumber2 != null ? placerOrderNumber2 : "");
    }

    /**
     * Gets the order status (HL7 field ORC.5).
     * Current status of the order: A=Active, C=Cancelled, S=Suspended, etc.
     *
     * @return String the order status code, empty string if null
     */
    public String getOrderStatus() {
        return (orderStatus != null ? orderStatus : "");
    }

    /**
     * Gets the responseFlag
     *
     * @return String responseFlag
     */
    public String getResponseFlag() {
        return (responseFlag != null ? responseFlag : "");
    }

    /**
     * Gets the quantityTiming
     *
     * @return String quantityTiming
     */
    public String getQuantityTiming() {
        return (quantityTiming != null ? quantityTiming : "");
    }

    /**
     * Gets the parent
     *
     * @return String parent
     */
    public String getParent() {
        return (parent != null ? parent : "");
    }

    /**
     * Gets the transaction date/time (HL7 field ORC.9).
     * The date and time when this transaction was processed.
     *
     * @return String the transaction date/time, may be null
     */
    public String getDateTimeOfTransaction() {
        return dateTimeOfTransaction;
    }

    /**
     * Gets the enteredBy
     *
     * @return String enteredBy
     */
    public String getEnteredBy() {
        return (enteredBy != null ? enteredBy : "");
    }

    /**
     * Gets the verifiedBy
     *
     * @return String verifiedBy
     */
    public String getVerifiedBy() {
        return (verifiedBy != null ? verifiedBy : "");
    }

    /**
     * Gets the orderingProvider
     *
     * @return String orderingProvider
     */
    public String getOrderingProvider() {
        return (orderingProvider != null ? orderingProvider : "");
    }

    /**
     * Gets the entererLocation
     *
     * @return String entererLocation
     */
    public String getEntererLocation() {
        return (entererLocation != null ? entererLocation : "");
    }

    /**
     * Gets the callbackPhoneNumber
     *
     * @return String callbackPhoneNumber
     */
    public String getCallbackPhoneNumber() {
        return (callbackPhoneNumber != null ? callbackPhoneNumber : "");
    }

    /**
     * Gets the orderEffectiveDateTime
     *
     * @return String orderEffectiveDateTime
     */
    public String getOrderEffectiveDateTime() {
        return orderEffectiveDateTime;
    }

    /**
     * Gets the orderControlCodeReason
     *
     * @return String orderControlCodeReason
     */
    public String getOrderControlCodeReason() {
        return (orderControlCodeReason != null ? orderControlCodeReason : "");
    }

    /**
     * Gets the enteringOrganization
     *
     * @return String enteringOrganization
     */
    public String getEnteringOrganization() {
        return (enteringOrganization != null ? enteringOrganization : "");
    }

    /**
     * Gets the enteringDevice
     *
     * @return String enteringDevice
     */
    public String getEnteringDevice() {
        return (enteringDevice != null ? enteringDevice : "");
    }

    /**
     * Gets the actionBy
     *
     * @return String actionBy
     */
    public String getActionBy() {
        return (actionBy != null ? actionBy : "");
    }

    /**
     * Sets the orcId
     *
     * @param orcId int
     */
    public void setOrcId(int orcId) {
        this.orcId = orcId;
    }

    /**
     * Sets the pidId
     *
     * @param pidId int
     */
    public void setPidId(int pidId) {
        this.pidId = pidId;
    }

    /**
     * Sets the orderControl
     *
     * @param orderControl String
     */
    public void setOrderControl(String orderControl) {
        this.orderControl = orderControl;
    }

    /**
     * Sets the placerOrderNumber1
     *
     * @param placerOrderNumber1 String
     */
    public void setPlacerOrderNumber1(String placerOrderNumber1) {
        this.placerOrderNumber1 = placerOrderNumber1;
    }

    /**
     * Sets the fillerOrderNumber
     *
     * @param fillerOrderNumber String
     */
    public void setFillerOrderNumber(String fillerOrderNumber) {
        this.fillerOrderNumber = fillerOrderNumber;
    }

    /**
     * Sets the placerOrderNumber2
     *
     * @param placerOrderNumber2 String
     */
    public void setPlacerOrderNumber2(String placerOrderNumber2) {
        this.placerOrderNumber2 = placerOrderNumber2;
    }

    /**
     * Sets the orderStatus
     *
     * @param orderStatus String
     */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * Sets the responseFlag
     *
     * @param responseFlag String
     */
    public void setResponseFlag(String responseFlag) {
        this.responseFlag = responseFlag;
    }

    /**
     * Sets the quantityTiming
     *
     * @param quantityTiming String
     */
    public void setQuantityTiming(String quantityTiming) {
        this.quantityTiming = quantityTiming;
    }

    /**
     * Sets the parent
     *
     * @param parent String
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * Sets the dateTimeOfTransaction
     *
     * @param dateTimeOfTransaction String
     */
    public void setDateTimeOfTransaction(String dateTimeOfTransaction) {
        this.dateTimeOfTransaction = dateTimeOfTransaction;
    }

    /**
     * Sets the enteredBy
     *
     * @param enteredBy String
     */
    public void setEnteredBy(String enteredBy) {
        this.enteredBy = enteredBy;
    }

    /**
     * Sets the verifiedBy
     *
     * @param verifiedBy String
     */
    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    /**
     * Sets the orderingProvider
     *
     * @param orderingProvider String
     */
    public void setOrderingProvider(String orderingProvider) {
        this.orderingProvider = orderingProvider;
    }

    /**
     * Sets the entererLocation
     *
     * @param entererLocation String
     */
    public void setEntererLocation(String entererLocation) {
        this.entererLocation = entererLocation;
    }

    /**
     * Sets the callbackPhoneNumber
     *
     * @param callbackPhoneNumber String
     */
    public void setCallbackPhoneNumber(String callbackPhoneNumber) {
        this.callbackPhoneNumber = callbackPhoneNumber;
    }

    /**
     * Sets the orderEffectiveDateTime
     *
     * @param orderEffectiveDateTime String
     */
    public void setOrderEffectiveDateTime(String
                                                  orderEffectiveDateTime) {
        this.orderEffectiveDateTime = orderEffectiveDateTime;
    }

    /**
     * Sets the orderControlCodeReason
     *
     * @param orderControlCodeReason String
     */
    public void setOrderControlCodeReason(String orderControlCodeReason) {
        this.orderControlCodeReason = orderControlCodeReason;
    }

    /**
     * Sets the enteringOrganization
     *
     * @param enteringOrganization String
     */
    public void setEnteringOrganization(String enteringOrganization) {
        this.enteringOrganization = enteringOrganization;
    }

    /**
     * Sets the enteringDevice
     *
     * @param enteringDevice String
     */
    public void setEnteringDevice(String enteringDevice) {
        this.enteringDevice = enteringDevice;
    }

    /**
     * Sets the actionBy
     *
     * @param actionBy String
     */
    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }
}
