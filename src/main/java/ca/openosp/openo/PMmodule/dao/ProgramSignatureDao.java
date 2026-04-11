//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.model.ProgramSignature;

public interface ProgramSignatureDao {

    public ProgramSignature getProgramFirstSignature(Integer programId);

    public List<ProgramSignature> getProgramSignatures(Integer programId);

    public void saveProgramSignature(ProgramSignature programSignature);
}
