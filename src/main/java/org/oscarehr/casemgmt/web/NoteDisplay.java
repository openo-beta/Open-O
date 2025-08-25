//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package org.oscarehr.casemgmt.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.oscarehr.casemgmt.model.CaseManagementNoteLink;

import oscar.oscarRx.data.RxPrescriptionData;

public interface NoteDisplay {
    Comparator<NoteDisplay> noteProviderComparator = new Comparator<NoteDisplay>() {
        public int compare(NoteDisplay note1, NoteDisplay note2) {
            if (note1 == null || note2 == null) {
                return 0;
            }

            return note1.getProviderName().compareTo(note2.getProviderName());
        }
    };

    Comparator<NoteDisplay> noteProgramComparator = new Comparator<NoteDisplay>() {
        public int compare(NoteDisplay note1, NoteDisplay note2) {
            if (note1 == null || note1.getProgramName() == null || note2 == null || note2.getProgramName() == null) {
                return 0;
            }
            return note1.getProgramName().compareTo(note2.getProgramName());
        }
    };

    Comparator<NoteDisplay> noteRoleComparator = new Comparator<NoteDisplay>() {
        public int compare(NoteDisplay note1, NoteDisplay note2) {
            if (note1 == null || note2 == null) {
                return 0;
            }
            return note1.getRoleName().compareTo(note2.getRoleName());
        }
    };

    Comparator<NoteDisplay> noteObservationDateComparator = new Comparator<NoteDisplay>() {
        public int compare(NoteDisplay note1, NoteDisplay note2) {
            if (note1 == null || note2 == null) {
                return 0;
            }

            if (note2.getObservationDate() == null || note1.getObservationDate() == null) {
                return 0;
            }

            return note2.getObservationDate().compareTo(note1.getObservationDate());
        }
    };

    Integer getNoteId();

    boolean isSigned();

    boolean isEditable();

    Date getObservationDate();

    String getRevision();

    Date getUpdateDate();

    String getProviderName();

    String getProviderNo();

    String getStatus();

    String getProgramName();

    String getLocation();

    String getRoleName();

    Integer getRemoteFacilityId();

    String getUuid();

    boolean getHasHistory();

    boolean isLocked();

    String getNote();

    boolean isDocument();

    boolean isRxAnnotation();

    boolean isEformData();

    boolean isEncounterForm();

    boolean isInvoice();

    boolean isTicklerNote();

    boolean isExternalNote();


    boolean isEmailNote();

    CaseManagementNoteLink getNoteLink();

    RxPrescriptionData.Prescription getRxFromAnnotation(CaseManagementNoteLink cmnl);

    String getEncounterType();

    ArrayList<String> getEditorNames();

    ArrayList<String> getIssueDescriptions();

    //not controlled by note attributes / business logic like "editable".
    //use this for a category of notes - like integrator, group notes, etc
    boolean isReadOnly();

    boolean isGroupNote();

    boolean isCpp();

    boolean containsIssue(String issueCode);

    String getEncounterTime();

    String getEncounterTransportationTime();

    Integer getAppointmentNo();
}
