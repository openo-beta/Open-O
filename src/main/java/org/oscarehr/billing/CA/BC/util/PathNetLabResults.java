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

package org.oscarehr.billing.CA.BC.util;

import org.oscarehr.billing.CA.BC.model.Hl7Msh;
import org.oscarehr.billing.CA.BC.model.Hl7Obr;
import org.oscarehr.billing.CA.BC.model.Hl7Orc;
import org.oscarehr.billing.CA.BC.model.Hl7Pid;
import org.oscarehr.common.model.PatientLabRouting;
import org.oscarehr.common.model.ProviderLabRoutingModel;

public class PathNetLabResults {
    private Hl7Msh hl7Msh;
    private Hl7Pid hl7Pid;
    private Hl7Orc hl7Orc;
    private Hl7Obr hl7Obr;
    private ProviderLabRoutingModel providerLabRouting;
    private PatientLabRouting patientLabRouting;
    private Long minResultStatus;

    public PathNetLabResults(Hl7Msh hl7Msh, Hl7Pid hl7Pid, Hl7Orc hl7Orc, Hl7Obr hl7Obr, ProviderLabRoutingModel providerLabRouting, String minResultStatus) {
        this.hl7Msh = hl7Msh;
        this.hl7Pid = hl7Pid;
        this.hl7Orc = hl7Orc;
        this.hl7Obr = hl7Obr;
        this.providerLabRouting = providerLabRouting;
        this.minResultStatus = Long.valueOf(minResultStatus);
    }

    public PathNetLabResults(Hl7Msh hl7Msh, Hl7Pid hl7Pid, Hl7Orc hl7Orc, Hl7Obr hl7Obr, PatientLabRouting patientLabRouting, String minResultStatus) {
        this.hl7Msh = hl7Msh;
        this.hl7Pid = hl7Pid;
        this.hl7Orc = hl7Orc;
        this.hl7Obr = hl7Obr;
        this.patientLabRouting = patientLabRouting;
        this.minResultStatus = Long.valueOf(minResultStatus);
    }

    public Hl7Msh getHl7Msh() {
        return hl7Msh;
    }

    public void setHl7Msh(Hl7Msh hl7Msh) {
        this.hl7Msh = hl7Msh;
    }

    public Hl7Pid getHl7Pid() {
        return hl7Pid;
    }

    public void setHl7Pid(Hl7Pid hl7Pid) {
        this.hl7Pid = hl7Pid;
    }

    public Hl7Orc getHl7Orc() {
        return hl7Orc;
    }

    public void setHl7Orc(Hl7Orc hl7Orc) {
        this.hl7Orc = hl7Orc;
    }

    public Hl7Obr getHl7Obr() {
        return hl7Obr;
    }

    public void setHl7Obr(Hl7Obr hl7Obr) {
        this.hl7Obr = hl7Obr;
    }

    public ProviderLabRoutingModel getProviderLabRouting() {
        return providerLabRouting;
    }

    public void setProviderLabRouting(ProviderLabRoutingModel providerLabRouting) {
        this.providerLabRouting = providerLabRouting;
    }

    public PatientLabRouting getPatientLabRouting() {
        return patientLabRouting;
    }

    public void setPatientLabRouting(PatientLabRouting patientLabRouting) {
        this.patientLabRouting = patientLabRouting;
    }

    public Long getMinResultStatus() {
        return minResultStatus;
    }

    public void setMinResultStatus(Long minResultStatus) {
        this.minResultStatus = minResultStatus;
    }
}
