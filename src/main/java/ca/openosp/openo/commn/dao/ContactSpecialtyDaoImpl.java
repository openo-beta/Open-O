//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ContactSpecialty;
import org.springframework.stereotype.Repository;

@Repository
public class ContactSpecialtyDaoImpl extends AbstractDaoImpl<ContactSpecialty> implements ContactSpecialtyDao {

    protected ContactSpecialtyDaoImpl() {
        super(ContactSpecialty.class);
    }

    @Override
    public List<ContactSpecialty> findAll() {
        Query findAll = entityManager.createNamedQuery("ContactSpecialty.findAll");
        List<ContactSpecialty> contactSpecialtyList = findAll.getResultList();
        return contactSpecialtyList;
    }

    @Override
    public ContactSpecialty findBySpecialty(String specialtyName) {
        Query query = entityManager.createQuery("SELECT s FROM ContactSpecialty s WHERE s.specialty LIKE ?1");
        query.setParameter(1, specialtyName);
        ContactSpecialty contactSpecialty = getSingleResultOrNull(query);
        return contactSpecialty;
    }

}
