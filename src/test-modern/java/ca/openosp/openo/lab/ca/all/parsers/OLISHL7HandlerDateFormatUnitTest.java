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
 * Regression test for the collection / specimen-received date-time formatting. A
 * date-only HL7 DTM (yyyyMMdd) must be formatted (yyyy-MM-dd) rather than dropped
 * (specimen received) or rendered raw (collection): the caller gates previously
 * required {@code length() > 13}, so anything shorter than a full timestamp was
 * left unformatted even though {@code formatDateTime} renders every DTM length.
 *
 * @since 2026-06-18
 */
@DisplayName("OLISHL7Handler collection/specimen date-time formatting")
@Tag("unit")
@Tag("fast")
@Tag("read")
public class OLISHL7HandlerDateFormatUnitTest extends OpenOUnitTestBase {

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

    /** OBR-7 (collection from) and OBR-14 (specimen received) injected with the given DTMs. */
    private static String message(String obr7, String obr14) {
        return String.join("\r",
                "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20240115120000||ORU^R01^ORU_R01|MSG1|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO",
                "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F",
                "ZPD|N|||||||||N",
                "OBR|1|||GLU^Glucose^L|||" + obr7 + "|||||||" + obr14 + "|SER^Serum^HL70487|||||||20240115110000|||F",
                "OBX|1|NM|14749-6^Glucose^LN||5.4|mmol/L|3.6-6.0|N|||F|||20240115083000");
    }

    @Test
    @DisplayName("formats a date-only collection time instead of leaving it raw")
    void shouldFormatDateOnlyCollectionTime() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(message("20240115", ""));

        String collection = handler.getCollectionDateTime(0);
        assertThat(collection).contains("2024-01-15");
        assertThat(collection).doesNotContain("20240115");
    }

    @Test
    @DisplayName("formats a date-only specimen-received time instead of dropping it")
    void shouldFormatDateOnlySpecimenReceived() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(message("20240115083000", "20240115"));

        String received = handler.getSpecimenReceivedDateTime();
        assertThat(received).contains("2024-01-15");
        assertThat(received).doesNotContain("20240115");
    }

    @Test
    @DisplayName("still formats a full timestamp collection time (no regression)")
    void shouldStillFormatFullTimestamp() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(message("20240115083000", "20240115093000"));

        assertThat(handler.getCollectionDateTime(0)).contains("2024-01-15 08:30:00");
    }
}
