package ca.openosp.openo.lab.ca.all.parsers;

import ca.openosp.openo.commn.dao.Hl7TextInfoDao;
import ca.openosp.openo.olis.dao.OLISFacilityDao;
import ca.openosp.openo.olis.dao.OLISMicroorganismNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISRequestNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISResultNomenclatureDao;
import ca.openosp.openo.olis.model.OLISFacility;
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
 * Unit test for {@link OLISHL7Handler}'s performing/reporting facility name
 * enrichment from the local OLIS facility catalog, including the hospital org type.
 *
 * <p>Each facility is carried by a colon-delimited identifier (OID:licence). The
 * handler resolves the name by OID+licence against a seeded {@link OLISFacility}
 * (disambiguating LAB/SCC/HOS, which share the licence space), falling back to the
 * raw ZBR name on a miss. The facility DAO is mocked, so the test is offline.</p>
 *
 * @since 2026-06-17
 */
@DisplayName("OLISHL7Handler facility name enrichment")
@Tag("unit")
@Tag("fast")
public class OLISHL7HandlerFacilityUnitTest extends OpenOUnitTestBase {

    private static final String LAB_OID = OLISFacility.OID_LAB;
    private static final String HOSP_OID = OLISFacility.OID_HOSP;

    private static String message(String zbr4Ident, String zbr4RawName, String zbr6Ident, String zbr6RawName) {
        return String.join("\r",
                "MSH|^~\\&|OLIS|2.16.840.1.113883.3.59.1:HL7-LAB|EMR|2.16.840.1.113883.3.59.1.2|20240115120000||ORU^R01^ORU_R01|OLISMSGFAC1|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO",
                "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F",
                "ZPD|N|||||||||N",
                "ORC|RE|||ACCFAC1^^GDL:5552^OBI||CM",
                "OBR|1|||GLU^Glucose Random^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487|||||||20240115110000|||F",
                // ZBR-4 = reporting (component 1 = raw name, component 6 subcomponent 2 = OID:licence)
                // ZBR-6 = performing
                "ZBR|1|HEM^Hematology^OLIS|HEM^Hematology^OLIS|" + zbr4RawName + "^^^^^GDL&" + zbr4Ident + "&ISO^L||"
                        + zbr6RawName + "^^^^^GDL&" + zbr6Ident + "&ISO^L",
                "OBX|1|NM|14749-6^Glucose [Moles/volume] in Serum or Plasma^LN||5.4|mmol/L^^UCUM|3.6-6.0|N|||F|||20240115083000");
    }

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

        OLISFacilityDao facilityDao = Mockito.mock(OLISFacilityDao.class);
        when(facilityDao.findByOidAndLicence(LAB_OID, "5552")).thenReturn(facility("5552", "Gamma-Dynacare"));
        when(facilityDao.findByOidAndLicence(HOSP_OID, "0153")).thenReturn(facility("0153", "Grace Hospital"));
        registerMock(OLISFacilityDao.class, facilityDao);
    }

    private static OLISFacility facility(String licence, String name) {
        OLISFacility f = new OLISFacility();
        f.setLicenceNumber(licence);
        f.setName(name);
        return f;
    }

    @Test
    @DisplayName("should resolve a licence-only LAB reporting facility to its catalog name")
    void shouldEnrichReportingFacilityNameFromCatalog() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(message(LAB_OID + ":5552", "", LAB_OID + ":9999", "RawPerf"));

        assertThat(handler.getReportingFacilityName()).isEqualTo("Gamma-Dynacare (Lab 5552)");
    }

    @Test
    @DisplayName("should fall back to the raw ZBR name when the licence has no catalog match")
    void shouldFallBackToRawNameOnCatalogMiss() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(message(LAB_OID + ":5552", "", LAB_OID + ":9999", "RawPerf"));

        assertThat(handler.getPerformingFacilityName()).isEqualTo("RawPerf (Lab 9999)");
    }

    @Test
    @DisplayName("should resolve a hospital (OID .59.3) reporting facility to its catalog name")
    void shouldEnrichHospitalFacilityName() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(message(HOSP_OID + ":0153", "", LAB_OID + ":9999", "RawPerf"));

        assertThat(handler.getReportingFacilityName()).isEqualTo("Grace Hospital (Hospital 0153)");
    }
}
