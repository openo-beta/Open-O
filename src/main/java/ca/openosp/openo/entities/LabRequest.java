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
 * Laboratory request entity linking requests to HL7 OBR segments.
 *
 * This entity represents the relationship between laboratory requests and their
 * corresponding HL7 OBR (Observation Request) segments. It provides a bridge
 * between internal lab request management and standardized HL7 messaging.
 *
 * Key functionality includes:
 * - Linking internal lab requests to HL7 OBR message segments
 * - Supporting laboratory order tracking and management
 * - Enabling integration between EMR lab orders and HL7 messaging
 * - Facilitating laboratory information system (LIS) communication
 *
 * This entity supports:
 * - Laboratory order workflow management
 * - HL7 message processing and routing
 * - Integration with external laboratory systems
 * - Order status tracking and result correlation
 *
 * @see Hl7Obr for HL7 OBR segment details
 * @since October 11, 2004
 */
public class LabRequest {
    private int id;
    private int hl7ObrId;

    /**
     * Default constructor for laboratory request entity.
     * Initializes all fields to their default values.
     */
    public LabRequest() {
    }

    /**
     * Complete constructor for laboratory request entity.
     * Creates a fully initialized lab request with HL7 OBR linkage.
     *
     * @param id       int unique identifier for this lab request
     * @param hl7ObrId int identifier of the related HL7 OBR segment
     */
    public LabRequest(int id, int hl7ObrId) {
        this.id = id;
        this.hl7ObrId = hl7ObrId;
    }

    /**
     * Gets the unique laboratory request identifier.
     * This is the database primary key for this lab request record.
     *
     * @return int the unique lab request identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the HL7 OBR segment identifier.
     * Links this lab request to its corresponding HL7 OBR (Observation Request) segment.
     *
     * @return int the HL7 OBR segment identifier
     * @see Hl7Obr for OBR segment details
     */
    public int getHl7ObrId() {
        return hl7ObrId;
    }

    /**
     * Sets the unique laboratory request identifier.
     * This is the database primary key for this lab request record.
     *
     * @param id int the unique lab request identifier
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the HL7 OBR segment identifier.
     * Links this lab request to its corresponding HL7 OBR segment.
     *
     * @param hl7ObrId int the HL7 OBR segment identifier
     * @see Hl7Obr for OBR segment details
     */
    public void setHl7ObrId(int hl7ObrId) {
        this.hl7ObrId = hl7ObrId;
    }

}
