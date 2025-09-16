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
package ca.openosp.openo.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Patient appointment list container bean for healthcare scheduling displays.
 *
 * This XML-serializable bean aggregates patient appointment information for
 * display in healthcare scheduling interfaces. It provides a structured container
 * for organizing multiple patient appointments along with the appropriate display
 * template, enabling flexible presentation of appointment data across different
 * clinical workflows and user interfaces.
 *
 * <p>The bean supports XML marshaling/unmarshaling for integration with web
 * services and AJAX-based scheduling interfaces. This is particularly important
 * for healthcare applications that need to exchange appointment data between
 * different systems or provide real-time scheduling updates to clinical staff.</p>
 *
 * <p>Appointment lists are critical for clinical workflow management, helping
 * healthcare providers organize their daily schedules, manage patient flow,
 * and ensure appropriate allocation of clinical time. The template-based
 * approach allows for customized display formats based on provider preferences
 * or clinical setting requirements.</p>
 *
 * @since March 2012
 * @see ca.openosp.openo.web.PatientListApptItemBean
 */
@XmlRootElement
@XmlType(name = "", propOrder = {"template", "patients"})
public class PatientListApptBean implements Serializable {

    /** Display template path for rendering the patient appointment list */
    private String template = "patientlist/patientList1.jsp";

    /** Collection of patient appointment items for display */
    private List<PatientListApptItemBean> patients = new ArrayList<PatientListApptItemBean>();


    /**
     * Retrieves the display template path for the appointment list.
     *
     * @return String JSP template path for rendering the patient list
     */
    public String getTemplate() {
        return template;
    }


    /**
     * Sets the display template path for the appointment list.
     *
     * @param template String JSP template path for customized list rendering
     */
    public void setTemplate(String template) {
        this.template = template;
    }


    /**
     * Retrieves the collection of patient appointment items.
     *
     * @return List&lt;PatientListApptItemBean&gt; collection of patient appointments
     */
    public List<PatientListApptItemBean> getPatients() {
        return patients;
    }


    /**
     * Sets the collection of patient appointment items.
     *
     * @param patients List&lt;PatientListApptItemBean&gt; collection of patient appointments to display
     */
    public void setPatients(List<PatientListApptItemBean> patients) {
        this.patients = patients;
    }


}
