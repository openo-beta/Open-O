//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */

package ca.openosp.openo.email.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import ca.openosp.openo.commn.model.EmailAttachment;
import ca.openosp.openo.commn.model.EmailLog.TransactionType;

/**
 * Keeps what each open email compose page will send, under its own random key.
 * <p>
 * The key goes on the compose page and is posted back on Send or Cancel. Send uses only the
 * patient, eForm and attachments stored under that key, so a second compose page, or viewing an
 * old email from the eChart, can never change what another page sends. Send and Cancel remove the
 * entry, so the same page cannot send twice. A refresh of the compose page keeps the entry.
 * <p>
 * Entries that are never sent or cancelled are dropped once more than {@value #MAX_ENTRIES} are
 * waiting (the least recently used first), or when the user logs out.
 * <p>
 * Nothing here survives a Tomcat restart: the store is not serializable, so Tomcat drops it
 * when it saves sessions, and an email page opened before the restart is refused on Send.
 *
 * @since 2026-10-30
 */
public final class EmailComposeStore {

    private static final String SESSION_KEY = "emailComposeStore";
    private static final int MAX_ENTRIES = 50;

    private EmailComposeStore() {
    }

    /**
     * What one compose page shows and may send.
     *
     * @param settings EmailAttachmentSettings the eForm email settings, null when resending an old email
     * @param transactionType TransactionType EFORM for an eForm email, DIRECT for a resend
     * @param demographicNo String the patient the email is for
     * @param fdid String the eForm data id, empty when the email is not from an eForm
     * @param attachments List&lt;EmailAttachment&gt; the attachments shown on the compose page
     */
    public record Entry(EmailAttachmentSettings settings, TransactionType transactionType, String demographicNo,
                        String fdid, ArrayList<EmailAttachment> attachments) {
    }

    /**
     * Stores the settings of an eForm being emailed. The attachments are added when the compose page is built.
     *
     * @param session HttpSession the logged in user's session
     * @param settings EmailAttachmentSettings the eForm email settings
     * @return String the key to put on the compose page URL
     */
    public static String putEForm(HttpSession session, EmailAttachmentSettings settings) {
        String fdid = settings.fdid() == null ? "" : settings.fdid();
        return put(session, new Entry(settings, TransactionType.EFORM, settings.demographicNo(), fdid, new ArrayList<>()));
    }

    /**
     * Stores an old email opened for resending.
     *
     * @param session HttpSession the logged in user's session
     * @param demographicNo int the patient the email is for
     * @param attachments List&lt;EmailAttachment&gt; the attachments shown on the compose page
     * @return String the key to put on the compose page
     */
    public static String putDirect(HttpSession session, int demographicNo, List<EmailAttachment> attachments) {
        return put(session, new Entry(null, TransactionType.DIRECT, String.valueOf(demographicNo), "", new ArrayList<>(attachments)));
    }

    /**
     * Reads the entry stored under a key. The entry stays, so the compose page can be refreshed.
     *
     * @param session HttpSession the logged in user's session
     * @param key String the key from the compose page
     * @return Entry the stored entry, or null when the key is unknown, sent or cancelled
     */
    public static synchronized Entry get(HttpSession session, String key) {
        Entries entries = (Entries) session.getAttribute(SESSION_KEY);
        if (entries == null || key == null) {
            return null;
        }
        return entries.get(key);
    }

    /**
     * Sets the attachments the compose page shows, replacing the ones from an earlier load of the same page.
     *
     * @param session HttpSession the logged in user's session
     * @param key String the key from the compose page
     * @param attachments List&lt;EmailAttachment&gt; the attachments shown on the compose page
     */
    public static synchronized void setAttachments(HttpSession session, String key, List<EmailAttachment> attachments) {
        Entries entries = (Entries) session.getAttribute(SESSION_KEY);
        Entry entry = entries == null || key == null ? null : entries.get(key);
        if (entry == null) {
            return;
        }
        entries.put(key, new Entry(entry.settings(), entry.transactionType(), entry.demographicNo(), entry.fdid(), new ArrayList<>(attachments)));
        session.setAttribute(SESSION_KEY, entries);
    }

    /**
     * Removes and returns the entry stored under a key, when the email is sent.
     * Only the first Send of a page gets the entry, a second one gets null.
     *
     * @param session HttpSession the logged in user's session
     * @param key String the key posted by the compose page, may be null
     * @return Entry the stored entry, or null when the key is unknown, already sent or cancelled
     */
    public static synchronized Entry take(HttpSession session, String key) {
        Entries entries = (Entries) session.getAttribute(SESSION_KEY);
        if (entries == null || key == null) {
            return null;
        }
        Entry entry = entries.remove(key);
        if (entries.isEmpty()) {
            session.removeAttribute(SESSION_KEY);
        } else {
            session.setAttribute(SESSION_KEY, entries);
        }
        return entry;
    }

    /**
     * Removes the entry stored under a key, when the email is cancelled.
     *
     * @param session HttpSession the logged in user's session
     * @param key String the key posted by the compose page, may be null
     */
    public static void remove(HttpSession session, String key) {
        take(session, key);
    }

    private static synchronized String put(HttpSession session, Entry entry) {
        Entries entries = (Entries) session.getAttribute(SESSION_KEY);
        if (entries == null) {
            entries = new Entries();
        }
        String key = UUID.randomUUID().toString();
        entries.put(key, entry);
        session.setAttribute(SESSION_KEY, entries);
        return key;
    }

    /**
     * Entries in least recently used order, so an open page that is refreshed or sent soon is kept.
     * Not serializable on purpose, so Tomcat does not save it with the session or restore it after a restart.
     */
    private static class Entries {
        private final Map<String, Entry> map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                return size() > MAX_ENTRIES;
            }
        };

        Entry get(String key) {
            return map.get(key);
        }

        void put(String key, Entry entry) {
            map.put(key, entry);
        }

        Entry remove(String key) {
            return map.remove(key);
        }

        boolean isEmpty() {
            return map.isEmpty();
        }
    }
}
