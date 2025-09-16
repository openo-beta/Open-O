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

package ca.openosp.openo.scratch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.openosp.openo.commn.dao.ScratchPadDao;
import ca.openosp.openo.commn.model.ScratchPad;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.util.ConversionUtils;

/**
 * Data access layer for clinical scratch pad functionality in the OpenO EMR system.
 *
 * <p>This class provides database operations for managing temporary clinical notes
 * that healthcare providers use during patient encounters. Scratch pads serve as
 * private, temporary note-taking areas where providers can jot down observations,
 * thoughts, or preliminary notes before formalizing them into the official medical record.</p>
 *
 * <p>The scratch pad system supports clinical workflow by:</p>
 * <ul>
 *   <li>Providing quick access to provider-specific temporary notes</li>
 *   <li>Maintaining version history for recovery and audit purposes</li>
 *   <li>Ensuring privacy by associating notes with specific provider numbers</li>
 *   <li>Supporting concurrent clinical sessions with unique timestamps</li>
 * </ul>
 *
 * <p>Database schema for scratch_pad table:</p>
 * <pre>
 * CREATE TABLE scratch_pad (
 *   id int(10) NOT NULL AUTO_INCREMENT PRIMARY KEY,
 *   provider_no varchar(6) NOT NULL,
 *   date_time datetime NOT NULL,
 *   scratch_text text,
 *   status boolean DEFAULT TRUE
 * );
 * </pre>
 *
 * <p>This is part of the clinical workflow enhancement tools that help providers
 * capture and organize information efficiently during busy clinical sessions.</p>
 *
 * @see Scratch2Action
 * @see ScratchTag
 * @see ca.openosp.openo.commn.model.ScratchPad
 * @see ca.openosp.openo.commn.dao.ScratchPadDao
 * @since September 2006
 */
public class ScratchData {

    /**
     * Creates a new instance of ScratchData for managing clinical scratch pad operations.
     *
     * <p>This no-argument constructor initializes the data access object for
     * scratch pad operations. The actual database connections are handled through
     * Spring dependency injection via SpringUtils.</p>
     */
    public ScratchData() {
    }

    /**
     * Retrieves all saved dates for scratch pad entries by a specific provider.
     *
     * <p>This method supports the clinical workflow by allowing providers to
     * see a chronological history of their scratch pad usage. This is useful
     * for tracking when clinical thoughts and observations were captured during
     * different patient encounters or clinical sessions.</p>
     *
     * @param providerNo String the unique identifier for the healthcare provider
     * @return List&lt;Object[]&gt; list of date objects representing scratch pad save timestamps
     * @see #getLatest(String)
     */
    public List<Object[]> getAllDates(String providerNo) {
        ScratchPadDao dao = SpringUtils.getBean(ScratchPadDao.class);
        return dao.findAllDatesByProviderNo(providerNo);
    }

    /**
     * Retrieves the most recent scratch pad entry for a specific provider.
     *
     * <p>This method is central to the scratch pad functionality, providing
     * quick access to the provider's most recent temporary notes. This supports
     * clinical workflow by allowing providers to immediately resume their
     * note-taking from where they left off, even across different browser
     * sessions or after system restarts.</p>
     *
     * <p>The returned map contains:</p>
     * <ul>
     *   <li>"id" - Database ID for version tracking and concurrent editing detection</li>
     *   <li>"text" - The actual scratch pad content for clinical notes</li>
     *   <li>"date" - Formatted timestamp of when the content was last saved</li>
     * </ul>
     *
     * @param providerNo String the unique identifier for the healthcare provider
     * @return Map&lt;String, String&gt; containing the latest scratch pad data, or null if none exists
     * @see #getAllDates(String)
     * @see #insert(String, String)
     */
    public Map<String, String> getLatest(String providerNo) {
        ScratchPadDao dao = SpringUtils.getBean(ScratchPadDao.class);
        ScratchPad scratchPad = dao.findByProviderNo(providerNo);
        if (scratchPad == null) return null;

        // Prepare response map with scratch pad details
        Map<String, String> retval = new HashMap<String, String>();
        retval.put("id", scratchPad.getId().toString());
        retval.put("text", scratchPad.getText());
        retval.put("date", ConversionUtils.toDateString(scratchPad.getDateTime()));
        return retval;
    }

    /**
     * Internal method to create a new scratch pad entry in the database.
     *
     * <p>This method handles the actual database insertion of scratch pad content.
     * It creates a new versioned entry with the current timestamp, supporting
     * the clinical workflow by maintaining a complete audit trail of provider
     * notes and observations.</p>
     *
     * <p>Each insertion creates a new version rather than updating existing content,
     * which supports:</p>
     * <ul>
     *   <li>Concurrent editing detection and conflict resolution</li>
     *   <li>Recovery of accidentally lost or modified content</li>
     *   <li>Clinical audit trails for quality assurance</li>
     * </ul>
     *
     * @param providerNo String the unique identifier for the healthcare provider
     * @param text String the clinical scratch pad content to be saved
     * @return String the database ID of the newly created scratch pad entry
     * @see #insert(String, String)
     */
    public String insert2(String providerNo, String text) {
        // Create new scratch pad entity with current timestamp
        ScratchPad scratchPad = new ScratchPad();
        scratchPad.setProviderNo(providerNo);
        scratchPad.setText(text);
        scratchPad.setDateTime(new Date());

        // Persist to database and return the new ID for version tracking
        ScratchPadDao dao = SpringUtils.getBean(ScratchPadDao.class);
        dao.persist(scratchPad);
        return scratchPad.getId().toString();
    }

    /**
     * Creates a new scratch pad entry for the specified provider.
     *
     * <p>This is the primary method for saving scratch pad content. It supports
     * the clinical workflow by allowing providers to quickly save temporary notes
     * and observations during patient encounters. Each save operation creates a
     * new versioned entry to support concurrent editing and content recovery.</p>
     *
     * <p>This method is typically called when:</p>
     * <ul>
     *   <li>A provider modifies their scratch pad content</li>
     *   <li>Auto-save functionality triggers during clinical documentation</li>
     *   <li>A provider manually saves their temporary clinical notes</li>
     * </ul>
     *
     * @param providerNo String the unique identifier for the healthcare provider
     * @param text String the clinical scratch pad content to be saved
     * @return String the database ID of the newly created scratch pad entry
     * @see #getLatest(String)
     * @see #insert2(String, String)
     */
    public String insert(String providerNo, String text) {
        return insert2(providerNo, text);
    }

}
