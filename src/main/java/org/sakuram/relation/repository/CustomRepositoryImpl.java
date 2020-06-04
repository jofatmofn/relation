package org.sakuram.relation.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.sakuram.relation.bean.Person;
import org.springframework.stereotype.Repository;

@Repository
public class CustomRepositoryImpl implements CustomRepository {
    @PersistenceContext
    public EntityManager entityManager;

    public Session getSession() {
    	return entityManager.unwrap(Session.class);
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public List<Person> executeDynamicQuery(String queryString) {
    	LogManager.getLogger().debug(queryString);
    	Query query;
    	query = entityManager.createNativeQuery(queryString, Person.class);
    	return query.getResultList();
	}

}
