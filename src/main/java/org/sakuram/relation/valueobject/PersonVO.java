package org.sakuram.relation.valueobject;

import org.sakuram.relation.util.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PersonVO implements Comparable<PersonVO>{
	
	private String id;
	private String label;
	@JsonIgnore private String personLabel;
	@JsonIgnore private String firstName;
	@JsonIgnore private String gender;
	private double size;
	private String color;
	private double x;
	private double y;
	
	public void determineLabel() {
		label =  "(" + (id == null ? "" : id) + "/" + (gender == null ? "" : gender) + ")" + (firstName == null ? "" : firstName) +
				(firstName == null || personLabel  == null ? "" : "/") + (personLabel == null ? "" : personLabel);
	}

	public void determineGender(String genderId) {
		if (genderId.equals(Constants.GENDER_NAME_MALE)) {
			this.gender = "M";
		} else if (genderId.equals(Constants.GENDER_NAME_FEMALE)) {
			this.gender = "F";
		} else {
			this.gender = "-";			
		}
	}

	public int compareTo(PersonVO personVO) {
		return (this.getY() < personVO.getY() ? -1 : this.getY() > personVO.getY() ? 1 : this.getX() < personVO.getX() ? -1 : this.getX() == personVO.getX() ? 0 : 1);
	}
}
