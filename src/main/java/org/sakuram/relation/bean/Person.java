package org.sakuram.relation.bean;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.annotation.JsonIgnore;

@EnableAutoConfiguration
@ComponentScan
@Entity
@Table(name="person")
public class Person {

	@Id	 
	@SequenceGenerator(name="person_seq_generator",sequenceName="person_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="person_seq_generator")
	@Column(name="id", nullable = false)
	private long id;
	
	@Column(name="login_id", nullable=true, unique=true)
	private String loginId;
	
	@ManyToOne
	@JoinColumn(name="creator_id", nullable=false)
	private Person creatorId;
	
	@Column(name="created_at", nullable=false, updatable=false)
	@CreationTimestamp
	private Timestamp createdAt;
	
	@JsonIgnore
	@OneToMany(mappedBy="person", cascade=CascadeType.ALL)
	private List<AttributeValue> attributeValueList;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public Person getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Person creatorId) {
		this.creatorId = creatorId;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public List<AttributeValue> getAttributeValueList() {
		return attributeValueList;
	}

	public void setAttributeValueList(List<AttributeValue> attributeValueList) {
		this.attributeValueList = attributeValueList;
	}

}
