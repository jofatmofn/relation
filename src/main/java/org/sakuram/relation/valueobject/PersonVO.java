package org.sakuram.relation.valueobject;

public class PersonVO {
	
	private String id;
	private String label;
	private String gender;
	private String size;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
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

	public void setAttribute(String attributeName, String attributeValue) {
		switch(attributeName) {
			case "label":
				setLabel(attributeValue);
				break;
			case "size":
				setSize(attributeValue);
				break;
			case "color":
				setColor(attributeValue);
				break;
			case "gender":
				setGender(attributeValue);
				break;
			default:
				System.out.println("Attribute " + attributeName + " ignored.");
		}
	}
	
}
