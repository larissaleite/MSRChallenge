package br.com.ufrn.msr;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataAnalyzer {
	
	private static String regex = "\\<(.*?)\\>";
	
	private static int tagsCount = 0;
	
	private static int questionsCount = 0;
	
	private static DecimalFormat decimalFormat = new DecimalFormat("0.00");
	
	@SuppressWarnings("rawtypes")
	public static void showStatsTagsVsTags() {
		
		Map<String, Integer> mapTags = getMapTags();
		
		int count = 0;
		
		mapTags = HashMapUtil.sortByValues(mapTags);
		Iterator<Entry<String, Integer>> it = mapTags.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + "     " + pairs.getValue() + "      " + decimalFormat.format(Double.valueOf(pairs.getValue().toString())/(double)tagsCount));
	        count++;
	        if (count == 15) {
	        	break;
	        }
	    }
		
	}

	@SuppressWarnings("rawtypes")
	public static void showStatsTagsPerQuestion() {
		
		Map<String, Integer> mapTags = getMapTags();
		
		int count = 0;
		
		mapTags = HashMapUtil.sortByValues(mapTags);
		Iterator<Entry<String, Integer>> it = mapTags.entrySet().iterator();
	    while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + "     " + pairs.getValue() + "      " + decimalFormat.format(Double.valueOf(pairs.getValue().toString())/(double)questionsCount));
	        count++;
	        if (count == 15) {
	        	break;
	        }
	    }
		
	}
	
	private static Map<String, Integer> getMapTags() {
		List<String> allTags = getTagsFromPosts();
		
		questionsCount = allTags.size();
		
		Map<String, Integer> mapTags = new HashMap<String, Integer>();
		
		for (String tags : allTags) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(tags);
							
			while (m.find()) {
				tagsCount++;
				String tag = m.group(1);
				if (mapTags.containsKey(tag)) {
					mapTags.put(tag, mapTags.get(tag) + 1);
				} else {
					mapTags.put(tag, 1);
				}
			}
		}
		
		return mapTags;
	}
	
	private static List<String> getTagsFromPosts() {
		return PostMySQLDAO.getAllTags();
	}
	
	private static List<Integer> getAcceptedAnswersFromPosts() {
		return PostMySQLDAO.getAllAcceptedAnswersId();
	}
	
	public static void showStatsAcceptedAnswer() {
		List<Integer> allAcceptedAnswersId = getAcceptedAnswersFromPosts();
		
		int idCount = 0;
		
		for (Integer id : allAcceptedAnswersId) {
			if (id != -1) {
				idCount++;
			}
		}
		
		System.out.println("% of accepted answers: ");
		System.out.println(idCount/(float)allAcceptedAnswersId.size());
		
	}

}
