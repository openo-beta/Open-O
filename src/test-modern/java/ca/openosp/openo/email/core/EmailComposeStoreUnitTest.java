package ca.openosp.openo.email.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ca.openosp.openo.commn.model.EmailAttachment;
import ca.openosp.openo.commn.model.EmailLog.TransactionType;
import ca.openosp.openo.commn.model.enumerator.DocumentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins down that each email compose page sends only what was stored under its own key.
 *
 * <p>Before {@link EmailComposeStore}, the compose page kept its attachments in one session slot
 * per user, so opening a second compose page, or viewing an old email from the eChart, replaced
 * them and Send went out with no or the wrong attachments. These tests keep that from coming back:</p>
 * <ul>
 *   <li>every compose page has its own entry, and other pages cannot change it</li>
 *   <li>Send takes the entry once, so the same page cannot send twice</li>
 *   <li>A failed send puts the entry back, unchanged, so the same page can send again</li>
 *   <li>a refresh keeps the entry, Cancel removes only its own entry</li>
 *   <li>the store is bounded and is not kept across a Tomcat restart</li>
 * </ul>
 */
@DisplayName("Email compose store keeps each compose page separate")
@Tag("unit")
@Tag("fast")
@Tag("service")
@Tag("email")
class EmailComposeStoreUnitTest {

    private static final String SESSION_KEY = "emailComposeStore";

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    private static EmailAttachmentSettings eFormSettings(String fdid, String demographicNo) {
        return new EmailAttachmentSettings(fdid, demographicNo, null, null, null, null, null,
                false, false, false, false, false, false, null, null, null, null, null, null, null, null);
    }

    private static List<EmailAttachment> documents(int... documentIds) {
        List<EmailAttachment> attachments = new ArrayList<>();
        for (int id : documentIds) {
            attachments.add(new EmailAttachment("doc" + id + ".pdf", "/tmp/doc" + id + ".pdf", DocumentType.DOC, id));
        }
        return attachments;
    }

    private static List<Integer> documentIds(EmailComposeStore.Entry entry) {
        return entry.attachments().stream().map(EmailAttachment::getDocumentId).toList();
    }

    @Nested
    @DisplayName("Isolated keys")
    class IsolatedKeys {

        @Test
        @DisplayName("should keep each page's patient and attachments when other pages and an old email view are opened")
        void shouldKeepOwnEntry_whenOtherPagesOpened() {
            String pageA = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));
            EmailComposeStore.setAttachments(session, pageA, documents(1));
            String pageB = EmailComposeStore.putEForm(session, eFormSettings("202", "2"));
            EmailComposeStore.setAttachments(session, pageB, documents(4));
            String oldEmailView = EmailComposeStore.putDirect(session, 2, documents(9));

            EmailComposeStore.Entry a = EmailComposeStore.get(session, pageA);
            EmailComposeStore.Entry b = EmailComposeStore.get(session, pageB);
            EmailComposeStore.Entry view = EmailComposeStore.get(session, oldEmailView);

            assertThat(a.demographicNo()).isEqualTo("1");
            assertThat(a.fdid()).isEqualTo("101");
            assertThat(documentIds(a)).containsExactly(1);
            assertThat(b.demographicNo()).isEqualTo("2");
            assertThat(documentIds(b)).containsExactly(4);
            assertThat(documentIds(view)).containsExactly(9);
        }

        @Test
        @DisplayName("should give every page a different key")
        void shouldReturnDifferentKeys_whenSamePageOpenedTwice() {
            String first = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));
            String second = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should store an eForm email as EFORM with its settings")
        void shouldStoreEFormEntry_whenPutEForm() {
            EmailAttachmentSettings settings = eFormSettings("101", "1");
            String key = EmailComposeStore.putEForm(session, settings);

            EmailComposeStore.Entry entry = EmailComposeStore.get(session, key);

            assertThat(entry.transactionType()).isEqualTo(TransactionType.EFORM);
            assertThat(entry.settings()).isSameAs(settings);
            assertThat(entry.attachments()).isEmpty();
        }

        @Test
        @DisplayName("should store a resent email as DIRECT with no settings and no eForm id")
        void shouldStoreDirectEntry_whenPutDirect() {
            String key = EmailComposeStore.putDirect(session, 2, documents(9));

            EmailComposeStore.Entry entry = EmailComposeStore.get(session, key);

            assertThat(entry.transactionType()).isEqualTo(TransactionType.DIRECT);
            assertThat(entry.settings()).isNull();
            assertThat(entry.demographicNo()).isEqualTo("2");
            assertThat(entry.fdid()).isEmpty();
        }

        @Test
        @DisplayName("should store a null eForm id as empty")
        void shouldStoreEmptyFdid_whenSettingsFdidIsNull() {
            String key = EmailComposeStore.putEForm(session, eFormSettings(null, "3"));

            assertThat(EmailComposeStore.get(session, key).fdid()).isEmpty();
        }

        @Test
        @DisplayName("should not change the stored list when the caller's list changes later")
        void shouldCopyAttachments_whenCallerListChanges() {
            List<EmailAttachment> attachments = documents(9);
            String key = EmailComposeStore.putDirect(session, 2, attachments);

            attachments.clear();

            assertThat(documentIds(EmailComposeStore.get(session, key))).containsExactly(9);
        }

        @Test
        @DisplayName("should keep an entry with no attachments so a legit email without attachments can be sent")
        void shouldKeepEntry_whenNoAttachments() {
            String key = EmailComposeStore.putDirect(session, 1, List.of());

            EmailComposeStore.Entry entry = EmailComposeStore.take(session, key);

            assertThat(entry).isNotNull();
            assertThat(entry.attachments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Unknown and consumed keys")
    class UnknownAndConsumedKeys {

        @Test
        @DisplayName("should return null for a null or unknown key")
        void shouldReturnNull_whenKeyNullOrUnknown() {
            EmailComposeStore.putEForm(session, eFormSettings("101", "1"));

            assertThat(EmailComposeStore.get(session, null)).isNull();
            assertThat(EmailComposeStore.get(session, "unknown")).isNull();
            assertThat(EmailComposeStore.take(session, null)).isNull();
            assertThat(EmailComposeStore.take(session, "unknown")).isNull();
        }

        @Test
        @DisplayName("should return null when the session has no store yet")
        void shouldReturnNull_whenSessionEmpty() {
            assertThat(EmailComposeStore.get(session, "any")).isNull();
            assertThat(EmailComposeStore.take(session, "any")).isNull();
        }

        @Test
        @DisplayName("should return the entry on the first Send and null on the second")
        void shouldReturnEntryOnce_whenSentTwice() {
            String key = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));
            EmailComposeStore.setAttachments(session, key, documents(1));

            EmailComposeStore.Entry first = EmailComposeStore.take(session, key);
            EmailComposeStore.Entry second = EmailComposeStore.take(session, key);

            assertThat(documentIds(first)).containsExactly(1);
            assertThat(second).isNull();
            assertThat(EmailComposeStore.get(session, key)).isNull();
        }

        @Test
        @DisplayName("should keep the other pages when one page is sent")
        void shouldKeepOtherPages_whenOnePageSent() {
            String pageA = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));
            String pageB = EmailComposeStore.putEForm(session, eFormSettings("202", "2"));

            EmailComposeStore.take(session, pageA);

            assertThat(EmailComposeStore.get(session, pageB)).isNotNull();
        }

        @Test
        @DisplayName("should ignore attachments set for an unknown key")
        void shouldIgnoreSetAttachments_whenKeyUnknown() {
            EmailComposeStore.setAttachments(session, "unknown", documents(1));
            EmailComposeStore.setAttachments(session, null, documents(1));

            assertThat(session.getAttribute(SESSION_KEY)).isNull();
        }
    }

    @Nested
    @DisplayName("Refresh")
    class Refresh {

        @Test
        @DisplayName("should replace the attachments and keep the entry when the page is refreshed")
        void shouldReplaceAttachments_whenPageRefreshed() {
            String key = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));
            EmailComposeStore.setAttachments(session, key, documents(1, 2));

            EmailComposeStore.setAttachments(session, key, documents(3));
            EmailComposeStore.Entry entry = EmailComposeStore.get(session, key);

            assertThat(documentIds(entry)).containsExactly(3);
            assertThat(entry.demographicNo()).isEqualTo("1");
            assertThat(entry.fdid()).isEqualTo("101");
            assertThat(entry.transactionType()).isEqualTo(TransactionType.EFORM);
        }

        @Test
        @DisplayName("should keep the entry when it is read many times")
        void shouldKeepEntry_whenReadRepeatedly() {
            String key = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));

            EmailComposeStore.get(session, key);
            EmailComposeStore.get(session, key);

            assertThat(EmailComposeStore.take(session, key)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cancellation")
    class Cancellation {

        @Test
        @DisplayName("should remove only the cancelled page's entry")
        void shouldRemoveOnlyOwnEntry_whenCancelled() {
            String pageA = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));
            String pageB = EmailComposeStore.putEForm(session, eFormSettings("202", "2"));

            EmailComposeStore.remove(session, pageA);

            assertThat(EmailComposeStore.get(session, pageA)).isNull();
            assertThat(EmailComposeStore.get(session, pageB)).isNotNull();
        }

        @Test
        @DisplayName("should remove the session attribute when the last entry is gone")
        void shouldRemoveSessionAttribute_whenStoreEmpty() {
            String pageA = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));
            String pageB = EmailComposeStore.putDirect(session, 2, documents(9));

            EmailComposeStore.remove(session, pageA);
            EmailComposeStore.take(session, pageB);

            assertThat(session.getAttribute(SESSION_KEY)).isNull();
        }

        @Test
        @DisplayName("should do nothing when cancelling with a null or unknown key")
        void shouldDoNothing_whenCancelKeyUnknown() {
            String key = EmailComposeStore.putEForm(session, eFormSettings("101", "1"));

            EmailComposeStore.remove(session, null);
            EmailComposeStore.remove(session, "unknown");

            assertThat(EmailComposeStore.get(session, key)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Concurrent compose pages")
    class ConcurrentPages {

        @Test
        @DisplayName("should keep every page's own entry and send each one exactly once when pages work at the same time")
        void shouldSendEachPageOnce_whenPagesRunConcurrently() throws InterruptedException {
            int pages = 40;
            ExecutorService pool = Executors.newFixedThreadPool(8);
            CountDownLatch start = new CountDownLatch(1);
            Set<String> keys = ConcurrentHashMap.newKeySet();
            List<Integer> mismatches = Collections.synchronizedList(new ArrayList<>());

            for (int i = 1; i <= pages; i++) {
                int documentId = i;
                pool.submit(() -> {
                    start.await();
                    String key = EmailComposeStore.putEForm(session, eFormSettings(String.valueOf(documentId), String.valueOf(documentId)));
                    EmailComposeStore.setAttachments(session, key, documents(documentId));
                    keys.add(key);
                    EmailComposeStore.Entry entry = EmailComposeStore.get(session, key);
                    if (entry == null || !documentIds(entry).equals(List.of(documentId))) {
                        mismatches.add(documentId);
                    }
                    return null;
                });
            }
            start.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

            assertThat(mismatches).isEmpty();
            assertThat(keys).hasSize(pages);

            ExecutorService senders = Executors.newFixedThreadPool(8);
            AtomicInteger sent = new AtomicInteger();
            for (String key : keys) {
                for (int click = 0; click < 2; click++) {
                    senders.submit(() -> {
                        EmailComposeStore.Entry entry = EmailComposeStore.take(session, key);
                        if (entry != null) {
                            sent.incrementAndGet();
                        }
                    });
                }
            }
            senders.shutdown();
            assertThat(senders.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

            assertThat(sent.get()).isEqualTo(pages);
            assertThat(session.getAttribute(SESSION_KEY)).isNull();
        }
    }

    @Nested
    @DisplayName("Failed send")
    class FailedSend {

        @Test
        @DisplayName("should send again under the same key after the entry is restored")
        void shouldSendAgain_whenEntryRestored() {
            String key = EmailComposeStore.putDirect(session, 1, documents(1));
            EmailComposeStore.Entry entry = EmailComposeStore.take(session, key);

            EmailComposeStore.restore(session, key, entry);

            assertThat(documentIds(EmailComposeStore.take(session, key))).containsExactly(1);
            assertThat(EmailComposeStore.take(session, key)).isNull();
        }

        @Test
        @DisplayName("should refuse a second send while the first is still sending")
        void shouldRefuseSecondSend_whileFirstIsSending() {
            String key = EmailComposeStore.putDirect(session, 1, documents(1));

            EmailComposeStore.Entry sending = EmailComposeStore.take(session, key);

            assertThat(sending).isNotNull();
            assertThat(EmailComposeStore.take(session, key)).isNull();
        }

        @Test
        @DisplayName("should keep the stored file path when a copied attachment is encrypted")
        void shouldKeepStoredFilePath_whenCopyIsEncrypted() {
            String key = EmailComposeStore.putDirect(session, 1, documents(1));
            EmailComposeStore.Entry entry = EmailComposeStore.take(session, key);

            entry.copyOfAttachments().get(0).setFilePath("/tmp/encrypted.pdf");

            EmailAttachment stored = entry.attachments().get(0);
            assertThat(stored.getFilePath()).isEqualTo("/tmp/doc1.pdf");
            assertThat(entry.copyOfAttachments().get(0).getFilePath()).isEqualTo("/tmp/doc1.pdf");
        }
    }

    @Nested
    @DisplayName("Size limit and restart")
    class LimitAndRestart {

        @Test
        @DisplayName("should drop the least recently used old email before any eForm page")
        void shouldDropLeastRecentlyUsedOldEmail_whenOver50Pages() {
            String eForm = EmailComposeStore.putEForm(session, eFormSettings("1", "1"));
            String oldest = EmailComposeStore.putDirect(session, 1, List.of());
            String refreshed = EmailComposeStore.putDirect(session, 1, List.of());
            List<String> others = new ArrayList<>();
            for (int i = 0; i < 47; i++) {
                others.add(EmailComposeStore.putDirect(session, 1, List.of()));
            }
            EmailComposeStore.get(session, refreshed);

            String newest = EmailComposeStore.putDirect(session, 1, List.of());

            assertThat(EmailComposeStore.get(session, oldest)).isNull();
            assertThat(EmailComposeStore.get(session, eForm)).isNotNull();
            assertThat(EmailComposeStore.get(session, refreshed)).isNotNull();
            assertThat(EmailComposeStore.get(session, newest)).isNotNull();
            assertThat(others).allMatch(key -> EmailComposeStore.get(session, key) != null);
        }

        @Test
        @DisplayName("should drop the least recently used eForm page when only eForm pages are waiting")
        void shouldDropLeastRecentlyUsedEForm_whenOnlyEFormPages() {
            String oldest = EmailComposeStore.putEForm(session, eFormSettings("1", "1"));
            String refreshed = EmailComposeStore.putEForm(session, eFormSettings("2", "1"));
            for (int i = 0; i < 48; i++) {
                EmailComposeStore.putEForm(session, eFormSettings(String.valueOf(i + 3), "1"));
            }
            EmailComposeStore.get(session, refreshed);

            String newest = EmailComposeStore.putEForm(session, eFormSettings("51", "1"));

            assertThat(EmailComposeStore.get(session, oldest)).isNull();
            assertThat(EmailComposeStore.get(session, refreshed)).isNotNull();
            assertThat(EmailComposeStore.get(session, newest)).isNotNull();
        }

        @Test
        @DisplayName("should not be serializable so Tomcat does not keep it across a restart")
        void shouldNotBeSerializable_soNotKeptAcrossRestart() {
            EmailComposeStore.putEForm(session, eFormSettings("101", "1"));

            assertThat(session.getAttribute(SESSION_KEY)).isNotNull().isNotInstanceOf(Serializable.class);
        }
    }
}
