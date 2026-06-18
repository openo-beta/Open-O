//CHECKSTYLE:OFF
package ca.openosp.openo.olis1.parameters;

/**
 * ZSD - Substitute Decision Maker.
 *
 * <p>Carries the identity of a Substitute Decision Maker (SDM) when a blocked
 * laboratory result is retrieved under an SDM consent override. It accompanies
 * the {@link ZPD1} "consent to view blocked information" directive (override
 * code {@code Z}) on a Z01/Z02 query so OLIS records <em>who</em> authorized the
 * override, as required for the SDM-override conformance scenarios
 * (CV11.2b / CV12.2b).</p>
 *
 * <p>The OLIS query-code form is:
 * {@code @ZSD.1^<firstName>~@ZSD.2^<lastName>~@ZSD.3^<relationship>}.</p>
 *
 * <p>Derived from the oscarpro {@code com.indivica.olis.parameters.ZSD} class
 * (GPLv2), namespace-migrated to {@code ca.openosp.openo}.</p>
 */
public class ZSD implements Parameter {

    private String firstName;
    private String lastName;
    private String relationship;

    public ZSD() {
    }

    public ZSD(String firstName, String lastName, String relationship) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.relationship = relationship;
    }

    @Override
    public String toOlisString() {
        return getQueryCode();
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Integer part, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Integer part, Integer part2, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryCode() {
        return "@ZSD.1^" + escapeHl7(firstName)
                + "~@ZSD.2^" + escapeHl7(lastName)
                + "~@ZSD.3^" + escapeHl7(relationship);
    }

    /**
     * Escapes the HL7 v2 delimiter characters in a free-text SDM field so a value
     * containing {@code ~ ^ \ &} cannot corrupt the {@code @ZSD.n^value~...} query
     * segment structure. The escape character must be substituted first so the
     * escape sequences introduced for the other delimiters are not re-escaped.
     *
     * @param value String the raw SDM field (first/last name or relationship); may be null
     * @return String the delimiter-escaped value, or an empty string when {@code value} is null
     */
    private static String escapeHl7(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\E\\")
                .replace("~", "\\R\\")
                .replace("^", "\\S\\")
                .replace("&", "\\T\\");
    }
}
