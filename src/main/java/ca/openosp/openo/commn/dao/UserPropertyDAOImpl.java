//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.UserProperty;
import org.springframework.stereotype.Repository;

/**
 * @author rjonasz
 */
@Repository
public class UserPropertyDAOImpl extends AbstractDaoImpl<UserProperty> implements UserPropertyDAO {

    /**
     * Creates a new instance of UserPropertyDAO
     */
    public UserPropertyDAOImpl() {
        super(UserProperty.class);
    }


    @Override
    public void delete(UserProperty prop) {
        if (prop != null && prop.getId() != null) {
            remove(prop.getId());
        }
    }
    

    public void saveProp(String provider, String userPropertyName, String value) {
        UserProperty prop = getProp(provider, userPropertyName);
        if (prop == null) {
            prop = new UserProperty();
            prop.setProviderNo(provider);
            prop.setName(userPropertyName);
        }
        prop.setValue(value);
        saveProp(prop);
    }


    public void saveProp(UserProperty prop) {
        if (prop.getId() != null && prop.getId().intValue() > 0) {
            merge(prop);
        } else {
            persist(prop);
        }
    }

    //Should properties be updateable?
    public void saveProp(String name, String val) {
        if (val != null) {
            UserProperty prop = getProp(name);
            if (prop == null) {
                prop = new UserProperty();
                prop.setName(name);
            }
            prop.setValue(val);
            saveProp(prop);
        }
    }

    public String getStringValue(String provider, String propertyName) {
        try {
            return getProp(provider, propertyName).getValue();
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserProperty> getAllProperties(String name, List<String> list) {
        Query query = entityManager.createQuery("select p from UserProperty p where p.name = ?1 and p.provider_no in ?2");
        query.setParameter(1, name);
        query.setParameter(2, list);

        return query.getResultList();
    }

    public List<UserProperty> getPropValues(String name, String value) {
        Query query = entityManager.createQuery("select p from UserProperty p where p.name = ?1 and p.value = ?2");
        query.setParameter(1, name);
        query.setParameter(2, value);

        return query.getResultList();
    }

    public UserProperty getProp(String prov, String name) {
        Query query = entityManager.createQuery("select p from UserProperty p where p.providerNo = ?1 and p.name = ?2");
        query.setParameter(1, prov);
        query.setParameter(2, name);

        @SuppressWarnings("unchecked")
        List<UserProperty> list = query.getResultList();
        if (list != null && list.size() > 0) {
            UserProperty prop = list.get(0);
            return prop;
        } else
            return null;
    }

    public UserProperty getProp(String name) {
        Query query = entityManager.createQuery("select p from UserProperty p where p.name = ?1");
        query.setParameter(1, name);

        @SuppressWarnings("unchecked")
        List<UserProperty> list = query.getResultList();
        if (list != null && list.size() > 0) {
            UserProperty prop = list.get(0);
            return prop;
        } else
            return null;
    }

    public List<UserProperty> getDemographicProperties(String providerNo) {
        Query query = entityManager.createQuery("select p from UserProperty p where p.providerNo = ?1");
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<UserProperty> list = query.getResultList();

        return list;
    }

    public Map<String, String> getProviderPropertiesAsMap(String providerNo) {
        Map<String, String> map = new HashMap<String, String>();

        Query query = entityManager.createQuery("select p from UserProperty p where p.providerNo = ?1");
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<UserProperty> list = query.getResultList();
        for (UserProperty p : list) {
            map.put(p.getName(), p.getValue());
        }
        return map;
    }

    public void saveProperties(String providerNo, Map<String, String> props) {
        for (String key : props.keySet()) {
            String value = props.get(key);
            if (value == null) value = new String();
            UserProperty prop = null;
            if ((prop = this.getProp(providerNo, key)) != null) {
                prop.setValue(value);
            } else {
                prop = new UserProperty();
                prop.setName(key);
                prop.setProviderNo(providerNo);
                prop.setValue(value);
            }
            saveProp(prop);
        }
    }
}
