//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public interface FormsDAO {

    public void saveForm(Object o);

    public Object getCurrentForm(String clientId, Class clazz);

    public List getFormInfo(String clientId, Class clazz);
}
 