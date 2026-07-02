package ca.openosp.openo.webserv.rest.to.model;

import java.util.Date;

/**
 * Transfer object for a single DHDR (Digital Health Drug Repository) dispense record.
 *
 * <p>Fields map directly onto the FHIR R4 {@code MedicationDispense} bundle returned by the DHDR
 * EHR Service. The FHIR element each field is sourced from is noted inline. Per DHDR requirements
 * the information MUST be displayed exactly as received from the DHDR EHR Service.</p>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>Dispense Date maps to the FHIR element {@code whenPrepared}, NOT {@code whenHandedOver}
 *       (the latter is the pickup date rather than the dispense date).</li>
 *   <li>The EMR may receive a coded value for Dosage Form.</li>
 * </ul>
 *
 * @since 2026-07-02
 */
public class MedicationDispenseTo1 {
  private Date dispenseDate;            // [MedicationDispense.whenPrepared]
  private String genericName;           // [Medication.code.coding[2].display]
  private String brandName;             // [Medication.code.coding[1].display]
  private String dispensedDrugStrength; // [Medication.extension[1].valueString]
  private String drugDosageForm;        // (e.g., tablet, capsule, injection) [Medication.form.text]
  private String dispensedQuantity;     // [MedicationDispense.quantity.value] [MedicationDispense.quantity.unit]
  private String estimatedDaysSupply;   // [MedicationDispense.daysSupply.value]
  // Prescriber Information
  private String prescriberFirstname;   // [Practitioner.name.given]
  private String prescriberLastname;    // [Practitioner.name.family]
  private String prescriberPhoneNumber; // [Practitioner.telecom[1].value]
  // Pharmacy Information
  private String dispensingPharmacy;          // [Organization.name]
  private String dispensingPharmacyFaxNumber; // [Organization.telecom[2].value]

  public Date getDispenseDate() {
    return dispenseDate;
  }

  public void setDispenseDate(Date dispenseDate) {
    this.dispenseDate = dispenseDate;
  }

  public String getGenericName() {
    return genericName;
  }

  public void setGenericName(String genericName) {
    this.genericName = genericName;
  }

  public String getBrandName() {
    return brandName;
  }

  public void setBrandName(String brandName) {
    this.brandName = brandName;
  }

  public String getDispensedDrugStrength() {
    return dispensedDrugStrength;
  }

  public void setDispensedDrugStrength(String dispensedDrugStrength) {
    this.dispensedDrugStrength = dispensedDrugStrength;
  }

  public String getDrugDosageForm() {
    return drugDosageForm;
  }

  public void setDrugDosageForm(String drugDosageForm) {
    this.drugDosageForm = drugDosageForm;
  }

  public String getDispensedQuantity() {
    return dispensedQuantity;
  }

  public void setDispensedQuantity(String dispensedQuantity) {
    this.dispensedQuantity = dispensedQuantity;
  }

  public String getEstimatedDaysSupply() {
    return estimatedDaysSupply;
  }

  public void setEstimatedDaysSupply(String estimatedDaysSupply) {
    this.estimatedDaysSupply = estimatedDaysSupply;
  }

  public String getPrescriberFirstname() {
    return prescriberFirstname;
  }

  public void setPrescriberFirstname(String prescriberFirstname) {
    this.prescriberFirstname = prescriberFirstname;
  }

  public String getPrescriberLastname() {
    return prescriberLastname;
  }

  public void setPrescriberLastname(String prescriberLastname) {
    this.prescriberLastname = prescriberLastname;
  }

  public String getPrescriberPhoneNumber() {
    return prescriberPhoneNumber;
  }

  public void setPrescriberPhoneNumber(String prescriberPhoneNumber) {
    this.prescriberPhoneNumber = prescriberPhoneNumber;
  }

  public String getDispensingPharmacy() {
    return dispensingPharmacy;
  }

  public void setDispensingPharmacy(String dispensingPharmacy) {
    this.dispensingPharmacy = dispensingPharmacy;
  }

  public String getDispensingPharmacyFaxNumber() {
    return dispensingPharmacyFaxNumber;
  }

  public void setDispensingPharmacyFaxNumber(String dispensingPharmacyFaxNumber) {
    this.dispensingPharmacyFaxNumber = dispensingPharmacyFaxNumber;
  }
}
