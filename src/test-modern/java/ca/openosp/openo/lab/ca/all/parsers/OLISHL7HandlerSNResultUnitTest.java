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
 * Pins {@link OLISHL7Handler#getOBXSNResult} for the HL7 Structured-Numeric (SN)
 * data type (CT 13.2). SN packs a value across up to five OBX-5 components —
 * {@code <comparator>^<num1>^<separator>^<num2>^<suffix>} — which the renderer must
 * reassemble into one readable string (e.g. {@code <100}, {@code 100-200}). A real
 * multi-component message is used here (an earlier ad-hoc sample under-populated the
 * components, which is what prompted the "is component 2 read?" doubt — it is).
 *
 * @since 2026-06-18
 */
@DisplayName("OLISHL7Handler SN (structured numeric) result assembly (CT 13.2)")
@Tag("unit")
@Tag("fast")
public class OLISHL7HandlerSNResultUnitTest extends OpenOUnitTestBase {

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
     * Build an ORU with two SN results: OBX|1 = {@code <^100} (comparator + value),
     * OBX|2 = {@code ^100^-^200} (a num1–num2 range). The {@code \S\} escapes encode
     * the literal {@code ^} component separators OLIS uses inside the SN value.
     */
    private static String messageWithSnResults() {
        return String.join("\r",
                "MSH|^~\\&|OLIS|OH|EMR|CLINIC|20240115120000||ORU^R01^ORU_R01|MSG1|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO",
                "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F",
                "ZPD|N|||||||||N",
                "ORC|RE|||ACC1^^GDL:5552^OBI||CM",
                "OBR|1|||TROP^Troponin^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487|||||||20240115110000|||F",
                "ZBR|1|CHEM^Chemistry^OLIS|CHEM^Chemistry^OLIS||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1:5552&ISO^L",
                // SN value "<^100": comparator "<", num1 "100"
                "OBX|1|SN|49563-0^Troponin^LN||<^100|ng/L|||||F|||20240115083000",
                // SN value "^100^-^200": num1 "100", separator "-", num2 "200" (a range)
                "OBX|2|SN|2160-0^Range^LN||^100^-^200|mmol/L|||||F|||20240115083000");
    }

    @Test
    @DisplayName("comparator + value: \"<^100\" assembles to \"<100\"")
    void shouldAssembleComparatorAndValue() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(messageWithSnResults());

        assertThat(handler.getOBXSNResult(0, 0)).isEqualTo("<100");
    }

    @Test
    @DisplayName("range: \"^100^-^200\" assembles to \"100-200\" (component 2 and 4 both read)")
    void shouldAssembleRange() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(messageWithSnResults());

        // num1 (comp 2) + separator (comp 3) + num2 (comp 4) — proves comp 2 is read.
        assertThat(handler.getOBXSNResult(0, 1)).isEqualTo("100-200");
    }
}
