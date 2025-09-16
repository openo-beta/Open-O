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
 * Healthcare HL7 message entity representing structured healthcare information exchange messages.
 * This entity encapsulates the data from the hl7_message table, which stores HL7 (Health Level 7)
 * formatted messages received from external healthcare systems such as laboratories, imaging centers,
 * and other healthcare providers.
 *
 * HL7 messages are the standard format for exchanging healthcare information between different
 * systems and typically contain patient data, test results, reports, and other clinical information.
 * Each message is timestamped and may include additional notes or processing information.
 *
 * @see Hl7Link
 * @since November 1, 2004
 */
public class Hl7Message {
    /**
     * Auto-increment unique identifier for this HL7 message record
     */
    private int messageId;

    /**
     * Date and time when this HL7 message was received or processed
     */
    private String dateTime;

    /**
     * Additional notes or comments related to this HL7 message processing
     */
    private String notes;

    /**
     * Default constructor creating an empty Hl7Message instance.
     * All fields will be initialized to their default values.
     */
    public Hl7Message() {
    }

    /**
     * Full constructor creating an Hl7Message instance with all field values.
     *
     * @param messageId int the unique HL7 message ID
     * @param dateTime  String the date and time when message was received
     * @param notes     String additional notes or processing comments
     */
    public Hl7Message(int messageId, String dateTime, String notes) {
        this.messageId = messageId;
        this.dateTime = dateTime;
        this.notes = notes;
    }

    /**
     * Gets the unique identifier for this HL7 message record.
     *
     * @return int the auto-increment HL7 message ID
     */
    public int getMessageId() {
        return messageId;
    }

    /**
     * Gets the date and time when this HL7 message was received or processed.
     *
     * @return String the message timestamp
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Gets additional notes or comments related to this HL7 message.
     *
     * @return String the processing notes, never null (empty string if null)
     */
    public String getNotes() {
        return (notes != null ? notes : "");
    }

    /**
     * Sets the unique identifier for this HL7 message record.
     *
     * @param messageId int the HL7 message ID
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    /**
     * Sets the date and time when this HL7 message was received or processed.
     *
     * @param dateTime String the message timestamp
     */
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Sets additional notes or comments related to this HL7 message.
     *
     * @param notes String the processing notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

}
