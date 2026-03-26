//CHECKSTYLE:OFF

package ca.openosp.openo.daos.security;

import java.util.List;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class UserAccessDaoImpl extends HibernateDaoSupport implements UserAccessDao {

    @Override
    public List GetUserAccessList(String providerNo, Integer shelterId) {
        String sSQL = "";
        if (shelterId != null && shelterId.intValue() > 0) {
            String s = "'%S" + shelterId.toString() + ",%'";
            sSQL = "from UserAccessValue s where s.providerNo= ?0 " +
                    " and s.orgCdcsv like " + s + " order by s.functionCd, s.privilege desc, s.orgCd";
        } else {
            sSQL = "from UserAccessValue s where s.providerNo= ?0 " +
                    " order by s.functionCd, s.privilege desc, s.orgCd";
        }
        return getHibernateTemplate().find(sSQL, providerNo);
    }

    @Override
    public List GetUserOrgAccessList(String providerNo, Integer shelterId) {
        String sSQL = "";
        if (shelterId != null && shelterId.intValue() > 0) {
            sSQL = "select distinct o.codecsv from UserAccessValue s, LstOrgcd o " +
                    "where s.providerNo= ?0 and s.privilege>='r' and s.orgCd=o.code " +
                    " and o.codecsv like '%S" + shelterId.toString() + ",%'" +
                    " order by o.codecsv";
            return getHibernateTemplate().find(sSQL, providerNo);
        } else {
            sSQL = "select distinct o.codecsv from UserAccessValue s, LstOrgcd o where s.providerNo= ?0 and s.privilege>='r' and s.orgCd=o.code order by o.codecsv";
            return getHibernateTemplate().find(sSQL, providerNo);
        }
    }
}
