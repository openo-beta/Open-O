/**
 *
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
 * This software was written for
 * Magenta Health
 * Toronto, Ontario, Canada
 */
package ca.openosp.openo.tickler.dao;

import ca.openosp.openo.commn.dao.TicklerDocsDao;
import ca.openosp.openo.commn.model.TicklerDocs;
import ca.openosp.openo.test.base.OpenOTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TicklerDocsDao}, the data access layer behind the modern
 * Tickler document attachment component (issue #2476).
 *
 * <p>Verifies the three finders and confirms that soft-deleted rows ({@code deleted = 'Y'})
 * are excluded from query results.</p>
 *
 * @since 2026-06-12
 * @see TicklerDocsDao
 * @see TicklerDocs
 */
@Tag("integration")
@Tag("database")
@Tag("slow")
@Tag("dao")
@Tag("tickler")
@Transactional
class TicklerDocsDaoIntegrationTest extends OpenOTestBase {

    @Autowired
    private TicklerDocsDao ticklerDocsDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    private static final int TICKLER_ID = 5000;
    private static final String PROVIDER_NO = "999998";

    private TicklerDocs persistAttachment(int ticklerId, int documentNo, String docType) {
        TicklerDocs ticklerDocs = new TicklerDocs(ticklerId, documentNo, docType, PROVIDER_NO);
        ticklerDocsDao.persist(ticklerDocs);
        entityManager.flush();
        return ticklerDocs;
    }

    @Test
    @DisplayName("should persist and find tickler attachment by id")
    void shouldPersistAndFindAttachment_whenAttachmentSaved() {
        TicklerDocs saved = persistAttachment(TICKLER_ID, 11, TicklerDocs.DOCTYPE_DOC);

        TicklerDocs found = ticklerDocsDao.find(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getTicklerId()).isEqualTo(TICKLER_ID);
        assertThat(found.getDocumentNo()).isEqualTo(11);
        assertThat(found.getDocType()).isEqualTo(TicklerDocs.DOCTYPE_DOC);
    }

    @Test
    @DisplayName("should return all non-deleted attachments for a tickler")
    @Tag("read")
    void shouldReturnAllAttachments_whenFindingByTicklerId() {
        persistAttachment(TICKLER_ID, 11, TicklerDocs.DOCTYPE_DOC);
        persistAttachment(TICKLER_ID, 22, TicklerDocs.DOCTYPE_LAB);
        persistAttachment(TICKLER_ID + 1, 33, TicklerDocs.DOCTYPE_DOC);

        List<TicklerDocs> results = ticklerDocsDao.findByTicklerId(TICKLER_ID);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(TicklerDocs::getDocumentNo).containsExactlyInAnyOrder(11, 22);
    }

    @Test
    @DisplayName("should filter attachments by document type")
    @Tag("read")
    @Tag("filter")
    void shouldFilterByDocType_whenFindingByTicklerIdDocType() {
        persistAttachment(TICKLER_ID, 11, TicklerDocs.DOCTYPE_DOC);
        persistAttachment(TICKLER_ID, 22, TicklerDocs.DOCTYPE_LAB);

        List<TicklerDocs> docs = ticklerDocsDao.findByTicklerIdDocType(TICKLER_ID, TicklerDocs.DOCTYPE_DOC);

        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getDocumentNo()).isEqualTo(11);
    }

    @Test
    @DisplayName("should find a specific attachment by tickler id, document number and type")
    @Tag("read")
    void shouldFindSpecificAttachment_whenFindingByTicklerIdDocNoDocType() {
        persistAttachment(TICKLER_ID, 11, TicklerDocs.DOCTYPE_DOC);

        List<TicklerDocs> results = ticklerDocsDao.findByTicklerIdDocNoDocType(TICKLER_ID, 11, TicklerDocs.DOCTYPE_DOC);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDocType()).isEqualTo(TicklerDocs.DOCTYPE_DOC);
    }

    @Test
    @DisplayName("should exclude soft-deleted attachments from finder results")
    @Tag("read")
    @Tag("delete")
    void shouldExcludeSoftDeleted_whenAttachmentMarkedDeleted() {
        TicklerDocs attachment = persistAttachment(TICKLER_ID, 11, TicklerDocs.DOCTYPE_DOC);

        attachment.setDeleted(TicklerDocs.DELETED);
        ticklerDocsDao.merge(attachment);
        entityManager.flush();

        assertThat(ticklerDocsDao.findByTicklerId(TICKLER_ID)).isEmpty();
        assertThat(ticklerDocsDao.findByTicklerIdDocType(TICKLER_ID, TicklerDocs.DOCTYPE_DOC)).isEmpty();
        assertThat(ticklerDocsDao.findByTicklerIdDocNoDocType(TICKLER_ID, 11, TicklerDocs.DOCTYPE_DOC)).isEmpty();
    }
}
