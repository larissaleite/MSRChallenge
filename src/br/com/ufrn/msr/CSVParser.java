package br.com.ufrn.msr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVParser {
	
	private static int tagsCount;
	
	public static void csvSeparator() {
		String csvFile = "/Users/larissaleite/Downloads/Stack Overflow/Title_Body_Tags_Info.csv";
		BufferedReader br = null;
		String line = "";
		
		String end_title = ",\"<";
		//String end_body = "\"";
		String end_body_2 = "\",<";
		
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy hh:mm");

		try {

			br = new BufferedReader(new FileReader(csvFile));
			
			String title = "";
			String body = "";
			String tags = "";
			Date date;
			int views = 0;
			int answers = 0;
			int acceptedAnswer = 0;
			
			boolean title_flag = true;
			boolean body_flag = false;
			
			int id = 1;
			try {
				
				//ignore first line
				line = br.readLine();

				while ((line = br.readLine()) != null) {
					if (title_flag) {
						if (line.contains(end_title)) {
							String[] pieces = line.split(end_title);
							title += pieces[0];
							body += pieces[1];
							title_flag = false;
							body_flag = true;
						} else {
							title += line;
						}
					} else if (body_flag) {
						if (line.startsWith(end_body_2)) {
							title_flag = true;
							body_flag = false;
							
							String[] pieces = line.split(",");
							
							tags = pieces[1];
							date = new java.sql.Date(formatter.parse(pieces[2]).getTime());
							views = Integer.parseInt(pieces[3]);
							answers = Integer.parseInt(pieces[4]);
							if (pieces.length > 5 && pieces[5] != "" && !pieces[5].isEmpty()) {
								acceptedAnswer = Integer.parseInt(pieces[5]);
							} else {
								acceptedAnswer = -1;
							}
							
							Post post = new Post();
							post.setBody(body);
							post.setTitle(title);
							post.setTags(tags);
							post.setAnswerCount(answers);
							post.setAcceptedAnswer(acceptedAnswer);
							post.setViewCount(views);
							post.setCreationDate(date);
							
							System.out.println(title);
							System.out.println("-----");
							System.out.println(body);
							System.out.println("-----------------------------------------------------------------");
							
							PostMySQLDAO.savePost(post, id);
							
							id++;
							
							title = "";
							body = "";
							tags = "";
						} else {
							body += line;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			MySQLConnector.closeConnection();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void insertTags() {
		String csvFile = "/Users/larissaleite/Downloads/StackOverflow CSV/UPPER/tags.csv";
		BufferedReader br = null;
		String line = "";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			int id = 0;
			while ((line = br.readLine()) != null) {
				PostMySQLDAO.saveTags(line, id);
				id++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static HashMap<String, Integer> getMapTags() {
		HashMap<String, Integer> mapTags = new HashMap<String, Integer>();
		tagsCount = 0;

		String csvFile = "/Users/larissaleite/Downloads/StackOverflow CSV/UPPER/tags.csv";
		BufferedReader br = null;
		String line = "";
		String regex = "\\<(.*?)\\>";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(line);
								
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

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return mapTags;
	}

	public static int getTagsCount() {
		return tagsCount;
	}

	public static void setTagsCount(int tagsCount) {
		CSVParser.tagsCount = tagsCount;
	}

}
