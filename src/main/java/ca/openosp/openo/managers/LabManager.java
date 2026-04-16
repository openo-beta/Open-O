//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.nio.file.Path;
import java.util.List;

import ca.openosp.openo.commn.model.Hl7TextInfo;
import ca.openosp.openo.commn.model.Hl7TextMessage;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;


public interface LabManager {
    public List<Hl7TextMessage> getHl7Messages(LoggedInInfo loggedInInfo, Integer demographicNo, int offset, int limit);

    public List<Hl7TextInfo> getHl7TextInfo(LoggedInInfo loggedInInfo, int demographicNo);

    public Hl7TextMessage getHl7Message(LoggedInInfo loggedInInfo, int labId);

    public Path renderLab(LoggedInInfo loggedInInfo, Integer segmentId) throws PDFGenerationException;
}
