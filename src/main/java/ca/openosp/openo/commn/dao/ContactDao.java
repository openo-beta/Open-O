//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Contact;

public interface ContactDao extends AbstractDao<Contact> {
    public List<Contact> search(String searchMode, String orderBy, String keyword);
}
