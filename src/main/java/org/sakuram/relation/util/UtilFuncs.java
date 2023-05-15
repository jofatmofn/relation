package org.sakuram.relation.util;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilFuncs {
	private static final List<String> SINGLE_ATTRIBUTE_CLASS_NAME_LIST = Arrays.asList(new String[] {"java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.String", "java.lang.Double"});
	private static final float EPSILON = 0.00000096F;
	
    @SuppressWarnings("unchecked")
	public static <T> void listSet(List<T> list, float sequenceNo, T valueToBeAdded, T fillValue) {
    	int position;
    	if (sequenceNo == (int)sequenceNo) {
    		position = (int)sequenceNo;
    	} else {
    		position = (int)sequenceNo + (int)((sequenceNo - (float)(int)sequenceNo) * 10 + EPSILON) - 1;
    		// It is assumed here that the decimal part is just a single digit
    	}
    	for (int i = list.size(); i <= position; i++) {
    		try {
    			if (fillValue == null) {
    				list.add(null);
    			}
    			else if (SINGLE_ATTRIBUTE_CLASS_NAME_LIST.contains(fillValue.getClass().getName())) {
    				list.add(fillValue);
    			}
    			else {
    				list.add((T)(fillValue.getClass().newInstance()));
    			}
    		}
    		catch(InstantiationException | IllegalAccessException e) {
    			// TODO:
    		}
    	}
    	list.set(position, valueToBeAdded);
    }
    
    public static <T> T listGet(List<T> list, int position, T fillValue) {
    	if (position < list.size()) {
    		return list.get(position);
    	}
    	else {
    		return fillValue;
    	}
    }
    
    public static boolean dateEquals(Date date1, Date date2) {
    	if (date1 == null && date2 == null) return true;
    	if (date1 == null && date2 != null || date1 != null && date2 == null) return false;
    	// java.sql.Date toString() return format: yyyy-mm-dd
    	if (date1.toString().equals(date2.toString())) return true;
    	return false;
    }

    /*
     * TamNamSim: For similar names search with Tamil Names
     */
    public static List<String> normaliseForSearch(String inStr) {
    	List<String> wordsList, outStrList;
    	StringBuilder sb;
    	char[] inCharArray;
    	int inputLength;
    	
    	wordsList = new ArrayList<String>();
    	inputLength = inStr.length();
    	sb = new StringBuilder(inputLength);
    	inCharArray = inStr.toLowerCase().toCharArray();
    	for (int i = 0; i < inputLength; i++) {
    		if (inCharArray[i] == 'y') {
				sb.append('i');
    			if (i < inputLength - 1 && inCharArray[i+1] == 'y') {
    				i++; // Skip second y
    			}
    		} else if (i < inputLength - 1 && inCharArray[i] != 'a' && inCharArray[i] != 'e' && inCharArray[i] != 'i' && inCharArray[i] != 'o'  && inCharArray[i] != 'u' && inCharArray[i+1] == 'h') {
    			if (inCharArray[i] == 'd') {
        			sb.append('t');
    			} else {
    				sb.append(inCharArray[i]);
    			}
    			i++;	// Skip h
    		} else if (i < inputLength - 1 && (inCharArray[i] == ' ' || inCharArray[i] == '.') && (inCharArray[i+1] == ' ' || inCharArray[i+1] == '.')) {
    			// Skip current occurrence
    		} else if (inCharArray[i] == ' ' || inCharArray[i] == '.') {
    			if (sb.length() > 0) {
    				wordsList.add(normaliseWordLevel(sb.toString()));
    				sb = new StringBuilder(inputLength);
    			}
    		} else if (inCharArray[i] == 'd') {
    			sb.append('t');
    		} else if (i < inputLength - 1 && inCharArray[i] == 'e' && inCharArray[i+1] == 'e') {
    			sb.append('i');
    			i++;	// Skip second e
    		} else if (i < inputLength - 1 && inCharArray[i] == 'o' && inCharArray[i+1] == 'o') {
    			sb.append('u');
    			i++;	// Skip second o
    		} else if (i < inputLength - 1 && inCharArray[i] == 'z' && inCharArray[i+1] == 'h') {
    			sb.append('l');
    			i++;	// Skip next
    		} else if (inCharArray[i] == 'e') {
    			sb.append('a');
    		} else if (i < inputLength - 1 && (inCharArray[i] == 'a' || inCharArray[i] == 'k' || inCharArray[i] == 'r' || inCharArray[i] == 's') && inCharArray[i] == inCharArray[i+1]) {
    			sb.append(inCharArray[i]);
    			i++;	// Skip second occurrence
    		} else if (i < inputLength - 2 && inCharArray[i] == 's' && inCharArray[i+1] == 'w' && inCharArray[i+2] == 'a') {
    			sb.append("sa");
    			i += 2;	// Skip wa
    		} else if (i < inputLength - 2 && inCharArray[i] == 'k' && inCharArray[i+1] == 's' && inCharArray[i+2] == 'h') {
    			sb.append("x");
    			i += 2;	// Skip sh
    		} else {
    			sb.append(inCharArray[i]);
    		}
    	}
		if (sb.length() > 0) {
			wordsList.add(normaliseWordLevel(sb.toString()));
		}
    	
    	outStrList = new ArrayList<String>();
		sb = new StringBuilder(inputLength);
		// Same order of names, no permutations
		// TODO: All combinations with all values of k: 1 to outStrList.size()
    	for (int i = 0; i < wordsList.size(); i++) {	// k = outStrList.size()
    		sb.append(wordsList.get(i));
    	}
    	outStrList.add(sb.toString());
    	return outStrList;
    	
    }
    
    private static String normaliseWordLevel(String word) {
    	if (word.endsWith("an")) {
    		return word.substring(0, word.length() - 2);
    	} else {
    		return word;
    	}
    }
    
}
