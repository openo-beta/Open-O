package ca.openosp.openo.email.core;

import ca.openosp.openo.commn.model.EmailLog.TransactionType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmailData#setTransactionType(String)}, which reads the email type posted by the compose page.
 *
 * <p>Cancel on the compose page routes on this type: an eForm email goes back to the eForm, and a document
 * email closes its popup. An unknown or missing type is a direct email.</p>
 *
 * @since 2026-11-25
 */
@DisplayName("EmailData transaction type from the compose page")
@Tag("unit")
@Tag("fast")
@Tag("email")
class EmailDataTransactionTypeUnitTest {

    @ParameterizedTest
    @EnumSource(TransactionType.class)
    @DisplayName("should read every email type by its name, so a new type needs no change in EmailData")
    void shouldReturnType_whenTypeIsAnyTransactionTypeName(TransactionType type) {
        EmailData emailData = new EmailData();
        emailData.setTransactionType(type.name());

        assertThat(emailData.getTransactionType()).isEqualTo(type);
    }

    @Test
    @DisplayName("should read DOCUMENT as a document email, in any case")
    void shouldReturnDocument_whenTypeIsDocument() {
        EmailData upper = new EmailData();
        upper.setTransactionType("DOCUMENT");
        EmailData lower = new EmailData();
        lower.setTransactionType("document");

        assertThat(upper.getTransactionType()).isEqualTo(TransactionType.DOCUMENT);
        assertThat(lower.getTransactionType()).isEqualTo(TransactionType.DOCUMENT);
    }

    @Test
    @DisplayName("should read EFORM as an eForm email")
    void shouldReturnEForm_whenTypeIsEForm() {
        EmailData emailData = new EmailData();
        emailData.setTransactionType("EFORM");

        assertThat(emailData.getTransactionType()).isEqualTo(TransactionType.EFORM);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "DIRECT", "SOMETHING_ELSE"})
    @DisplayName("should read a missing or unknown type as a direct email")
    void shouldReturnDirect_whenTypeIsMissingOrUnknown(String type) {
        EmailData emailData = new EmailData();
        emailData.setTransactionType(type);

        assertThat(emailData.getTransactionType()).isEqualTo(TransactionType.DIRECT);
    }
}
