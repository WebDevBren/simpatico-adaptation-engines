package eu.simpaticoproject.adaptation.common;

import java.util.HashMap;
import java.util.Map;

public class Utils {
	
	public static Map<String,String> handleError(Exception exception) {
		Map<String,String> errorMap = new HashMap<String,String>();
		errorMap.put("errorType", exception.getClass().getSimpleName());
		errorMap.put("errorMsg", exception.getMessage());
		return errorMap;
	}

}
