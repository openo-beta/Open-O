package ca.openosp.openo.integration.dhdr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The physician's decision on a DHDR temporary consent unblock, as it is stored and as it is shown.
 *
 * <p>Each decision is written to the gateway log by the consent-override path with
 * {@code externalSystem = "PCOI"} and the decision itself carried in {@code transactionType}. This
 * enum is the single source of truth for that vocabulary: the report's query selects on
 * {@link #storedValues()}, and the report's display uses {@link #labelFor(String)}. Adding a
 * decision here is therefore enough to make it both selectable and labelled.
 *
 * <p>DHDR13.02 speaks of a continue / refuse / cancel choice. {@link #FAILED} is a fourth,
 * operational state written when the override did not complete (DHDR11.01.b); it has no requirement
 * vocabulary of its own and is surfaced plainly.
 *
 * @since 2026-07-09
 */
public enum ConsentOverrideChoice {

  /** The patient's consent block was temporarily unblocked and the DHDR query proceeded. */
  OVERWRITE("Overwrite", "Continue (Unblock)"),

  /** The unblock was refused at the consent-block prompt. */
  REFUSED("Refused", "Refused"),

  /** The unblock was cancelled, either at the prompt or inside the PCOI viewlet. */
  CANCELLED("Cancelled", "Cancelled"),

  /** The unblock was attempted but did not complete; the block remains in force. */
  FAILED("Failed", "Failed (did not complete)");

  private static final List<String> STORED_VALUES;

  static {
    List<String> values = new ArrayList<String>();
    for (ConsentOverrideChoice choice : values()) {
      values.add(choice.storedValue);
    }
    STORED_VALUES = Collections.unmodifiableList(values);
  }

  private final String storedValue;
  private final String label;

  ConsentOverrideChoice(String storedValue, String label) {
    this.storedValue = storedValue;
    this.label = label;
  }

  /**
   * @return String the value written to {@code OMDGatewayTransactionLog.transactionType}
   */
  public String getStoredValue() {
    return storedValue;
  }

  /**
   * @return String the human-readable label for the report
   */
  public String getLabel() {
    return label;
  }

  /**
   * Every stored decision value, for use as the report query's transaction-type whitelist.
   *
   * @return List&lt;String&gt; an unmodifiable list of the stored values, in declaration order
   */
  public static List<String> storedValues() {
    return STORED_VALUES;
  }

  /**
   * Resolves a stored {@code transactionType} back to its choice.
   *
   * @param storedValue String the raw stored value, may be {@code null}
   * @return ConsentOverrideChoice the matching choice, or {@code null} if the value is null or
   *     unrecognised
   */
  public static ConsentOverrideChoice fromStoredValue(String storedValue) {
    for (ConsentOverrideChoice choice : values()) {
      if (choice.storedValue.equals(storedValue)) {
        return choice;
      }
    }
    return null;
  }

  /**
   * Renders a stored {@code transactionType} for display. An unrecognised value is passed through
   * unchanged rather than hidden, so an override written by a future code path still shows up.
   *
   * @param storedValue String the raw stored value, may be {@code null}
   * @return String the label, the raw value if unrecognised, or {@code ""} if {@code null}
   */
  public static String labelFor(String storedValue) {
    if (storedValue == null) {
      return "";
    }
    ConsentOverrideChoice choice = fromStoredValue(storedValue);
    return choice == null ? storedValue : choice.label;
  }
}
