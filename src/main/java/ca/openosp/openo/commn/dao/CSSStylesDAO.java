//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CssStyle;

public interface CSSStylesDAO extends AbstractDao<CssStyle> {
    List<CssStyle> findAll();
}
