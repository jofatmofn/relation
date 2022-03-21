package org.sakuram.relation.bean;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

@EnableAutoConfiguration
@ComponentScan
@Entity
@Where(clause="overwritten_by_fk is null and deleter_fk is null")
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = "long")})
@Filter(name = "tenantFilter", condition = "tenant_fk = :tenantId")
@Table(name="attribute_value")
public class AttributeValue {

	@Id	 
	@SequenceGenerator(name="attribute_value_seq_generator",sequenceName="attribute_value_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="attribute_value_seq_generator")
	@Column(name="id", nullable=false)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="person_fk", nullable=true)
	private Person person;
	
	@ManyToOne
	@JoinColumn(name="relation_fk", nullable=true)
	private Relation relation;
	
	@ManyToOne
	@JoinColumn(name="attribute_fk", nullable=false)
	private DomainValue attribute;
	
	@Column(name="attribute_value", nullable=false)
	private String attributeValue;
	
	@Column(name="is_value_accurate", nullable=false)
	private boolean isValueAccurate;
	
	@Column(name="start_date", nullable=true)
	private Date startDate;
	
	@Column(name="end_date", nullable=true)
	private Date endDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="tenant_fk", nullable=false)
	private Tenant tenant;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="creator_fk", nullable=false)
	private AppUser creator;
	
	@Column(name="created_at", nullable=false, updatable=false)
	@CreationTimestamp
	private Timestamp createdAt;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="overwritten_by_fk", nullable=true)
	private AttributeValue overwrittenBy;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="deleter_fk", nullable=true)
	private AppUser deleter;
	
	@Column(name="deleted_at", nullable=true)
	private Timestamp deletedAt;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public DomainValue getAttribute() {
		return attribute;
	}

	public void setAttribute(DomainValue attribute) {
		this.attribute = attribute;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public boolean isValueAccurate() {
		return isValueAccurate;
	}

	public void setValueAccurate(boolean isValueAccurate) {
		this.isValueAccurate = isValueAccurate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
		this.creator= creator;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public AttributeValue getOverwrittenBy() {
		return overwrittenBy;
	}

	public void setOverwrittenBy(AttributeValue overwrittenBy) {
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

	public AttributeValue() {
		
	}
	
	public AttributeValue(DomainValue attribute, String attributeValue, Person person, Relation relation) {
		this.attribute = attribute;
		this.attributeValue = attributeValue;
		this.person = person;
		this.relation = relation;
		this.creator = SecurityContext.getCurrentUser();
	}
	
	@PrePersist
	@PreUpdate
	public void prePersist() {
	    tenant = SecurityContext.getCurrentTenant();
	}
	
}
