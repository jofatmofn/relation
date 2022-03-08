package org.sakuram.relation.valueobject;

import org.sakuram.relation.util.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PersonVO implements Comparable<PersonVO>{
	
	private String id;
	private String label;
	@JsonIgnore private String firstName;
	@JsonIgnore private String gender;
	private double size;
	private String color;
	private double x;
	private double y;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getNormalisedLabel() {
		return "(" + (id == null ? "" : id) + "/" + (gender == null ? "" : gender) + ")" + (firstName == null ? "" : firstName) +
				(label != null && (firstName == null || !firstName.startsWith(label) && !firstName.endsWith(label)) ? (firstName == null ? "" : "/") + label : "");
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String genderId) {
		if (genderId.equals(Constants.GENDER_NAME_MALE)) {
			this.gender = "M";
		} else if (genderId.equals(Constants.GENDER_NAME_FEMALE)) {
			this.gender = "F";
		} else {
			this.gender = "-";			
		}

	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int compareTo(PersonVO personVO) {
		return (this.getY() < personVO.getY() ? -1 : this.getY() > personVO.getY() ? 1 : this.getX() < personVO.getX() ? -1 : this.getX() == personVO.getX() ? 0 : 1);
	}
}
