// src/main/java/org/oscarehr/app/OAuth1Utils.java
//CHECKSTYLE:OFF
package org.oscarehr.app;

import java.util.List;

import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.common.model.AppUser;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;

/**
 * Deprecated shim for legacy OAuth1 outbound helpers.
 * The Scribe-based client flow has been removed; these methods now
 * log and return null to preserve binary compatibility.
 */
@Deprecated
public final class OAuth1Utils {
    private static final Logger logger = MiscUtils.getLogger();
    private static final JSONProvider<Object> jsonProvider = new JSONProvider<>();

    static {
        // keep previous CXF consumer behavior for callers that still pass this around
        jsonProvider.setDropRootElement(true);
    }

    private OAuth1Utils() {}

    /** Provide a CXF JSONProvider (legacy callers expect this). */
    public static List<Object> getProviderK2A() {
        return List.of(jsonProvider);
    }

    // --- static wrappers preserved for compatibility ---

    public static String getOAuthGetResponse(
        LoggedInInfo info,
        AppDefinition app,
        AppUser user,
        String requestURI,
        String baseRequestURI
    ) {
        logger.warn("OAuth1Utils.getOAuthGetResponse called, but OAuth1 client is disabled. URI={}", requestURI);
        return null;
    }

    public static String getOAuthPostResponse(
        LoggedInInfo info,
        AppDefinition app,
        AppUser user,
        String requestURI,
        String baseRequestURI,
        List<Object> providers,
        Object payload
    ) {
        logger.warn("OAuth1Utils.getOAuthPostResponse called, but OAuth1 client is disabled. URI={}", requestURI);
        return null;
    }

    public static void getOAuthDeleteResponse(
        AppDefinition app,
        AppUser user,
        String requestURI,
        String auditURI
    ) {
        logger.warn("OAuth1Utils.getOAuthDeleteResponse called, but OAuth1 client is disabled. URI={}", requestURI);
    }
}
