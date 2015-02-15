package br.com.ufrn.msr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionMinerUtil {
	
	public static List<String> extractCodesFromBody(String body) {
		Pattern pattern = Pattern.compile("<code>(.*?)</code>");
		Matcher matcher = pattern.matcher(body);
		
		List<String> codes = new ArrayList<String>();
		
		while(matcher.find()) {
			codes.add(matcher.group(1));
		}
		
		return codes;
	}

}
