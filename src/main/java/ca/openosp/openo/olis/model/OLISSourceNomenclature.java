//CHECKSTYLE:OFF
package ca.openosp.openo.olis.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * OLIS specimen-source nomenclature catalog entry. Maps an OLIS specimen source
 * code (sent in a test request's specimen source, OBR-15.1.1) to a human-readable
 * specimen type for display (CT 9.4 "Specimen Type" — lookup = "Source Nom File").
 *
 * <p>Columns mirror the "Source" sheet of the OLIS Nomenclatures distribution,
 * which is a two-column lookup: {@code Value} (the code, e.g. {@code 24H}) and
 * {@code Description} (the display name, e.g. {@code Urine 24 Hour}). Modelled to
 * match the existing nomenclature catalogs (Result/Request/Microorganism).</p>
 *
 * @since 2026-06-18
 */
@Entity
public class OLISSourceNomenclature extends AbstractModel<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String value;
    private String description;

    public OLISSourceNomenclature() {
        super();
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
