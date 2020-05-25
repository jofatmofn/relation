package org.sakuram.relation.repository;

import java.util.List;
import java.util.Set;

import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelationRepository extends JpaRepository<Relation, Long>{
	List<Relation> findByPerson1(Person person1);
	List<Relation> findByPerson2(Person person2);
	Relation findByPerson1AndPerson2(Person person1, Person person2);	// TODO: If more than 1 entity instance is returned?
	List<Relation> findByPerson1InAndPerson2In(Set<Person> person1Set, Set<Person> person2Set);
}
