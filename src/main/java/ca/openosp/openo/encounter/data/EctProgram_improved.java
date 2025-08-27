//CHECKSTYLE:OFF
/**
 * Improved version of EctProgram that defaults to OSCAR program ID
 * instead of always returning "0"
 */

package ca.openosp.openo.encounter.data;

import org.oscarehr.PMmodule.dao.ProgramDao;
import org.oscarehr.PMmodule.dao.ProgramProviderDAO;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.util.SpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Improved implementation that properly defaults to the OSCAR program
 * when no specific program is configured for a provider
 */
public class EctProgram_improved {
    private HttpSession se;
    private static String cachedOscarProgramId = null;

    /**
     * Creates a new instance of EctProgram
     */
    public EctProgram_improved(HttpSession se) {
        this.se = se;
    }

    /**
     * Gets the program ID for a provider.
     * 
     * This improved implementation:
     * 1. First checks if the provider has an assigned program
     * 2. Falls back to the OSCAR program if it exists
     * 3. Only returns "0" if no OSCAR program exists (backwards compatibility)
     * 
     * @param providerNo The provider number
     * @return The program ID as a string
     */
    public String getProgram(String providerNo) {
        try {
            // First check if provider has an assigned program
            ProgramProviderDAO programProviderDao = SpringUtils.getBean(ProgramProviderDAO.class);
            if (providerNo != null && !providerNo.trim().isEmpty()) {
                List<ProgramProvider> programProviders = programProviderDao.getProgramProviderByProviderNo(providerNo);
                if (programProviders != null && !programProviders.isEmpty()) {
                    // Return the first active program for this provider
                    for (ProgramProvider pp : programProviders) {
                        if (pp.getProgramId() != null) {
                            return String.valueOf(pp.getProgramId());
                        }
                    }
                }
            }
            
            // No provider-specific program found, default to OSCAR program
            return getOscarProgramId();
            
        } catch (Exception e) {
            // Log error and return OSCAR program ID or "0" as fallback
            org.oscarehr.util.MiscUtils.getLogger().error("Error getting program for provider " + providerNo, e);
            return getOscarProgramId();
        }
    }
    
    /**
     * Gets the OSCAR program ID, with caching for performance
     * @return The OSCAR program ID or "0" if not found
     */
    private String getOscarProgramId() {
        // Use cached value if available
        if (cachedOscarProgramId != null) {
            return cachedOscarProgramId;
        }
        
        try {
            ProgramDao programDao = SpringUtils.getBean(ProgramDao.class);
            Program oscarProgram = programDao.getProgramByName("OSCAR");
            
            if (oscarProgram != null && oscarProgram.getId() != null) {
                cachedOscarProgramId = String.valueOf(oscarProgram.getId());
                return cachedOscarProgramId;
            }
        } catch (Exception e) {
            org.oscarehr.util.MiscUtils.getLogger().error("Error getting OSCAR program", e);
        }
        
        // Fallback to "0" for backwards compatibility
        return "0";
    }
    
    /**
     * Clears the cached OSCAR program ID (useful if program is created/modified)
     */
    public static void clearCache() {
        cachedOscarProgramId = null;
    }

    public ApplicationContext getAppContext() {
        return WebApplicationContextUtils.getWebApplicationContext(
                se.getServletContext());
    }
}