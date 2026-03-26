//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CssStyle;
import org.springframework.stereotype.Repository;

@Repository
public class CSSStylesDaoImpl extends AbstractDaoImpl<CssStyle> implements CSSStylesDAO {

    public CSSStylesDaoImpl() {
        super(CssStyle.class);
    }

    @SuppressWarnings("unchecked")
    public List<CssStyle> findAll() {
        String sql = "select css from CssStyle css where css.status = ?1 order by css.name";
        Query q = entityManager.createQuery(sql);
        q.setParameter(1, CssStyle.ACTIVE);

        return q.getResultList();
    }
}
