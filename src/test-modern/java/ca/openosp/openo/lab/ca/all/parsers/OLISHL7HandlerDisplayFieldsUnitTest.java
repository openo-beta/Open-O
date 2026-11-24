package ca.openosp.openo.lab.ca.all.parsers;

import ca.openosp.openo.commn.dao.Hl7TextInfoDao;
import ca.openosp.openo.olis.dao.OLISFacilityDao;
import ca.openosp.openo.olis.dao.OLISMicroorganismNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISRequestNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISResultNomenclatureDao;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Regression coverage for the OLIS display-sequence fields that were relocated within
 * the clinical-viewer layout but had no direct unit test: the order-level diagnosis
 * (DG1-3 bound to its OBR), the collector's comment (OBR-39-2), and the ancillary-order
 * flag (OBX-11="Z"). It also guards that a report carrying no ZBR segment still parses
 * cleanly — the sort-key lookup ({@code getZBR11}) logs a caught HL7Exception in that
 * case, and this test pins that the condition stays non-fatal.
 *
 * @since 2026-06-18
 */
@DisplayName("OLISHL7Handler display-sequence fields (diagnosis / collector's comment / ancillary)")
@Tag("unit")
@Tag("fast")
@Tag("read")
public class OLISHL7HandlerDisplayFieldsUnitTest extends OpenOUnitTestBase {

    @BeforeEach
    void registerMocks() {
        registerMock(Hl7TextInfoDao.class, Mockito.mock(Hl7TextInfoDao.class));
        OLISResultNomenclatureDao resultDao = Mockito.mock(OLISResultNomenclatureDao.class);
        when(resultDao.findByNameId(anyString())).thenReturn(null);
        registerMock(OLISResultNomenclatureDao.class, resultDao);
        OLISRequestNomenclatureDao requestDao = Mockito.mock(OLISRequestNomenclatureDao.class);
        when(requestDao.findByNameId(anyString())).thenReturn(null);
        registerMock(OLISRequestNomenclatureDao.class, requestDao);
        OLISMicroorganismNomenclatureDao microDao = Mockito.mock(OLISMicroorganismNomenclatureDao.class);
        when(microDao.findByMicroorganismCodes(any())).thenReturn(new java.util.HashMap<>());
        registerMock(OLISMicroorganismNomenclatureDao.class, microDao);
        registerMock(OLISFacilityDao.class, Mockito.mock(OLISFacilityDao.class));
    }

    private static final String MSH =
            "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20240115120000||ORU^R01^ORU_R01|MSG1|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO";
    private static final String PID =
            "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F";
    private static final String ZPD = "ZPD|N|||||||||N";
    private static final String ZBR =
            "ZBR|1|CHEM^Chemistry^OLIS|CHEM^Chemistry^OLIS||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L";
    /** OBR-39 (collector's comment, component 2) populated; status (OBR-25) = F. */
    private static final String OBR_WITH_COLLECTOR_COMMENT =
            "OBR|1|||GLU^Glucose^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487||||||||||F||||||||||||||^Patient was fasting prior to collection";
    /** Same OBR with no collector's comment field. */
    private static final String OBR_PLAIN =
            "OBR|1|||GLU^Glucose^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487||||||||||F";
    private static final String OBX_RESULT =
            "OBX|1|NM|14749-6^Glucose^LN||5.4|mmol/L|3.6-6.0|N|||F|||20240115083000";

    // ---- Diagnosis (DG1-3-2 bound to the OBR) -----------------------------------------

    @Test
    @DisplayName("returns the diagnosis text from DG1-3 bound to its OBR")
    void shouldReturnDiagnosisTextFromDg1BoundToObr() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(String.join("\r",
                MSH, PID, ZPD, OBR_PLAIN, ZBR,
                "DG1|1||250.00^Diabetes mellitus^I9", OBX_RESULT));

        assertThat(handler.getDiagnosis(0)).isEqualTo("Diabetes mellitus");
    }

    @Test
    @DisplayName("returns empty diagnosis when the report carries no DG1 segment")
    void shouldReturnEmptyDiagnosisWhenNoDg1() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(String.join("\r", MSH, PID, ZPD, OBR_PLAIN, ZBR, OBX_RESULT));

        assertThat(handler.getDiagnosis(0)).isEmpty();
    }

    // ---- Collector's comment (OBR-39-2) -----------------------------------------------

    @Test
    @DisplayName("returns the collector's comment from OBR-39 component 2")
    void shouldReturnCollectorsCommentFromObr39() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(String.join("\r", MSH, PID, ZPD, OBR_WITH_COLLECTOR_COMMENT, ZBR, OBX_RESULT));

        assertThat(handler.getCollectorsComment(0)).isEqualTo("Patient was fasting prior to collection");
    }

    @Test
    @DisplayName("returns empty collector's comment when OBR-39 is absent")
    void shouldReturnEmptyCollectorsCommentWhenAbsent() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(String.join("\r", MSH, PID, ZPD, OBR_PLAIN, ZBR, OBX_RESULT));

        assertThat(handler.getCollectorsComment(0)).isEmpty();
    }

    // ---- Ancillary order information (OBX-11 = "Z") -----------------------------------

    @Test
    @DisplayName("flags an OBX whose result status (OBX-11) is Z as ancillary")
    void shouldFlagObxAsAncillaryWhenStatusIsZ() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(String.join("\r", MSH, PID, ZPD, OBR_PLAIN, ZBR,
                // OBX-11 = Z -> ancillary order information (CT category 11)
                "OBX|1|ST|34555-3^Ordering provider^LN||Dr Smith||||||Z",
                OBX_RESULT.replace("OBX|1|", "OBX|2|")));

        assertThat(handler.isAncillary(0, 0)).isTrue();
    }

    @Test
    @DisplayName("does not flag a regular final result (OBX-11 = F) as ancillary")
    void shouldNotFlagRegularResultAsAncillary() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(String.join("\r", MSH, PID, ZPD, OBR_PLAIN, ZBR,
                "OBX|1|ST|34555-3^Ordering provider^LN||Dr Smith||||||Z",
                OBX_RESULT.replace("OBX|1|", "OBX|2|")));

        assertThat(handler.isAncillary(0, 1)).isFalse();
    }

    // ---- ZBR-absent robustness --------------------------------------------------------

    @Test
    @DisplayName("parses a report with no ZBR segment without failing (sort-key lookup stays non-fatal)")
    void shouldParseCleanlyWhenZbrSegmentAbsent() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(String.join("\r", MSH, PID, ZPD, OBR_PLAIN,
                "DG1|1||250.00^Diabetes mellitus^I9", OBX_RESULT));

        // init must complete and the request must remain addressable even though the
        // missing ZBR makes getZBR11 throw-and-log internally.
        assertThat(handler.getOBRCount()).isEqualTo(1);
        assertThat(handler.getMappedOBR(0)).isZero();
        assertThat(handler.getDiagnosis(0)).isEqualTo("Diabetes mellitus");
    }
}
