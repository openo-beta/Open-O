//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.LogLetters;
import org.springframework.stereotype.Repository;

@Repository
public class LogLettersDaoImpl extends AbstractDaoImpl<LogLetters> implements LogLettersDao {

    public LogLettersDaoImpl() {
        super(LogLetters.class);
    }
}
