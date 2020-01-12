const ENTITY_TYPE_PERSON = 1;
const ENTITY_TYPE_RELATION = 2;

const CATEGORY_RELATION_NAME = "RelName";
const CATEGORY_RELATION_SUB_TYPE = "RelSubType";
const CATEGORY_PERSON_ATTRIBUTE = "PersAttribute";
const CATEGORY_RELATION_ATTRIBUTE = "RelAttribute";

const FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED = "NA";
const FLAG_ATTRIBUTE_REPETITION_OVERLAPPING_ALLOWED = "OA";
const FLAG_ATTRIBUTE_REPETITION_NON_OVERLAPPING_ALLOWED = "NOA";

const DEFAULT_COLOR = "rgb(1,179,255)";
const HIGHLIGHT_COLOR = "rgb(179,179,179)";

const NEW_ENTITY_ID = "-1";
const SEARCH_ENTITY_ID = "-2";

const PERSON_ATTRIBUTE_DV_ID_LABEL = 16;
const RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2 = 34;
const RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1 = 35;

const ACTION_SAVE = "Save";
const ACTION_SEARCH = "Search";
const ACTION_RELATE = "Relate";

const VALID_RELATIONS_JSON = JSON.stringify([["1", "5"],["5", "1"],["1", "6"],["6", "1"],["2", "5"],["5", "2"],["2", "6"],["6", "2"],["3", "4"],["4", "3"]]);
