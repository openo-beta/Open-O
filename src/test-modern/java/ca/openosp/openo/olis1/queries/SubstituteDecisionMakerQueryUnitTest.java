package ca.openosp.openo.olis1.queries;

import ca.openosp.openo.olis1.parameters.ZPD1;
import ca.openosp.openo.olis1.parameters.ZSD;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the Substitute Decision Maker ({@link ZSD}) consent-override
 * segment on OLIS query classes.
 *
 * <p>When a blocked laboratory result is retrieved under an SDM consent override
 * (CV11.2b / CV12.2b), the query must carry the SDM identity so OLIS records who
 * authorized viewing the blocked information. These tests assert the {@code ZSD}
 * serializes to the OLIS query-code form and is emitted by the patient/order
 * queries (Z01/Z02) immediately after the consent directives, while the
 * non-consent queries reject it.</p>
 *
 * @since 2026-06-17
 * @see ZSD
 * @see Z01Query
 * @see Z02Query
 */
@DisplayName("Substitute Decision Maker (ZSD) query serialization")
@Tag("unit")
@Tag("fast")
public class SubstituteDecisionMakerQueryUnitTest extends OpenOUnitTestBase {

    @Test
    @DisplayName("should serialize ZSD to the OLIS query-code form")
    void shouldSerializeZsdToOlisQueryCodeForm() {
        ZSD zsd = new ZSD("John", "Doe", "Spouse");

        assertThat(zsd.toOlisString())
                .isEqualTo("@ZSD.1^John~@ZSD.2^Doe~@ZSD.3^Spouse");
    }

    @Test
    @DisplayName("should emit ZSD after the consent directive in a Z01 query")
    void shouldEmitZsdAfterConsentInZ01Query() {
        Z01Query query = new Z01Query();
        query.setConsentToViewBlockedInformation(new ZPD1("Z"));
        query.setSubstituteDecisionMaker(new ZSD("Jane", "Smith", "Parent"));

        String hl7 = query.getQueryHL7String();

        assertThat(hl7).contains("@ZSD.1^Jane~@ZSD.2^Smith~@ZSD.3^Parent");
        // ZSD must follow the consent directive (ZPD.1) it accompanies.
        assertThat(hl7.indexOf("@ZPD.1")).isLessThan(hl7.indexOf("@ZSD.1"));
    }

    @Test
    @DisplayName("should omit ZSD from a Z01 query when no SDM is set")
    void shouldOmitZsdWhenNoSdmSetInZ01Query() {
        Z01Query query = new Z01Query();
        query.setConsentToViewBlockedInformation(new ZPD1("Z"));

        assertThat(query.getQueryHL7String()).doesNotContain("@ZSD");
    }

    @Test
    @DisplayName("should emit ZSD after the consent directive in a Z02 query")
    void shouldEmitZsdAfterConsentInZ02Query() {
        Z02Query query = new Z02Query();
        query.setConsentToViewBlockedInformation(new ZPD1("Z"));
        query.setSubstituteDecisionMaker(new ZSD("Mary", "Jones", "Guardian"));

        String hl7 = query.getQueryHL7String();

        assertThat(hl7).contains("@ZSD.1^Mary~@ZSD.2^Jones~@ZSD.3^Guardian");
        assertThat(hl7.indexOf("@ZPD.1")).isLessThan(hl7.indexOf("@ZSD.1"));
    }

    @Test
    @DisplayName("should reject ZSD on queries that do not carry patient consent")
    void shouldRejectZsdOnNonConsentQueries() {
        ZSD zsd = new ZSD("A", "B", "C");

        assertThatThrownBy(() -> new Z04Query().setSubstituteDecisionMaker(zsd))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> new Z50Query().setSubstituteDecisionMaker(zsd))
                .isInstanceOf(RuntimeException.class);
    }
}
