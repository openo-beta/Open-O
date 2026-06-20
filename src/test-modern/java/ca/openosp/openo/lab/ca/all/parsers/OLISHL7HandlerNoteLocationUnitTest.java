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
 * Regression test for the note-location ({@code getNTELocation}) lookup. When the
 * requested OBR/OBX has no note of the queried level, the helper must report "no
 * NTE" rather than falling back to the last segment of the message — otherwise a
 * trailing NTE belonging to a different result is counted and surfaced as a comment
 * under the wrong test. This mirrors the same fix made to {@code getZBXLocation}.
 *
 * @since 2026-06-18
 */
@DisplayName("OLISHL7Handler note-location (NTE) binding")
@Tag("unit")
@Tag("fast")
@Tag("read")
public class OLISHL7HandlerNoteLocationUnitTest extends OpenOUnitTestBase {

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

    /**
     * A report whose only note (NTE) is attached to the OBX (an OBX-level comment) and
     * is the last segment of the message. The OBR itself carries no order-level note.
     */
    private static String messageWithObxNoteOnly() {
        return String.join("\r",
                "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20240115120000||ORU^R01^ORU_R01|MSG1|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO",
                "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F",
                "ZPD|N|||||||||N",
                "ORC|RE|||ACC1^^GDL:5552^OBI||CM",
                "OBR|1|||GLU^Glucose^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487|||||||20240115110000|||F",
                "ZBR|1|CHEM^Chemistry^OLIS|CHEM^Chemistry^OLIS||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L",
                "OBX|1|NM|14749-6^Glucose^LN||5.4|mmol/L|3.6-6.0|N|||F|||20240115083000",
                "NTE|1|L|This note belongs to the OBX, not to the OBR");
    }

    @Test
    @DisplayName("does not bind a trailing OBX note as an order-level (OBR) comment")
    void shouldNotBindTrailingNoteAsObrComment() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(messageWithObxNoteOnly());

        // The OBR has no order-level note: with the old last-segment fallback the
        // trailing OBX NTE was mis-counted here as 1.
        assertThat(handler.getOBRCommentCount(0)).isZero();
    }

    @Test
    @DisplayName("still counts a note that genuinely belongs to the OBX")
    void shouldStillCountLegitimateObxComment() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(messageWithObxNoteOnly());

        // The note legitimately attached to the OBX must still be found.
        assertThat(handler.getOBXCommentCount(0, 0)).isEqualTo(1);
    }
}
