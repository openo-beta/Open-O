package ca.openosp.openo.integration.dhdr;

import static org.assertj.core.api.Assertions.assertThat;

import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import java.util.List;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the value-rendering helpers in {@link DHDRPrint}.
 *
 * <p>This class earned a test file. Seven DHDR defects were fixed on 2026-07-29/30 and five of them
 * were here, every one a cell that rendered the wrong thing rather than a crash: a phone number under
 * a fax heading, the four-character text "null" where a value was absent, a phantom frequency reading
 * "every  - ", an empty bracket where a licence was missing, and a date in a second format. None would
 * fail a smoke test, and none did - they were found by comparing printed output against a requirement's
 * element list.
 *
 * <p>So these tests assert the *rendered strings*, not that a method returns non-null. Each group
 * names the defect it guards, so a future change that reintroduces one fails here with a message that
 * explains itself.
 *
 * <p>The helpers are package-private for this reason, as {@link DHDRManager#fhirBirthDate} already is.
 * {@link OpenOUnitTestBase} is needed only because {@code DHDRPrint} resolves a
 * {@code DemographicManager} through {@code SpringUtils} in a field initialiser; none of the helpers
 * under test touch it.
 */
@Tag("unit")
@Tag("dhdr")
class DHDRPrintUnitTest extends OpenOUnitTestBase {

  private DHDRPrint print;

  @BeforeEach
  void createPrinter() {
    // DHDRPrint resolves this in a field initialiser, so it must be registered before construction
    // even though none of the helpers under test call it.
    createAndRegisterMock(DemographicManager.class);
    print = new DHDRPrint();
  }

  /** A dispense/prescription payload built from alternating key/value pairs. */
  private static JSONObject med(Object... keysAndValues) throws Exception {
    JSONObject med = new JSONObject();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      med.put((String) keysAndValues[i], keysAndValues[i + 1]);
    }
    return med;
  }

  @Nested
  @DisplayName("optText - absence must not render as data")
  class OptTextTests {

    @Test
    @DisplayName("should return an empty string when a serialised null arrives as the text \"null\"")
    void shouldReturnEmpty_whenValueIsStringifiedNull() throws Exception {
      // The EMR payload carries nullable boxed types; a serialised null read back through optString
      // surfaces as "null" and printed verbatim reads as a recorded value.
      assertThat(print.optText(med("refillQuantity", "null"), "refillQuantity")).isEmpty();
      assertThat(print.optText(med("refillQuantity", "NULL"), "refillQuantity")).isEmpty();
    }

    @Test
    @DisplayName("should return an empty string when the key is absent")
    void shouldReturnEmpty_whenKeyAbsent() throws Exception {
      assertThat(print.optText(med(), "nothingHere")).isEmpty();
    }

    @Test
    @DisplayName("should trim surrounding whitespace")
    void shouldTrim_whenValueIsPadded() throws Exception {
      assertThat(print.optText(med("strength", "  50  "), "strength")).isEqualTo("50");
    }

    @Test
    @DisplayName("should preserve a value that merely contains the word null")
    void shouldPreserve_whenValueOnlyContainsNull() throws Exception {
      assertThat(print.optText(med("frequency", "nullify daily"), "frequency"))
          .isEqualTo("nullify daily");
    }
  }

  @Nested
  @DisplayName("isMeaningful - falsy for zero, matching the screen's ng-if")
  class IsMeaningfulTests {

    @Test
    @DisplayName("should treat zero as absent, as ng-if does")
    void shouldTreatAsAbsent_whenValueIsZero() {
      // Treating "0" as present printed "0 (0 / 0 days)" where the screen showed "0".
      assertThat(print.isMeaningful("0")).isFalse();
      assertThat(print.isMeaningful("0.0")).isFalse();
      assertThat(print.isMeaningful(" 0 ")).isFalse();
    }

    @Test
    @DisplayName("should treat a non-zero number as present")
    void shouldTreatAsPresent_whenValueIsNonZero() {
      assertThat(print.isMeaningful("1")).isTrue();
      assertThat(print.isMeaningful("0.5")).isTrue();
    }

    @Test
    @DisplayName("should treat free text as present, since it carries information")
    void shouldTreatAsPresent_whenValueIsNotNumeric() {
      assertThat(print.isMeaningful("mg")).isTrue();
    }

    @Test
    @DisplayName("should treat null and blank as absent")
    void shouldTreatAsAbsent_whenValueIsNullOrBlank() {
      assertThat(print.isMeaningful(null)).isFalse();
      assertThat(print.isMeaningful("   ")).isFalse();
    }
  }

  @Nested
  @DisplayName("joinValueAndUnit - no stray separator when a part is missing")
  class JoinValueAndUnitTests {

    @Test
    @DisplayName("should join a value and its unit with a single space")
    void shouldJoin_whenBothPresent() {
      assertThat(print.joinValueAndUnit("50", "mg")).isEqualTo("50 mg");
    }

    @Test
    @DisplayName("should return the value alone when no unit is recorded")
    void shouldReturnValueAlone_whenUnitAbsent() {
      assertThat(print.joinValueAndUnit("50", "")).isEqualTo("50");
      assertThat(print.joinValueAndUnit("50", null)).isEqualTo("50");
    }

    @Test
    @DisplayName("should return the unit alone rather than a leading space")
    void shouldReturnUnitAlone_whenValueAbsent() {
      assertThat(print.joinValueAndUnit("", "mg")).isEqualTo("mg");
    }

    @Test
    @DisplayName("should return an empty string when neither part is recorded")
    void shouldReturnEmpty_whenBothAbsent() {
      assertThat(print.joinValueAndUnit(null, null)).isEmpty();
    }
  }

  @Nested
  @DisplayName("pharmacistName - every separator is conditional")
  class PharmacistNameTests {

    private JSONObject withLicence(String family, String given, String licence) throws Exception {
      JSONObject med = med();
      if (family != null) {
        med.put("pharmacistLastname", family);
      }
      if (given != null) {
        med.put("pharmacistFirstname", given);
      }
      if (licence != null) {
        med.put("pharmacistLicenceNumber", new JSONObject().put("value", licence));
      }
      return med;
    }

    @Test
    @DisplayName("should render surname, given name and licence when all three are present")
    void shouldRenderFully_whenAllPartsPresent() throws Exception {
      assertThat(print.pharmacistName(withLicence("Sway", "Ken", "200087")))
          .isEqualTo("Sway, Ken (200087)");
    }

    @Test
    @DisplayName("should omit the bracket entirely when no licence is on file")
    void shouldOmitBracket_whenLicenceAbsent() throws Exception {
      // The two pharmacy-service tables printed ", ()" for a pharmacist the viewer could not resolve,
      // while the detail print guarded it - one obligation, two renderings.
      assertThat(print.pharmacistName(withLicence("Sway", "Ken", null))).isEqualTo("Sway, Ken");
      assertThat(print.pharmacistName(withLicence(null, null, null))).isEmpty();
    }

    @Test
    @DisplayName("should not render a leading comma when no surname is recorded")
    void shouldOmitComma_whenSurnameAbsent() throws Exception {
      assertThat(print.pharmacistName(withLicence(null, "Ken", "200087")))
          .isEqualTo("Ken (200087)");
    }

    @Test
    @DisplayName("should render the surname alone when no given name is recorded")
    void shouldRenderSurnameAlone_whenGivenNameAbsent() throws Exception {
      assertThat(print.pharmacistName(withLicence("Sway", null, null))).isEqualTo("Sway");
    }

    @Test
    @DisplayName("should render the licence alone rather than an orphan bracket")
    void shouldRenderLicenceAlone_whenNoNameRecorded() throws Exception {
      assertThat(print.pharmacistName(withLicence(null, null, "200087"))).isEqualTo("200087");
    }

    @Test
    @DisplayName("should treat a blank licence value as absent")
    void shouldTreatAsAbsent_whenLicenceValueIsBlank() throws Exception {
      assertThat(print.pharmacistName(withLicence("Sway", "Ken", "   "))).isEqualTo("Sway, Ken");
    }
  }

  @Nested
  @DisplayName("displayDate - one format across the views (DHDR03.06)")
  class DisplayDateTests {

    @Test
    @DisplayName("should format epoch milliseconds as the screen's medium date")
    void shouldFormatMedium_whenValueIsEpochMillis() {
      // The EMR transfer objects carry rxDate as epoch millis, and printing it raw emitted the
      // millisecond number (#60).
      //
      // Derived from a local date rather than hardcoded, because displayDate formats in the JVM's
      // default zone - as the screen's Angular date filter does. A hardcoded UTC-midnight epoch
      // renders as the previous day in any zone behind UTC, which is what a first draft of this test
      // asserted wrongly. The zone dependency is correct behaviour, not a defect: only the EMR side
      // supplies epoch values and those come from local calendar dates, while the DHDR service
      // supplies ISO dates that never go through this branch.
      long localMidnight = java.time.LocalDate.of(2016, 6, 23)
          .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
      assertThat(print.displayDate(String.valueOf(localMidnight))).isEqualTo("Jun 23, 2016");
    }

    @Test
    @DisplayName("should format an ISO date as the screen's medium date")
    void shouldFormatMedium_whenValueIsIsoDate() {
      assertThat(print.displayDate("1984-08-08")).isEqualTo("Aug 8, 1984");
    }

    @Test
    @DisplayName("should take the date part of an ISO date-time")
    void shouldFormatMedium_whenValueIsIsoDateTime() {
      assertThat(print.displayDate("2019-10-02T11:45:17.000-04:00")).isEqualTo("Oct 2, 2019");
    }

    @Test
    @DisplayName("should return an empty string for an absent, blank or stringified-null value")
    void shouldReturnEmpty_whenValueIsAbsentOrNull() {
      assertThat(print.displayDate(null)).isEmpty();
      assertThat(print.displayDate("")).isEmpty();
      assertThat(print.displayDate("   ")).isEmpty();
      assertThat(print.displayDate("null")).isEmpty();
    }

    @Test
    @DisplayName("should return an unrecognised value unchanged rather than blanking it")
    void shouldReturnUnchanged_whenValueIsNotADate() {
      // An unrecognised value is still information; blanking it would hide data from the clinician.
      assertThat(print.displayDate("N/A")).isEqualTo("N/A");
      assertThat(print.displayDate("sometime in 2016")).isEqualTo("sometime in 2016");
    }
  }

  @Nested
  @DisplayName("frequencyText - absence must not read as a frequency")
  class FrequencyTextTests {

    @Test
    @DisplayName("should return an empty string when no dosage instruction was supplied")
    void shouldReturnEmpty_whenNoPartSupplied() throws Exception {
      // dosageInstruction is optional under the consumer profile and the IG's own pharmacy-service
      // example carries none; concatenating unconditionally printed " every  - ".
      assertThat(print.frequencyText(med())).isEmpty();
    }

    @Test
    @DisplayName("should compose the frequency when the parts are supplied")
    void shouldCompose_whenPartsSupplied() throws Exception {
      assertThat(print.frequencyText(
          med("frequency", "1", "period", "1", "periodMax", "2", "periodUnit", "d")))
          .isEqualTo("1 every 1 - 2 d");
    }

    @Test
    @DisplayName("should still compose when only some parts are supplied")
    void shouldCompose_whenOnlySomePartsSupplied() throws Exception {
      assertThat(print.frequencyText(med("frequency", "3", "periodUnit", "d")))
          .isEqualTo("3 every  -  d");
    }
  }

  @Nested
  @DisplayName("serviceTypeWithPin - the product identifier is optional")
  class ServiceTypeWithPinTests {

    @Test
    @DisplayName("should append the PIN when one is present")
    void shouldAppendPin_whenPresent() throws Exception {
      assertThat(print.serviceTypeWithPin(
          new JSONObject().put("display", "MedsCheck").put("code", "MEDSCHECK")))
          .isEqualTo("MedsCheck (PIN: MEDSCHECK)");
    }

    @Test
    @DisplayName("should render the display alone when no PIN is present")
    void shouldRenderDisplayAlone_whenPinAbsent() throws Exception {
      assertThat(print.serviceTypeWithPin(new JSONObject().put("display", "MedsCheck")))
          .isEqualTo("MedsCheck");
    }

    @Test
    @DisplayName("should render the PIN alone when there is no display text")
    void shouldRenderPinAlone_whenDisplayAbsent() throws Exception {
      assertThat(print.serviceTypeWithPin(new JSONObject().put("code", "MEDSCHECK")))
          .isEqualTo("PIN: MEDSCHECK");
    }

    @Test
    @DisplayName("should return an empty string when there is no brandName at all")
    void shouldReturnEmpty_whenBrandNameAbsent() {
      assertThat(print.serviceTypeWithPin(null)).isEmpty();
    }
  }

  @Nested
  @DisplayName("EMR prescription cells (DHDR05.02)")
  class EmrCellTests {

    @Test
    @DisplayName("should fall back generic then brand then custom for the medication name")
    void shouldFallBackInOrder_whenNamingTheMedication() throws Exception {
      assertThat(print.emrMedicationName(
          med("genericName", "ASA", "brandName", "Novasen", "customName", "Something")))
          .isEqualTo("ASA");
      assertThat(print.emrMedicationName(med("brandName", "Novasen", "customName", "Something")))
          .isEqualTo("Novasen");
      assertThat(print.emrMedicationName(med("customName", "Something"))).isEqualTo("Something");
      assertThat(print.emrMedicationName(med())).isEmpty();
    }

    @Test
    @DisplayName("should render a dose range when the maximum differs from the minimum")
    void shouldRenderRange_whenMaxDiffersFromMin() throws Exception {
      assertThat(print.emrDose(med("takeMin", "1", "takeMax", "2", "unit", "tab")))
          .isEqualTo("1 - 2 tab");
    }

    @Test
    @DisplayName("should render a single dose when minimum and maximum agree")
    void shouldRenderSingleDose_whenMinEqualsMax() throws Exception {
      assertThat(print.emrDose(med("takeMin", "1", "takeMax", "1", "unit", "tab")))
          .isEqualTo("1 tab");
    }

    @Test
    @DisplayName("should omit a dosage unit that merely repeats the strength unit")
    void shouldOmitUnit_whenItRepeatsTheStrengthUnit() throws Exception {
      // OpenO stores the dosage unit as free text and records often repeat the strength there, which
      // printed "50 mg" under Dosage beside "50 mg" under Strength and read as a second strength.
      assertThat(print.emrDose(med("takeMin", "50", "unit", "mg", "strengthUnit", "MG")))
          .isEqualTo("50");
    }

    @Test
    @DisplayName("should render nothing when the recorded dose is zero")
    void shouldRenderNothing_whenDoseIsZero() throws Exception {
      assertThat(print.emrDose(med("takeMin", "0", "unit", "tab"))).isEmpty();
    }

    @Test
    @DisplayName("should append the duration to the first fill quantity when one is recorded")
    void shouldAppendDuration_whenRecorded() throws Exception {
      assertThat(print.emrQuantityAndDuration(
          med("quantity", "30", "duration", "30", "durationUnit", "days")))
          .isEqualTo("30 / 30 days");
    }

    @Test
    @DisplayName("should render the quantity alone when the duration is absent or zero")
    void shouldRenderQuantityAlone_whenDurationAbsentOrZero() throws Exception {
      assertThat(print.emrQuantityAndDuration(med("quantity", "30"))).isEqualTo("30");
      assertThat(print.emrQuantityAndDuration(med("quantity", "30", "duration", "0")))
          .isEqualTo("30");
    }

    @Test
    @DisplayName("should bracket the refill detail after the repeat count")
    void shouldBracketRefillDetail_whenRecorded() throws Exception {
      assertThat(print.emrRefills(
          med("repeats", "3", "refillQuantity", "30", "refillDuration", "30")))
          .isEqualTo("3 (30 / 30 days)");
    }

    @Test
    @DisplayName("should render the repeat count alone rather than an empty bracket")
    void shouldRenderCountAlone_whenNoRefillDetail() throws Exception {
      assertThat(print.emrRefills(med("repeats", "3"))).isEqualTo("3");
      assertThat(print.emrRefills(med("repeats", "0"))).isEqualTo("0");
    }

    @Test
    @DisplayName("should not print the text null for an absent boxed refill value")
    void shouldNotPrintNull_whenRefillValuesAreSerialisedNulls() throws Exception {
      assertThat(print.emrRefills(
          med("repeats", "2", "refillQuantity", "null", "refillDuration", "null")))
          .isEqualTo("2");
    }
  }

  @Nested
  @DisplayName("licenceBody - licensing college by name, not by system URI")
  class LicenceBodyTests {

    @Test
    @DisplayName("should map a known licensing system to the college name")
    void shouldMapToCollegeName_whenSystemIsKnown() {
      assertThat(print.licenceBody(
          "https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-license-physician"))
          .isEqualTo("College of Physicians and Surgeons of Ontario");
      assertThat(print.licenceBody(
          "https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-license-pharmacist"))
          .isEqualTo("Ontario College of Pharmacists");
    }

    @Test
    @DisplayName("should return an empty string for an unknown or absent system")
    void shouldReturnEmpty_whenSystemUnknownOrAbsent() {
      assertThat(print.licenceBody("https://example.org/NamingSystem/made-up")).isEmpty();
      assertThat(print.licenceBody(null)).isEmpty();
      assertThat(print.licenceBody("")).isEmpty();
    }
  }

  @Nested
  @DisplayName("Ages and dates of birth")
  class AgeAndBirthDateTests {

    @Test
    @DisplayName("should compute an age from a full, partial or year-only birth date")
    void shouldComputeAge_whenBirthDateIsFullOrPartial() {
      assertThat(print.computeAge("1984-08-08")).isNotEmpty().containsOnlyDigits();
      assertThat(print.computeAge("1984-08")).isNotEmpty().containsOnlyDigits();
      assertThat(print.computeAge("1984")).isNotEmpty().containsOnlyDigits();
    }

    @Test
    @DisplayName("should return an empty string when the birth date cannot be parsed")
    void shouldReturnEmpty_whenBirthDateUnparseable() {
      // Blank rather than "N/A", so it matches the DOB cell it is derived from.
      assertThat(print.computeAge("")).isEmpty();
      assertThat(print.computeAge("N/A")).isEmpty();
      assertThat(print.computeAge("1984-13-45")).isEmpty();
    }

    @Test
    @DisplayName("should render an unpadded demographic birth date in the display format")
    void shouldRenderMedium_whenDemographicColumnsAreUnpadded() {
      // getBirthDayAsString() joins the three columns without padding and can yield 1984-8-8, which
      // no formatter here parses; the padding comes from DHDRManager.fhirBirthDate.
      Demographic demo = new Demographic();
      demo.setYearOfBirth("1984");
      demo.setMonthOfBirth("8");
      demo.setDateOfBirth("8");
      assertThat(print.emrBirthDate(demo)).isEqualTo("Aug 8, 1984");
    }

    @Test
    @DisplayName("should fall back to the raw joined value when the birth columns are unusable")
    void shouldFallBackToRaw_whenBirthColumnsUnusable() {
      Demographic demo = new Demographic();
      demo.setYearOfBirth("");
      demo.setMonthOfBirth("");
      demo.setDateOfBirth("");
      // Whatever getBirthDayAsString() produces, the point is that it is not silently blanked.
      assertThat(print.emrBirthDate(demo)).isEqualTo(demo.getBirthDayAsString());
    }
  }

  @Nested
  @DisplayName("Column lists - the refactor's single point of failure")
  class ColumnListTests {

    /**
     * These two assertions exist because collapsing the duplicated tables onto one column list moved
     * the risk rather than removing it: two hand-built copies can drift from each other, but one list
     * can be reordered or relabelled in a single edit that compiles and renders a plausible-looking
     * table. The labels and their order are the contract with the requirement's element list, so they
     * are asserted literally.
     */
    @Test
    @DisplayName("should declare the pharmacy service columns in DHDR07.01's order")
    void shouldDeclarePharmacyServiceColumns_inRequirementOrder() {
      assertThat(print.pharmacyServiceColumns().stream().map(DHDRPrint.Column::label).toList())
          .containsExactly(
              "Last Service Date",
              "Pickup Date",
              "Pharmacy Service Type",
              "Pharmacy Service Description",
              "Rx Number",
              "Therapeutic Class/Sub-class",
              "Pharmacy Name",
              "Pharmacist",
              "Pharmacy Fax");
    }

    @Test
    @DisplayName("should declare the EMR prescription columns in DHDR05.02's order")
    void shouldDeclareEmrColumns_inRequirementOrder() {
      assertThat(print.emrPrescriptionColumns().stream().map(DHDRPrint.Column::label).toList())
          .containsExactly(
              "Start Date",
              "Medication",
              "Strength",
              "Dosage",
              "Frequency",
              "Prescriber",
              "DIN",
              "Qty / Duration",
              "Refills");
    }

    @Test
    @DisplayName("should name the pharmacy fax column the fax, not the phone (DHDR07.01(h))")
    void shouldNameTheFaxColumnTheFax() throws Exception {
      // The Comparative print showed dispensingPharmacyPhoneNumber under a "Pharmacy #" heading while
      // the Summary print's copy of the same table was correct. Assert both halves: the label, and
      // that the extractor reads the fax field.
      List<DHDRPrint.Column> columns = print.pharmacyServiceColumns();
      DHDRPrint.Column fax = columns.get(columns.size() - 1);
      assertThat(fax.label()).isEqualTo("Pharmacy Fax");
      assertThat(fax.value().apply(med(
          "dispensingPharmacyFaxNumber", "416-652-4553",
          "dispensingPharmacyPhoneNumber", "416-539-5532")))
          .isEqualTo("416-652-4553");
    }

    @Test
    @DisplayName("should render every column without throwing on an empty event")
    void shouldRenderAllColumns_whenEventIsEmpty() throws Exception {
      // A sparse but legal record must not abort a print; every extractor has to tolerate absence.
      JSONObject empty = med();
      for (DHDRPrint.Column column : print.pharmacyServiceColumns()) {
        assertThat(column.value().apply(empty)).isNotNull();
      }
      for (DHDRPrint.Column column : print.emrPrescriptionColumns()) {
        assertThat(column.value().apply(empty)).isNotNull();
      }
    }
  }
}
