//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ContactSpecialty;

public interface ContactSpecialtyDao extends AbstractDao<ContactSpecialty> {

    public List<ContactSpecialty> findAll();

    public ContactSpecialty findBySpecialty(String specialtyName);
}
