package ca.openosp.openo.olis.model;

/**
 * The OLIS participating laboratories offered as query-parameter options on the
 * OLIS search and preferences screens (Reporting / Exclude Reporting / Performing /
 * Exclude Performing / Specimen Collector laboratory selectors).
 * <p>
 * Single source of truth for the lab list that was previously hard-coded across
 * seven JSP dropdowns in {@code olis/Search.jsp} and {@code provider/olis_preferences.jsp}
 * — see OLIS04.03. Each constant carries the OLIS laboratory number ({@code labNo},
 * used as the dropdown option value) and the human-readable {@code displayName}. The
 * fully-qualified OLIS object identifier used by {@code OLISUtils} for source-facility
 * matching is derived via {@link #getOid()}.
 *
 * @since 2026-05-14
 */
public enum OLISParticipatingLab {

    GAMMA_DYNACARE("5552", "Gamma-Dynacare"),
    CML("5407", "CML"),
    LIFELABS("5687", "LifeLabs");

    /** OLIS root OID that Ontario laboratory identifiers are scoped under. */
    private static final String OLIS_OID_ROOT = "2.16.840.1.113883.3.59.1:";

    private final String labNo;
    private final String displayName;

    OLISParticipatingLab(String labNo, String displayName) {
        this.labNo = labNo;
        this.displayName = displayName;
    }

    /**
     * @return String the OLIS laboratory number (dropdown option value), e.g. "5552"
     */
    public String getLabNo() {
        return labNo;
    }

    /**
     * @return String the human-readable laboratory name, e.g. "Gamma-Dynacare"
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return String the fully-qualified OLIS object identifier, e.g.
     *         "2.16.840.1.113883.3.59.1:5552"
     */
    public String getOid() {
        return OLIS_OID_ROOT + labNo;
    }
}
