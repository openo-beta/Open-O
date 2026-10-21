/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.integration.dhir;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImmunizationRecommendationsHandler {

  private List<ca.openosp.openo.integration.dhir.ImmunizationRecommendation> recs = new ArrayList<>();
  private Date date;

  public ImmunizationRecommendationsHandler(ImmunizationRecommendation recommendation) {
    date = recommendation.getDate();
    for (ImmunizationRecommendationRecommendationComponent comp : recommendation.getRecommendation()) {
      ca.openosp.openo.integration.dhir.ImmunizationRecommendation rec = new ca.openosp.openo.integration.dhir.ImmunizationRecommendation();
      if (comp.getVaccineCode() != null) {
        for (CodeableConcept cc : comp.getVaccineCode()) {
          for (Coding coding : cc.getCoding()) {
            rec.getCodes().add(
                new ca.openosp.openo.integration.dhir.Coding(coding.getSystem(), coding.getCode(),
                    coding.getDisplay()));
          }
        }
      }
      if (comp.getTargetDisease() != null) {
        CodeableConcept cc = comp.getTargetDisease();
        if (cc.getCodingFirstRep() != null) {
          rec.setTargetDisease(cc.getCodingFirstRep().getDisplay());
        }
      }

      if (comp.getForecastStatus() != null) {
        CodeableConcept cc = comp.getForecastStatus();
        if (cc.getCodingFirstRep() != null) {
          for (Coding coding : cc.getCoding()) {
            rec.setForecastStatus(
                new ca.openosp.openo.integration.dhir.Coding(coding.getSystem(), coding.getCode(),
                    coding.getDisplay()));

          }
        }
      }
      if (comp.getDateCriterionFirstRep() != null) {
        for (ImmunizationRecommendationRecommendationDateCriterionComponent cc : comp.getDateCriterion()) {
          Date date = cc.getValue();
          rec.setDate(date);
        }
      }
      recs.add(rec);
    }
  }

  public List<ca.openosp.openo.integration.dhir.ImmunizationRecommendation> getRecs() {
    return recs;
  }

  public void setRecs(List<ca.openosp.openo.integration.dhir.ImmunizationRecommendation> recs) {
    this.recs = recs;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
