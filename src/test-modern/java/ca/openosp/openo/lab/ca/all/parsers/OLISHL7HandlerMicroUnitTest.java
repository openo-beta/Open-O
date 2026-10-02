package ca.openosp.openo.lab.ca.all.parsers;

import ca.openosp.openo.commn.dao.Hl7TextInfoDao;
import ca.openosp.openo.olis.dao.OLISMicroorganismNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISRequestNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISResultNomenclatureDao;
import ca.openosp.openo.olis.model.OLISMicroorganismNomenclature;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OLISHL7Handler}'s microorganism resolution (CV06).
 *
 * <p>Parses an OLIS ORU message whose third result is a coded microorganism
 * (OBX value type {@code CE}, coding system {@code HL79905}, code {@code 3092008}
 * with an empty text component) and asserts {@link OLISHL7Handler#getOBXCEName}
 * resolves it to the catalog organism name rather than returning the raw code /
 * blank. The micro catalog DAO is mocked, so the test is offline.</p>
 *
 * @since 2026-06-17
 */
@DisplayName("OLISHL7Handler microorganism resolution (CV06)")
@Tag("unit")
@Tag("fast")
public class OLISHL7HandlerMicroUnitTest extends OpenOUnitTestBase {

    private static final String HL7 = String.join("\r",
            "MSH|^~\\&|OLIS|2.16.840.1.113883.3.59.1:HL7-LAB|EMR|2.16.840.1.113883.3.59.1.2|20240115120000||ORU^R01^ORU_R01|OLISMSGMICRO9|T|2.4|||AL|NE|CAN|ASCII|en|^^ISO",
            "PID|1|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN|9999999999^^^MOH&2.16.840.1.113883.4.595&ISO^JHN||DOE^JANE^Q^^^^L||19850624|F|||123 MAIN ST^^TORONTO^ON^M5H 2N2^CAN^H",
            "ZPD|N|||||||||N",
            "ORC|RE|||ACCMICRO9^^GDL:5552^OBI||CM",
            "OBR|1|||GLU^Glucose Random^L|||20240115083000|||||||20240115093000|SER^Serum^HL70487|DR1234^SMITH^JOHN^^^DR^^^OLIS&2.16.840.1.113883.3.59.1&ISO^L^^^DRLIC||||||20240115110000|||F",
            "ZBR|1|HEM^Hematology^OLIS|HEM^Hematology^OLIS||5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1.5552&ISO^L|5552^Gamma-Dynacare^^^^GDL&2.16.840.1.113883.3.59.1.5552&ISO^L",
            "OBX|1|NM|14749-6^Glucose [Moles/volume] in Serum or Plasma^LN||5.4|mmol/L^^UCUM|3.6-6.0|N|||F|||20240115083000",
            "OBX|2|NM|2160-0^Creatinine [Mass/volume] in Serum or Plasma^LN||112|umol/L^^UCUM|44-80|H|||F|||20240115083000",
            "OBX|3|CE|600-7^Bacteria identified^LN||3092008^^HL79905||||||F|||20240115083000");

    @BeforeEach
    void registerMocks() {
        // OLISUtils static init resolves this via SpringUtils.
        registerMock(Hl7TextInfoDao.class, Mockito.mock(Hl7TextInfoDao.class));

        OLISResultNomenclatureDao resultDao = Mockito.mock(OLISResultNomenclatureDao.class);
        when(resultDao.findByNameId(anyString())).thenReturn(null);
        registerMock(OLISResultNomenclatureDao.class, resultDao);

        OLISRequestNomenclatureDao requestDao = Mockito.mock(OLISRequestNomenclatureDao.class);
        when(requestDao.findByNameId(anyString())).thenReturn(null);
        registerMock(OLISRequestNomenclatureDao.class, requestDao);

        OLISMicroorganismNomenclature staph = new OLISMicroorganismNomenclature();
        staph.setMicroorganismCode("3092008");
        staph.setAlternateName1("Staphylococcus aureus");
        Map<String, OLISMicroorganismNomenclature> map = new HashMap<>();
        map.put("3092008", staph);
        OLISMicroorganismNomenclatureDao microDao = Mockito.mock(OLISMicroorganismNomenclatureDao.class);
        when(microDao.findByMicroorganismCodes(any())).thenReturn(map);
        registerMock(OLISMicroorganismNomenclatureDao.class, microDao);
    }

    @Test
    @DisplayName("should resolve a coded microorganism (CE/HL79905) to its catalog name")
    void shouldResolveCodedMicroorganismName() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(HL7);

        assertThat(handler.getOBXCount(0)).isEqualTo(3);
        // OBX-3 is the coded organism with an empty text component; without the catalog
        // it would render as the raw code / blank.
        assertThat(handler.getOBXCEName(0, 2)).isEqualTo("Staphylococcus aureus");
    }

    @Test
    @DisplayName("should not throw on the CE-branch method sequence the display JSP calls")
    void shouldNotThrowOnCeBranchMethods() throws Exception {
        OLISHL7Handler handler = new OLISHL7Handler();
        handler.init(HL7);

        // Mirror the labDisplayOLIS.jsp CE-branch calls for the coded micro OBX.
        handler.getOBXValueType(0, 2);
        handler.getOBXIdentifier(0, 2);
        handler.getOBXCEName(0, 2);
        String status = handler.getOBXResultStatus(0, 2);
        assertThat(status).isNotEmpty();
        handler.isStatusFinal(status.charAt(0));
        String parentId = handler.getOBXCEParentId(0, 2);
        int childOBR = handler.getChildOBR(parentId) - 1;
        if (childOBR != -1) {
            handler.getChildObrResults(childOBR);
        }
    }
}
