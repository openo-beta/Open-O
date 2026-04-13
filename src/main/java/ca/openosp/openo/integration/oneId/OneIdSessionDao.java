package ca.openosp.openo.integration.oneId;

import ca.openosp.openo.commn.dao.AbstractDao;
import org.springframework.stereotype.Repository;

@Repository
public class OneIdSessionDao extends AbstractDao<OneIdSession> {
  public OneIdSessionDao() {
    super(OneIdSession.class);
  }
}
