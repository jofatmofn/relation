package org.sakuram.relation.repository;

import org.sakuram.relation.bean.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Integer>{

}
