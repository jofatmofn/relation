package org.sakuram.relation.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan
@Entity
@Table(name="domain_value")
public class DomainValue {

	@Id	 
	@Column(name="id", nullable=false)
	private long id;
	
	@Column(name="category", nullable=false)
	private String category;
	
	@Column(name="value", nullable=false)
	private String value;
	
	@Column(name="flags_csv", nullable=true)
	private String flagsCsv;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFlagsCsv() {
		return flagsCsv;
	}

	public void setFlagsCsv(String flagsCsv) {
		this.flagsCsv = flagsCsv;
	}
	
}
