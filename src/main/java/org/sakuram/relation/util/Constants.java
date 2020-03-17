package org.sakuram.relation.util;

public class Constants {
	
	public static final byte ENTITY_TYPE_PERSON = (byte)1;
	public static final byte ENTITY_TYPE_RELATION = (byte)2;
	
	public static final String CATEGORY_RELATION_NAME = "RelName";
	public static final String CATEGORY_RELATION_SUB_TYPE = "RelSubType";
	public static final String CATEGORY_PERSON_ATTRIBUTE = "PersAttribute";
	public static final String CATEGORY_RELATION_ATTRIBUTE = "RelAttribute";

	public static final long PERSON_ATTRIBUTE_DV_ID_LABEL = 16;
	public static final long PERSON_ATTRIBUTE_DV_ID_GENDER = 19;
	public static final long RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2 = 34;
	public static final long RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1 = 35;
	
	public static final String RELATION_NAME_FATHER = "1";
	public static final String RELATION_NAME_MOTHER = "2";
	public static final String RELATION_NAME_HUSBAND = "3";
	public static final String RELATION_NAME_WIFE = "4";
	public static final String RELATION_NAME_SON = "5";
	public static final String RELATION_NAME_DAUGHTER = "6";
	
	public static final String FLAG_RELATION_TYPE_PARENT_CHILD = "PC";
	public static final String FLAG_RELATION_TYPE_SPOUSE = "Sp";
	
	public static final String FLAG_ATTRIBUTE_DOMAIN_LANGUAGE = "Language";
	public static final String FLAG_ATTRIBUTE_DOMAIN_CITY = "City";
	public static final String FLAG_ATTRIBUTE_DOMAIN_COUNTRY = "Country";
	public static final String FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED = "NA";
	public static final String FLAG_ATTRIBUTE_REPETITION_OVERLAPPING_ALLOWED = "OA";
	public static final String FLAG_ATTRIBUTE_REPETITION_NON_OVERLAPPING_ALLOWED = "NOA";

	public static final int FLAG_POSITION_RELATION_TYPE = 0;
	public static final int FLAG_POSITION_INPUT_AS_ATTRIBUTE = 0;
	public static final int FLAG_POSITION_REPETITION = 1;
	public static final int FLAG_POSITION_DOMAIN = 2;
	public static final int FLAG_POSITION_INPUT_MANDATORY = 3;
	
	public static final String CSV_SEPARATOR = ",";
	public static final long NEW_ENTITY_ID = -1L;
	public static final String DEFAULT_COLOR = "rgb(1,179,255)";
	public static final long DELETED_ATTRIBUTE_VALUE_ID = -1L;
	public static final int SEARCH_RESULTS_MAX_COUNT = 20;
}
