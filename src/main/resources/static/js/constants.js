const ENTITY_TYPE_PERSON = 1;
const ENTITY_TYPE_RELATION = 2;

const CATEGORY_RELATION_NAME = "RelName";
const CATEGORY_RELATION_SUB_TYPE = "RelSubType";
const CATEGORY_PERSON_ATTRIBUTE = "PersAttribute";
const CATEGORY_RELATION_ATTRIBUTE = "RelAttribute";
const CATEGORY_LANGUAGE = "Language";
const CATEGORY_BOOLEAN = "Bool";

const FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED = "NA";
const FLAG_ATTRIBUTE_REPETITION_OVERLAPPING_ALLOWED = "OA";
const FLAG_ATTRIBUTE_REPETITION_NON_OVERLAPPING_ALLOWED = "NOA";
const FLAG_ATTRIBUTE_PRIVACY_RESTRICTION_PUBLIC_ONLY = "PB";
const FLAG_ATTRIBUTE_PRIVACY_RESTRICTION_PRIVATE_ONLY = "PR";
const FLAG_ATTRIBUTE_PRIVACY_RESTRICTION_INDIVIDUAL_CHOICE = "BR";

const DEFAULT_COLOR = "rgb(1,179,255)";
const HIGHLIGHT_COLOR = "rgb(179,179,179)";

const NEW_ENTITY_ID = "-1";
const SEARCH_ENTITY_ID = "-2";

const PERSON_ATTRIBUTE_DV_ID_FIRST_NAME = 20;
const PERSON_ATTRIBUTE_DV_ID_ANY_NAME = -6;
const RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2 = 34;
const RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1 = 35;
const RELATION_ATTRIBUTE_DV_ID_RELATION_SUB_TYPE = 36;

const ACTION_SAVE = "Save";
const ACTION_SEARCH = "Search";
const ACTION_RELATE = "Relate";

const VALID_RELATIONS_JSON = JSON.stringify([["1", "5"],["5", "1"],["1", "6"],["6", "1"],["2", "5"],["5", "2"],["2", "6"],["6", "2"],["3", "4"],["4", "3"]]);
const VALID_RELSUBTYPES_SPOUSE = ["7", "8", "9", "10", "11", "12"]
const VALID_RELSUBTYPES_PARENT_CHILD = ["13", "14", "15", "312", "313", "334", "335"]

const GENDER_MALE_DV_ID = 59;
const GENDER_FEMALE_DV_ID = 60;
const RELATION_NAME_FATHER_DV_ID = 1;
const RELATION_NAME_MOTHER_DV_ID = 2;
const RELATION_NAME_HUSBAND_DV_ID = 3;
const RELATION_NAME_WIFE_DV_ID = 4;
const RELATION_NAME_SON_DV_ID = 5;
const RELATION_NAME_DAUGHTER_DV_ID = 6;

const RELATION_GROUP_PARENT_CHILD = "PC"
const RELATION_GROUP_SPOUSE = "Sp"

const TRANSLATABLE_REGEX = "\\p{L}|[ \\u0BBE-\\u0BCD]";
const DEFAULT_LANGUAGE_DV_ID = 336;
const DEFAULT_LANGUAGE_CODE = "eng";
const DEFAULT_BOOLEAN = "True";

const ADDITIONAL_PERSON_ATTRIBUTES_ARRAY = [
  {id: -1, value: "labelPersonId", attributeDomain: "", validationJsRegEx: "\\d"},
  {id: -2, value: "labelParents", attributeDomain: "", validationJsRegEx: TRANSLATABLE_REGEX},
  {id: -3, value: "labelSpouses", attributeDomain: "", validationJsRegEx: TRANSLATABLE_REGEX},
  {id: -4, value: "labelChildren", attributeDomain: "", validationJsRegEx: TRANSLATABLE_REGEX},
  {id: -5, value: "labelSiblings", attributeDomain: "", validationJsRegEx: TRANSLATABLE_REGEX},
  {id: -6, value: "labelAnyName", attributeDomain: "", validationJsRegEx: TRANSLATABLE_REGEX},
];


var eng = {
	labelPersonId: "Person Id",
	labelParents: "Parents",
	labelSpouses: "Spouses",
	labelChildren: "Children",
	labelSiblings: "Siblings",
	labelAnyName: "Any Name",
	labelDetails: "Details",
	labelNewPerson: "new person",
	labelDetailsOfRelation: "Details of relation between",
	labelAnd: "and",
	labelPersonSearchCriteria: "Person Search Criteria",
	messageSearchResults: "Showing #partialCount# of #resultCount# persons. Provide more restricting criteria to limit the results.",
	labelPerson: "Person",
	labelPersons: "persons",
	labelValue: "Value",
	labelIsApproximate: "Approximate",
	labelStartDate: "Start Date",
	labelEndDate: "End Date",
	labelIsPrivate: "Private",
	labelIsMaybeNotRegistered: "Maybe NOT registered",
	labelDeletePerson: "Delete Person",
	labelDeleteRelation: "Delete Relation",
	labelDeleteProperty: "Delete Property",
	labelYetToBeAdded: "Yet to be added",
	labelYetToBeSearched: "Yet to be Searched",
	labelRelate: "Relate",
	labelRelatedPersons: "Related Persons",
	labelFather: "Father",
	labelMother: "Mother",
	labelSonDaughter: "Son/Daughter",
	labelHusband: "Husband",
	labelWife: "Wife",
	labelAscertain: "Ascertain",
	labelAscertainRelation: "Ascertain Relation",
	labelExcludeRelations: "Exclude Relations",
	labelSelect: "Select",
	labelUploadFunctionCheckDuplicates: "Check Duplicates",
	labelUploadFunctionStore: "Store",
	labelUpload: "Upload",
	labelPersonsNRelationsUpload: "Persons & Relations Upload",
	labelSave: "Save",
	labelSearch: "Search",
	labelIsLenient: "Lenient",
	labelParentChild: "Parent-Child",
	labelSpouse: "Spouse"
};

var tam = {
	labelPersonId: "நபர் அடையாள எண்",
	labelParents: "பெற்றோர்கள்",
	labelSpouses: "வாழ்க்கைத் துணைவர்கள்",
	labelChildren: "குழந்தைகள்",
	labelSiblings: "உடன்பிறந்தவர்கள்",
	labelAnyName: "ஏதாவதொரு பெயர்",
	labelDetails: "விபரங்கள்",
	labelNewPerson: "புது நபர்",
	labelDetailsOfRelation: "உறவின் விவரங்கள்",
	labelAnd: "மற்றும்",
	labelPersonSearchCriteria: "நபர் தேடல் அளவுகோல்கள்",
	messageSearchResults: "காண்பிக்கப்படும் நபர்கள்: #resultCount#ல் #partialCount#. குறைப்பதற்கு மேலும் கட்டுப்படுத்தும் அளவுகோல்களை வழங்கவும்.",
	labelPerson: "நபர்",
	labelPersons: "நபர்கள்",
	labelValue: "மதிப்பு",
	labelIsApproximate: "தோராயமானது",
	labelStartDate: "தொடக்க தேதி",
	labelEndDate: "கடைசி தேதி",
	labelIsPrivate: "அந்தரங்கம்",
	labelIsMaybeNotRegistered: "பதியப்படாமலிருக்கலாம்",
	labelDeletePerson: "நபரை நீக்கு",
	labelDeleteRelation: "உறவை நீக்கு",
	labelDeleteProperty: "பண்பை நீக்கு",
	labelYetToBeAdded: "சேர்க்கப்பட வேண்டிய நபர்",
	labelYetToBeSearched: "தேடப்பட வேண்டிய நபர்",
	labelRelate: "தொடர்பாக்கு",
	labelRelatedPersons: "தொடர்புடைய நபர்கள்",
	labelFather: "அப்பா ",
	labelMother: "அம்மா",
	labelSonDaughter: "மகன்/மகள்",
	labelHusband: "கணவன்",
	labelWife: "மனைவி",
	labelAscertain: "கண்டுபிடி",
	labelAscertainRelation: "உறவைக் கண்டுபிடி",
	labelExcludeRelations: "நீக்கவேண்டிய உறவுகள்",
	labelSelect: "தேர்ந்தெடு",
	labelUploadFunctionCheckDuplicates: "நகல் தேடு",
	labelUploadFunctionStore: "சேமி",
	labelUpload: "பதிவேற்று",
	labelPersonsNRelationsUpload: "நபர்கள் & உறவுகள் பதிவேற்று",
	labelSave: "சேமி",
	labelSearch: "தேடு",
	labelIsLenient: "துல்லியமில்லா",
	labelParentChild: "பெற்றோர்-பிள்ளை",
	labelSpouse: "வாழ்க்கைத் துணை"
};