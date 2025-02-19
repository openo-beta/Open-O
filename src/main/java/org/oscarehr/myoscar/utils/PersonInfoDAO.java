package org.oscarehr.myoscar.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class PersonInfoDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public PersonInfo getPersonById(Long id) {
        return entityManager.find(PersonInfo.class, id);
    }
}
