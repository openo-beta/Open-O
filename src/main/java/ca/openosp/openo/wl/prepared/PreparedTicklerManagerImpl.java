//CHECKSTYLE:OFF

package ca.openosp.openo.wl.prepared;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

public class PreparedTicklerManagerImpl implements PreparedTicklerManager {

    static Logger log = MiscUtils.getLogger();

    private List<PreparedTickler> ticklers;

    public PreparedTicklerManagerImpl() {
        ticklers = new ArrayList<PreparedTickler>();
    }

    /* loads up the runtime plugins */
    public void setPath(String path) {
        ticklers.clear();
        File f = new File(path + "/WEB-INF/classes/org/caisi/tickler/prepared/runtime");
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int x = 0; x < files.length; x++) {
                String fileName = files[x].getName();
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                PreparedTickler pt = null;
                if ((pt = loadClass("org.caisi.tickler.prepared.runtime." + fileName)) != null) {
                    ticklers.add(pt);
                }
            }
        }

    }

    public PreparedTickler loadClass(String className) {
        PreparedTickler pt = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {
            pt = (PreparedTickler) cl.loadClass(className).newInstance();
        } catch (Exception e) {
            log.warn("Warning", e);
        }

        return pt;
    }

    public List<PreparedTickler> getTicklers() {
        return ticklers;
    }

    public PreparedTickler getTickler(String name) {
        for (int x = 0; x < ticklers.size(); x++) {
            PreparedTickler tickler = ticklers.get(x);
            if (tickler.getName().equals(name)) {
                return tickler;
            }
        }
        return null;
    }
}
