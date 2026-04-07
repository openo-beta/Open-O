//CHECKSTYLE:OFF

package ca.openosp.openo.wl.prepared;

import java.util.List;

public interface PreparedTicklerManager {
    void setPath(String path);

    PreparedTickler loadClass(String className);

    List<PreparedTickler> getTicklers();

    PreparedTickler getTickler(String name);
}
