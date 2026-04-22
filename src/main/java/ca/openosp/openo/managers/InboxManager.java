//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import ca.openosp.openo.inbox.InboxManagerQuery;
import ca.openosp.openo.inbox.InboxManagerResponse;
import ca.openosp.openo.utility.LoggedInInfo;

public interface InboxManager {
    public static final String NORMAL = "normal";
    public static final String ALL = "all";
    public static final String ABNORMAL = "abnormal";
    public static final String LABS = "labs";
    public static final String DOCUMENTS = "documents";

    public InboxManagerResponse getInboxResults(LoggedInInfo loggedInInfo, InboxManagerQuery query);
}


