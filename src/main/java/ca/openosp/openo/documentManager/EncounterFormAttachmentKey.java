package ca.openosp.openo.documentManager;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stable identity for an encounter-form attachment.
 *
 * <p>Encounter-form row identifiers are unique only within their form table. This value
 * carries both parts through HTML form submission without ever using the submitted table
 * name directly in a query.</p>
 */
public final class EncounterFormAttachmentKey {
    private static final String DELIMITER = "|";
    private static final Pattern SAFE_TABLE = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,49}");

    private final String formTable;
    private final int formId;

    private EncounterFormAttachmentKey(String formTable, int formId) {
        this.formTable = formTable;
        this.formId = formId;
    }

    /**
     * Creates a key from trusted server-side form metadata.
     *
     * @param formTable encounter-form database table
     * @param formId encounter-form row identifier
     * @return validated attachment key
     */
    public static EncounterFormAttachmentKey of(String formTable, int formId) {
        validate(formTable, formId);
        return new EncounterFormAttachmentKey(formTable, formId);
    }

    /**
     * Parses an untrusted submitted key.
     *
     * @param value encoded value in {@code formTable|formId} format
     * @return validated attachment key
     * @throws IllegalArgumentException when the value is malformed
     */
    public static EncounterFormAttachmentKey parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Encounter form attachment key is required");
        }
        int delimiter = value.indexOf(DELIMITER);
        if (delimiter <= 0 || delimiter != value.lastIndexOf(DELIMITER)) {
            throw new IllegalArgumentException("Invalid encounter form attachment key");
        }

        String table = value.substring(0, delimiter);
        int id;
        try {
            id = Integer.parseInt(value.substring(delimiter + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid encounter form identifier", e);
        }
        validate(table, id);
        return new EncounterFormAttachmentKey(table, id);
    }

    private static void validate(String formTable, int formId) {
        if (formTable == null || !SAFE_TABLE.matcher(formTable).matches()) {
            throw new IllegalArgumentException("Invalid encounter form table");
        }
        if (formId <= 0) {
            throw new IllegalArgumentException("Encounter form identifier must be positive");
        }
    }

    public String getFormTable() {
        return formTable;
    }

    public int getFormId() {
        return formId;
    }

    public String encode() {
        return formTable + DELIMITER + formId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EncounterFormAttachmentKey)) {
            return false;
        }
        EncounterFormAttachmentKey that = (EncounterFormAttachmentKey) other;
        return formId == that.formId && formTable.equals(that.formTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formTable, formId);
    }
}

