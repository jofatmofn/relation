package org.sakuram.relation.util;

public class Constants {
	
	public static byte ENTITY_TYPE_PERSON = (byte)1;
	public static byte ENTITY_TYPE_RELATION = (byte)2;
	
	public static String CATEGORY_RELATION_NAME = "RelName";
	public static String CATEGORY_RELATION_SUB_TYPE = "RelSubType";
	public static String CATEGORY_PERSON_ATTRIBUTE = "PersAttribute";
	public static String CATEGORY_RELATION_ATTRIBUTE = "RelAttribute";
	
	public static String FLAG_RELATION_TYPE_PARENT_CHILD = "PC";
	public static String FLAG_RELATION_TYPE_SPOUSE = "Sp";
	
	public static String FLAG_ATTRIBUTE_DOMAIN_LANGUAGE = "Language";
	public static String FLAG_ATTRIBUTE_DOMAIN_CITY = "City";
	public static String FLAG_ATTRIBUTE_DOMAIN_COUNTRY = "Country";
	public static String FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED = "NA";
	public static String FLAG_ATTRIBUTE_REPETITION_OVERLAPPING_ALLOWED = "OA";
	public static String FLAG_ATTRIBUTE_REPETITION_NON_OVERLAPPING_ALLOWED = "NOA";

	public static int FLAG_POSITION_RELATION_TYPE = 0;
	public static int FLAG_POSITION_INPUT_AS_ATTRIBUTE = 0;
	public static int FLAG_POSITION_REPETITION = 1;
	public static int FLAG_POSITION_DOMAIN = 2;
	
	public static String CSV_SEPARATOR = ",";
}
