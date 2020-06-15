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
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Where;
import org.sakuram.relation.util.SecurityContext;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.annotation.JsonIgnore;

@EnableAutoConfiguration
@ComponentScan
@Entity
@Where(clause="overwritten_by_fk is null and deleter_fk is null")
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = "long")})
@Filter(name = "tenantFilter", condition = "tenant_fk = :tenantId")
@Table(name="relation")
public class Relation {

	@Id	 
	@SequenceGenerator(name="relation_seq_generator",sequenceName="relation_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="relation_seq_generator")
	@Column(name="id", nullable=false)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="person_1_fk", nullable=true)
	private Person person1;
	
	@ManyToOne
	@JoinColumn(name="person_2_fk", nullable=true)
	private Person person2;
	
	@ManyToOne
	@JoinColumn(name="tenant_fk", nullable=false)
	private Tenant tenant;
	
	@ManyToOne
	@JoinColumn(name="creator_fk", nullable=false)
	private AppUser creator;
	
	@Column(name="created_at", nullable=false, updatable=false)
	@CreationTimestamp
	private Timestamp createdAt;

	@OneToOne
	@JoinColumn(name="overwritten_by_fk", nullable=true)
	private Relation overwrittenBy;
	
	@ManyToOne
	@JoinColumn(name="deleter_fk", nullable=true)
	private AppUser deleter;
	
	@Column(name="deleted_at", nullable=true)
	private Timestamp deletedAt;

	@JsonIgnore
	@OneToMany(mappedBy="relation", cascade=CascadeType.ALL)
	@Where(clause="overwritten_by_fk is null and deleter_fk is null")
	private List<AttributeValue> attributeValueList;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Person getPerson1() {
		return person1;
	}

	public void setPerson1(Person person1) {
		this.person1 = person1;
	}

	public Person getPerson2() {
		return person2;
	}

	public void setPerson2(Person person2) {
		this.person2 = person2;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public AppUser getCreator() {
		return creator;
	}

	public void setCreator(AppUser creator) {
		this.creator = creator;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Relation getOverwrittenBy() {
		return overwrittenBy;
	}

	public void setOverwrittenBy(Relation overwrittenBy) {
		this.overwrittenBy = overwrittenBy;
	}

	public AppUser getDeleter() {
		return deleter;
	}

	public void setDeleter(AppUser deleter) {
		this.deleter = deleter;
	}

	public Timestamp getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Timestamp deletedAt) {
		this.deletedAt = deletedAt;
	}

	public List<AttributeValue> getAttributeValueList() {
		return attributeValueList;
	}

	public void setAttributeValueList(List<AttributeValue> attributeValueList) {
		this.attributeValueList = attributeValueList;
	}
	
	@PrePersist
	@PreUpdate
	public void prePersist() {
	    tenant = SecurityContext.getCurrentTenant();
	}
	
}
