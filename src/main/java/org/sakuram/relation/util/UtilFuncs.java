package org.sakuram.relation.util;

import java.util.Arrays;
import java.util.List;

public class UtilFuncs {
	private static final List<String> SINGLE_ATTRIBUTE_CLASS_NAME_LIST = Arrays.asList(new String[] {"java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.String", "java.lang.Double"});

    @SuppressWarnings("unchecked")
	public static <T> void listSet(List<T> list, int position, T valueToBeAdded, T fillValue) {
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
}
