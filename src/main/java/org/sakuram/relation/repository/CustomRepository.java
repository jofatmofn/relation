package org.sakuram.relation.repository;

import java.util.List;

import org.sakuram.relation.bean.Person;

public interface CustomRepository {
	public List<Person> executeDynamicQuery(String query);

}
