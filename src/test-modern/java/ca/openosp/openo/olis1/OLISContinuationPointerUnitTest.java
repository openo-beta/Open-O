package ca.openosp.openo.olis1;

import ca.openosp.openo.commn.dao.OLISQueryLogDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the OLIS continuation-pointer paging helpers on {@link Driver}.
 *
 * <p>Quantity-limited OLIS queries return one page of orders plus a continuation
 * pointer in the DSC segment (Interface Spec §10.2.5.17); the EMR must resubmit the
 * same query with the pointer until OLIS stops returning one, then present the full
 * result set as a single message (CT Tracker req 1.1.3; CV16 / CV34). These tests
 * cover the pure pointer-extraction and page-merge helpers; the live multi-round-trip
 * loop is exercised only against the OLIS CST environment.</p>
 *
 * @since 2026-06-17
 * @see Driver#getContinuationPointer(String)
 * @see Driver#mergeOlisContinuationPages(String, java.util.List)
 */
@DisplayName("OLIS continuation-pointer paging helpers")
@Tag("unit")
@Tag("fast")
public class OLISContinuationPointerUnitTest extends OpenOUnitTestBase {

    /** Wraps raw HL7 in the unsigned OLIS response envelope (CDATA), as OLIS returns it. */
    private static String envelope(String hl7) {
        return "<Response xmlns=\"http://www.ssha.ca/2005/HIAL\"><Content><![CDATA["
                + hl7 + "]]></Content></Response>";
    }

    private static final String PAGE1_HL7 =
            "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20260617||ERP^Z01^ERP_R09|1|P|2.3.1\r"
                    + "MSA|AA|REQ-1\r"
                    + "PID|1||2000010534^^^ON^HC||Doe^Jane\r"
                    + "ORC|RE|A1\r"
                    + "OBR|1|||Electrolytes\r"
                    + "OBX|1|NM|2160-0^Creatinine||95|umol/L\r"
                    + "DSC|PTR-PAGE-2";

    private static final String PAGE2_HL7 =
            "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20260617||ERP^Z01^ERP_R09|2|P|2.3.1\r"
                    + "MSA|AA|REQ-2\r"
                    + "PID|1||2000010534^^^ON^HC||Doe^Jane\r"
                    + "ORC|RE|A2\r"
                    + "OBR|1|||Complete Blood Count\r"
                    + "OBX|1|NM|718-7^Hemoglobin||140|g/L";

    @BeforeEach
    void registerDriverStaticDeps() {
        // Driver's static fields resolve these DAOs via SpringUtils.getBean on class load.
        registerMock(OscarLogDao.class, mock(OscarLogDao.class));
        registerMock(OLISQueryLogDao.class, mock(OLISQueryLogDao.class));
    }

    @Test
    @DisplayName("should read the continuation pointer from DSC-1")
    void shouldReadContinuationPointerFromDsc() {
        assertThat(Driver.getContinuationPointer(envelope(PAGE1_HL7))).isEqualTo("PTR-PAGE-2");
    }

    @Test
    @DisplayName("should return null when the response carries no DSC segment")
    void shouldReturnNullWhenNoDscSegment() {
        assertThat(Driver.getContinuationPointer(envelope(PAGE2_HL7))).isNull();
    }

    @Test
    @DisplayName("should return null for an empty DSC pointer")
    void shouldReturnNullForEmptyPointer() {
        String hl7 = "MSH|^~\\&|OLIS\rMSA|AA|REQ-1\rORC|RE|A1\rDSC|";
        assertThat(Driver.getContinuationPointer(envelope(hl7))).isNull();
    }

    @Test
    @DisplayName("should unwrap HL7 from the CDATA Content envelope")
    void shouldUnwrapHl7FromEnvelope() {
        assertThat(Driver.extractHl7Content(envelope(PAGE2_HL7))).isEqualTo(PAGE2_HL7);
    }

    @Test
    @DisplayName("should merge continuation pages into one message keeping a single header")
    void shouldMergeContinuationPages() {
        String merged = Driver.mergeOlisContinuationPages(envelope(PAGE1_HL7), Collections.singletonList(PAGE2_HL7));
        String mergedHl7 = Driver.extractHl7Content(merged);

        // Both orders are present...
        assertThat(mergedHl7).contains("ORC|RE|A1").contains("ORC|RE|A2");
        assertThat(mergedHl7).contains("Creatinine").contains("Hemoglobin");
        // ...the continuation segment is gone (result set is now complete)...
        assertThat(mergedHl7).doesNotContain("DSC|");
        assertThat(Driver.getContinuationPointer(merged)).isNull();
        // ...and only the first page's header survives (no duplicate MSH/MSA/PID).
        assertThat(countOccurrences(mergedHl7, "MSH|")).isEqualTo(1);
        assertThat(countOccurrences(mergedHl7, "MSA|")).isEqualTo(1);
        assertThat(countOccurrences(mergedHl7, "PID|")).isEqualTo(1);
        // The envelope (with its CDATA wrapper) is preserved.
        assertThat(merged).startsWith("<Response").contains("<![CDATA[").endsWith("</Content></Response>");
    }

    @Test
    @DisplayName("should merge multiple continuation pages in order")
    void shouldMergeMultiplePages() {
        String page3 = "MSH|^~\\&|OLIS\rMSA|AA|REQ-3\rPID|1\rORC|RE|A3\rOBR|1|||Lipid Panel\rOBX|1|NM|2093-3^Cholesterol||5.1|mmol/L";
        String merged = Driver.mergeOlisContinuationPages(envelope(PAGE1_HL7), Arrays.asList(PAGE2_HL7, page3));
        String mergedHl7 = Driver.extractHl7Content(merged);

        assertThat(mergedHl7).contains("ORC|RE|A1").contains("ORC|RE|A2").contains("ORC|RE|A3");
        assertThat(countOccurrences(mergedHl7, "MSH|")).isEqualTo(1);
        assertThat(mergedHl7.indexOf("ORC|RE|A2")).isLessThan(mergedHl7.indexOf("ORC|RE|A3"));
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }
}
