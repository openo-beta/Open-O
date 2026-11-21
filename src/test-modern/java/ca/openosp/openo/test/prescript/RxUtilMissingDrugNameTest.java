package ca.openosp.openo.test.prescript;

import ca.openosp.openo.prescript.data.RxPrescriptionData;
import ca.openosp.openo.prescript.util.RxUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RxUtil#isMissingDrugName(RxPrescriptionData.Prescription, String)}, the
 * save-side guard that stops a staged medication from being saved without a name.
 *
 * <p>A custom drug saved with a blank name is stored with a null custom name and regional
 * identifier, which made the Timeline Drug Profile fail with a 500. Custom notes never have a
 * name and must stay exempt.</p>
 *
 * <p>The helper is static and {@link RxUtil} does no Spring lookups when it loads, so no container
 * is needed.</p>
 *
 * @since 2026-11-20
 */
@Tag("unit")
@Tag("rx")
@DisplayName("RxUtil.isMissingDrugName")
class RxUtilMissingDrugNameTest {

    /**
     * Builds a staged prescription that is or is not a custom note.
     *
     * @param customNote boolean whether the prescription is a custom note
     * @return RxPrescriptionData.Prescription a new staged prescription
     */
    private RxPrescriptionData.Prescription stagedRx(boolean customNote) {
        RxPrescriptionData.Prescription rx = new RxPrescriptionData.Prescription(0, "999998", 1);
        rx.setCustomNote(customNote);
        return rx;
    }

    @Test
    @DisplayName("should report a missing name when the submitted name is empty")
    void shouldReportMissingName_whenNameIsEmpty() {
        assertThat(RxUtil.isMissingDrugName(stagedRx(false), "")).isTrue();
    }

    @Test
    @DisplayName("should report a missing name when the submitted name is only whitespace")
    void shouldReportMissingName_whenNameIsWhitespace() {
        assertThat(RxUtil.isMissingDrugName(stagedRx(false), "   ")).isTrue();
    }

    @Test
    @DisplayName("should report a missing name when no name is submitted")
    void shouldReportMissingName_whenNameIsNull() {
        assertThat(RxUtil.isMissingDrugName(stagedRx(false), null)).isTrue();
    }

    @Test
    @DisplayName("should report a missing name when the submitted name is the literal null in any case")
    void shouldReportMissingName_whenNameIsLiteralNull() {
        assertThat(RxUtil.isMissingDrugName(stagedRx(false), "null")).isTrue();
        assertThat(RxUtil.isMissingDrugName(stagedRx(false), " NULL ")).isTrue();
    }

    @Test
    @DisplayName("should accept a medication when a name is submitted")
    void shouldAcceptMedication_whenNameIsPresent() {
        assertThat(RxUtil.isMissingDrugName(stagedRx(false), "Amoxicillin 500 mg")).isFalse();
    }

    @Test
    @DisplayName("should exempt a custom note when its name is blank")
    void shouldExemptCustomNote_whenNameIsBlank() {
        assertThat(RxUtil.isMissingDrugName(stagedRx(true), "")).isFalse();
    }

    @Test
    @DisplayName("should not report a missing name when the medication is not in the stash")
    void shouldNotReportMissingName_whenRxIsNull() {
        assertThat(RxUtil.isMissingDrugName(null, "")).isFalse();
    }
}
