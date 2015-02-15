package br.com.ufrn.msr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionMiner {
	
	private static String exc_regExpression = "(\\s)?([a-z](([\\w\\.]+)\\.([A-Z]([\\w]+))(Exception\\b[^\\.a-z]))|((([\\w\\.]+)\\.([A-Z][\\w]+)Error\\b)))|(([A-Z]([\\w]+))(Exception\\p{Space}+))";
	
	private static String exc_st_regexp_log = "((\\p{Graph}|\\p{Blank}|)*(Exception|Error).*((\\p{Space})+(\\p{Graph}|\\p{Blank})*at\\p{Space}+.+\\s*.+\\((\\p{Alnum}|\\.|\\:|\\p{Space}|\\[|\\])+\\))+)";
	
	public static ArrayList<String> extractStacktraces(String text, boolean cleanChars, boolean joinCause) {

		if (cleanChars) {
			text = cleanOddCharacters(text);
		}

		ArrayList<String> stacks = new ArrayList<String>();

		ArrayList<String> stacksExc = extractStacktracePerExp(text, exc_st_regexp_log);

		if (stacksExc != null) {

			if (joinCause) {
				stacksExc = joinCauses(stacksExc);
			}

			if (stacksExc.size() > 0) {
				if (text.contains("java.lang.RuntimeException: Unable to")
						|| text.contains("java.lang.RuntimeException: Failure")) {
					String stackExtracted = stacksExc.get(0);

					if (!stackExtracted.contains("java.lang.RuntimeException")) {
						stackExtracted = "java.lang.RuntimeException "
								+ stackExtracted;
						stacksExc.remove(0);
						stacksExc.add(0, stackExtracted);
					}
				}
			}

			stacks.addAll(stacksExc);

		}
		return stacks;
	}
	
	private static ArrayList<String> extractStacktracePerExp(String text, String expression) {

		text = cleanOddCharacters(text);
		Pattern pattern = Pattern.compile(expression);

		ArrayList<String> exceptions = null;

		try {
			if (text != null && !text.isEmpty()) {
				exceptions = new ArrayList<String>();

				Matcher matcher = pattern.matcher(text);

				while (matcher.find()) {
					String exceptionPattern = matcher.group();
					exceptions.add(exceptionPattern);
				}
			}
		} catch (java.lang.StackOverflowError err) {
			//err.printStackTrace();
			System.out.println("Nao encontrou padrao...");
		}
		
		return exceptions;

	}
	
	private static ArrayList<String> joinCauses(ArrayList<String> stacksExc) {

		ArrayList<String> joinedExc = new ArrayList<String>();

		if (stacksExc != null && stacksExc.size() > 0) {

			String previous = "";
			String current = "";

			previous = stacksExc.get(0);

			for (int i = 1; i < stacksExc.size(); i++) {
				current = stacksExc.get(i);

				if (isACause(current)) {
					previous = previous + "\n" + current;
				} else {
					joinedExc.add(previous);
					previous = current;
				}

			}

			if (!joinedExc.contains(previous)) {
				joinedExc.add(previous);
			}

		}
		return joinedExc;
	}
	
	private static boolean isACause(String current) {
		if (current.contains("Caused by:")) {
			return true;
		}

		return false;
	}
	
	public static List<String> findExceptions(String body) {
		Pattern pattern = Pattern.compile(exc_regExpression);
		Set<String> exceptions = null;
		List<String> except_list = new ArrayList<String>();

		if (body != null && !body.isEmpty()) {
			exceptions = new HashSet<String>();

			// this piece of code checks whether the text matches to the regular expression.
			Matcher matcher = pattern.matcher(body);

			while (matcher.find()) {
				String exceptionPattern = matcher.group();
				exceptionPattern = cleanExceptionName(exceptionPattern);
				exceptionPattern = exceptionPattern.trim();
				exceptions.add(exceptionPattern);
			}
		}

		if (exceptions != null) {
			except_list.addAll(exceptions);
			return except_list;
		} else {
			return null;
		}
	}
	
	private static String cleanExceptionName(String text) {
		if (text != null) {
			text = text.replace('(', ' ');
			text = text.replace(')', ' ');
			text = text.replace(':', ' ');
			text = cleanOddCharacters(text);
		}
		return text;
	}
	
	private static String cleanOddCharacters(String text) {

		if (text != null) {
			text = text.replace('\t', ' ');
			text = text.replace('/', ' ');
			text = text.replace('+', ' ');
			text = text.replace('-', ' ');
			text = text.replace('$', 'd');
			text = text.replace('%', 'p');
			text = text.replace('#', 'g');
			text = text.replace('@', 'a');
			text = text.replace('`', ' ');
			text = text.replace('?', 'i');
			text = text.replace('!', 'e');
			text = text.replace(',', ' ');
			text = text.replace('\\', '_');
			text = text.replace('[', 'c');
			text = text.replace(']', 'c');

		}
		// System.out.println(text);
		return text;

	}

}
