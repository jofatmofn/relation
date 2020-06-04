package org.sakuram.relation.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="tenant")
public class Tenant {

	@Id
	@SequenceGenerator(name="tenant_seq_generator",sequenceName="tenant_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="tenant_seq_generator")
	@Column(name="id", nullable=false)
	private long id;
	
	@Column(name="project_id", nullable=false)
	private String projectId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
}
