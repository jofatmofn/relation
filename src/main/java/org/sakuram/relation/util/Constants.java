package org.sakuram.relation.util;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Constants {
	
	public static final byte ENTITY_TYPE_PERSON = (byte)1;
	public static final byte ENTITY_TYPE_RELATION = (byte)2;
	
	public static final String CATEGORY_RELATION_NAME = "RelName";
	public static final String CATEGORY_RELATION_SUB_TYPE = "RelSubType";
	public static final String CATEGORY_PERSON_ATTRIBUTE = "PersAttribute";
	public static final String CATEGORY_RELATION_ATTRIBUTE = "RelAttribute";

	public static final long PERSON_ATTRIBUTE_DV_ID_LABEL = 16;
	public static final long PERSON_ATTRIBUTE_DV_ID_GENDER = 19;
	public static final long PERSON_ATTRIBUTE_DV_ID_FIRST_NAME = 20;
	public static final long PERSON_ATTRIBUTE_DV_ID_SUR_NAME = 22;
	public static final long RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2 = 34;
	public static final long RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1 = 35;
	public static final long RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2 = 61;
	public static final long RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1 = 62;
	public static final long RELATION_ATTRIBUTE_DV_ID_RELATION_SUB_TYPE = 36;
	
	public static final String RELATION_NAME_FATHER = "1";
	public static final String RELATION_NAME_MOTHER = "2";
	public static final String RELATION_NAME_HUSBAND = "3";
	public static final String RELATION_NAME_WIFE = "4";
	public static final String RELATION_NAME_SON = "5";
	public static final String RELATION_NAME_DAUGHTER = "6";
	public static final String RELATION_NAME_BROTHER = "316";
	public static final String RELATION_NAME_SISTER = "317";
	public static final String RELATION_NAME_FATHER_IN_LAW = "318";
	public static final String RELATION_NAME_MOTHER_IN_LAW = "319";
	public static final String RELATION_NAME_SON_IN_LAW = "320";
	public static final String RELATION_NAME_DAUGHTER_IN_LAW = "321";
	public static final String RELATION_NAME_UNCLE = "324";
	public static final String RELATION_NAME_AUNT = "325";
	public static final String RELATION_NAME_NEPHEW = "326";
	public static final String RELATION_NAME_NIECE = "327";
	public static final String RELATION_NAME_COUSIN_BROTHER = "328";
	public static final String RELATION_NAME_COUSIN_SISTER = "329";
	public static Map<String, String> RELATION_NAME_TO_ID_MAP = Stream.of(new String[][] {
		  { "Father", "1" },
		  { "Mother", "2" },
		  { "Husband", "3" },
		  { "Wife", "4" },
		  { "Son", "5" },
		  { "Daughter", "6" },
		  { "Brother", "316" },
		  { "Sister", "317" },
		  { "Father-in-law", "318" },
		  { "Mother-in-law", "319" },
		  { "Son-in-law", "320" },
		  { "Daughter-in-law", "321" },
		  { "Uncle", "324" },
		  { "Aunt", "325" },
		  { "Nephew", "326" },
		  { "Niece", "327" },
		  { "Cousin-brother", "328" },
		  { "Cousin-sister", "329" },
		  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));	// TODO: Read it from DB?
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
	public static final int FLAG_POSITION_VALIDATION_JS_REG_EX = 4;

	public static final String EDGE_TYPE_DIRECT_RELATION = "line";
	public static final String EDGE_TYPE_SIMPLIFIED_RELATION = "dotCurve";
	
	public static final String CSV_SEPARATOR = ",";
	public static final long NEW_ENTITY_ID = -1L;
	public static final String DEFAULT_COLOR = "rgb(1,179,255)";
	public static final long DELETED_ATTRIBUTE_VALUE_ID = -1L;
	public static final int SEARCH_RESULTS_MAX_COUNT = 20;
	public static final String RELATION_LABEL_TEMPLATE = "@@34@@(@@61@@)-@@35@@(@@62@@)(@@36@@)"; // TODO: Replace the hard-coded value with Constant
	public static final short EXPORT_TREE_MAX_DEPTH = 10;
	
	public static final String SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID = "projectId";
	public static final String SESSION_ATTRIBUTE_USER_SURROGATE_ID = "userId";
	
	public static final long ROLE_DV_ID_CREATOR = 332;
	public static final long ROLE_DV_ID_COLLABORATOR = 333;
}
