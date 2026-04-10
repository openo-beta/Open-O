//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import ca.openosp.openo.commn.model.Favorite;

public interface FavoriteDao extends AbstractDao<Favorite> {
    List<Favorite> findByProviderNo(String providerNo);
    Favorite findByEverything(String providerNo, String favoriteName, String bn, String gcn_SEQNO, String customName, float takeMin, float takeMax, String frequencyCode, String duration, String durationUnit, String quantity, int repeat, boolean nosubsInt, boolean prnInt, String parsedSpecial, String gn, String unitName, boolean customInstr);
}
