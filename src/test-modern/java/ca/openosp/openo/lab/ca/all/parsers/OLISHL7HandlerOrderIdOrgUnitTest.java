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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Order ID source-organization display (CT 5.2). The placer group number (ORC-4-1)
 * is shown with its associated org name, type, and id. When the message does not
 * carry the org name, it must be resolved from the OLIS Lab/SCC facility catalog —
 * and when it cannot be resolved at all, the report must show only the type/id and
 * never the literal string "null" (the prior behaviour formatted a null org name
 * straight into the output).
 *
 * @since 2026-06-18
 */
@DisplayName("OLISHL7Handler Order ID source-organization (CT 5.2)")
@Tag("unit")
@Tag("fast")
public class OLISHL7HandlerOrderIdOrgUnitTest extends OpenOUnitTestBase {

    private static final String LAB_OID = "2.16.840.1.113883.3.59.1";

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

        // Facility catalog: licence 5552 resolves to a name; everything else misses.
        OLISFacilityDao facilityDao = Mockito.mock(OLISFacilityDao.class);
        OLISFacility gdl = new OLISFacility();
        gdl.setName("Gamma-Dynacare");
        when(facilityDao.findByOidAndLicence(anyString(), anyString())).thenReturn(null);
        when(facilityDao.findByLicenceNumber(eq("5552"))).thenReturn(gdl);
        registerMock(OLISFacilityDao.class, facilityDao);
    }

    private static String messageWithOrderIdOrg(String orc43) {
        return String.join("\r",
                "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20240115120000||ORU^R01^ORU_R01|MSG1|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO",
                "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F",
                "ZPD|N|||||||||N",
                "ORC|RE|||ACC1^^" + orc43 + "^OBI||CM",
                "OBR|1|||GLU^Glucose^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487|||||||20240115110000|||F",
                "ZBR|1|CHEM^Chemistry^OLIS|CHEM^Chemistry^OLIS||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L",
                "OBX|1|NM|14749-6^Glucose^LN||5.4|mmol/L|3.6-6.0|N|||F|||20240115083000");
    }

    @Test
    @DisplayName("resolves the org name from the facility catalog when the message omits it")
    void shouldResolveOrgNameFromCatalog() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(messageWithOrderIdOrg(LAB_OID + ":5552"));

        assertThat(handler.getAccessionNumSourceOrganization()).isEqualTo("Gamma-Dynacare (Lab 5552)");
    }

    @Test
    @DisplayName("shows only the type/id (never literal \"null\") when the org cannot be resolved")
    void shouldNotPrintNullWhenOrgUnresolved() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(messageWithOrderIdOrg(LAB_OID + ":9999"));

        String org = handler.getAccessionNumSourceOrganization();
        assertThat(org).isEqualTo("(Lab 9999)");
        assertThat(org).doesNotContain("null");
    }
}
