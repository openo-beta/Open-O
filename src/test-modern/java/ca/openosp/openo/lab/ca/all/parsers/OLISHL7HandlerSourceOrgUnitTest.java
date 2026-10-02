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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Regression test for the note source-organization (ZNT) lookups. A note (NTE) carries
 * its source organization in a ZNT segment that immediately follows it; when a note has
 * no trailing ZNT — in particular when the NTE is the last segment of the message — the
 * lookup must return "" rather than indexing past the segment array
 * ({@code ArrayIndexOutOfBoundsException}).
 *
 * @since 2026-06-18
 */
@DisplayName("OLISHL7Handler note source-organization (ZNT) bounds")
@Tag("unit")
@Tag("fast")
public class OLISHL7HandlerSourceOrgUnitTest extends OpenOUnitTestBase {

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

    /** A report whose order-level note (NTE) is the LAST segment — no ZNT follows it. */
    private static String messageWithTrailingNoteNoZnt() {
        return String.join("\r",
                "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20240115120000||ORU^R01^ORU_R01|MSG1|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO",
                "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F",
                "ZPD|N|||||||||N",
                "ORC|RE|||ACC1^^GDL:5552^OBI||CM",
                "OBR|1|||GLU^Glucose^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487|||||||20240115110000|||F",
                "ZBR|1|HEM^Hematology^OLIS|HEM^Hematology^OLIS||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L",
                "OBX|1|NM|14749-6^Glucose^LN||5.4|mmol/L|3.6-6.0|N|||F|||20240115083000",
                "NTE|1|L|Order level note with no trailing ZNT segment");
    }

    @Test
    @DisplayName("source-org lookups return empty (no throw) when the note has no trailing ZNT")
    void shouldReturnEmptyWhenNoTrailingZnt() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(messageWithTrailingNoteNoZnt());

        assertThatCode(() -> {
            assertThat(handler.getOBRSourceOrganization(0, 0)).isEmpty();
            assertThat(handler.getReportSourceOrganization(0)).isEmpty();
            assertThat(handler.getOBXSourceOrganization(0, 0, 0)).isEmpty();
        }).doesNotThrowAnyException();
    }
}
