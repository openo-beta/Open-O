//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.TicklerDocs;

/**
 * Data access for {@link TicklerDocs} (Tickler document attachments).
 *
 * <p>Tickler counterpart of {@code ConsultDocsDao}/{@code EFormDocsDao}. All finders exclude
 * soft-deleted rows ({@code deleted is NULL}).</p>
 *
 * @since 2026-06-12
 */
public interface TicklerDocsDao extends AbstractDao<TicklerDocs> {
    List<TicklerDocs> findByTicklerIdDocNoDocType(Integer ticklerId, Integer documentNo, String docType);

    List<TicklerDocs> findByTicklerIdDocType(Integer ticklerId, String docType);

    List<TicklerDocs> findByTicklerId(Integer ticklerId);
}
