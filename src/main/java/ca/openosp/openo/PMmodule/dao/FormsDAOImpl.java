//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.model.FormInfo;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class FormsDAOImpl extends HibernateDaoSupport implements FormsDAO {

    private Logger log = MiscUtils.getLogger();

    public void saveForm(Object o) {
        this.getHibernateTemplate().save(o);

        if (log.isDebugEnabled()) {
            log.debug("saveForm:" + o);
        }
    }

    public Object getCurrentForm(String clientId, Class clazz) {
        Object result = null;

        if (clientId == null || clazz == null) {
            throw new IllegalArgumentException();
        }

        String className = clazz.getName();
        if (className.indexOf(".") != -1) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        String sSQL = "from ?0 f where f.DemographicNo=?1";
        List results = this.getHibernateTemplate().find(sSQL, new Object[]{className, clientId});
        if (results.size() > 0) {
            result = results.get(0);
        }

        if (log.isDebugEnabled()) {
            log.debug("getCurrentForm: clientId=" + clientId + ",class=" + clazz + ",found=" + (result != null));
        }

        return result;
    }

    public List getFormInfo(String clientId, Class clazz) {
        if (clientId == null || clazz == null) {
            throw new IllegalArgumentException();
        }

        List<FormInfo> formInfos = new ArrayList<FormInfo>();
        String className = clazz.getName();
        if (className.indexOf(".") != -1) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        String sSQL = "select f.id,f.ProviderNo,f.FormEdited from ?0 f where f.DemographicNo=?1 order by f.FormEdited DESC";
        Object[] params = new Object[] {
            className,
            Long.valueOf(clientId)
        };
        List results = this.getHibernateTemplate().find(sSQL, params);
        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
            FormInfo fi = new FormInfo();
            Object[] values = (Object[]) iter.next();
            Long id = (Long) values[0];
            Long providerNo = (Long) values[1];
            Date dateEdited = (Date) values[2];
            Provider provider = this.getHibernateTemplate().get(Provider.class, String.valueOf(providerNo));
            fi.setFormId(id);
            fi.setProviderNo(providerNo);
            fi.setFormDate(dateEdited);
            fi.setProviderName(provider.getFormattedName());
            formInfos.add(fi);
        }

        if (log.isDebugEnabled()) {
            log.debug("getFormInfo: clientId=" + clientId + ",class=" + clazz + ",# of results=" + formInfos.size());
        }

        return formInfos;
    }
}
