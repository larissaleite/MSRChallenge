package br.com.ufrn.msr;

import java.util.List;

public class Main {

	public static void main(String[] args) {
		
		System.out.println("TagsVsTags");
		DataAnalyzer.showStatsTagsVsTags();
		System.out.println("-------------------");
		System.out.println("TagsQuestions");
		DataAnalyzer.showStatsTagsPerQuestion();
		System.out.println("-------------------");
		DataAnalyzer.showStatsAcceptedAnswer();
		
//		CSVParser.csvSeparator();
//		String body = PostMySQLDAO.getBodyFromPost(1234);
//		if (body != null) {
//			List<String> codes = ExceptionMinerUtil.extractCodesFromBody(body);
//			System.out.println("Codes");
//			for (String code : codes) {
//				System.out.println(code);
//				List<String> stacktraces = ExceptionMiner.extractStacktraces(body, true, false);
//				for (String stacktrace : stacktraces) {
//					System.out.println(stacktrace);
//				}
//			}
//			System.out.println("--------------------------");
//			List<String> stacktraces = ExceptionMiner.extractStacktraces(body, true, false);
//			System.out.println("Stacktraces");
//			for (String stacktrace : stacktraces) {
//				System.out.println(stacktrace);
//			}
//			System.out.println("--------------------------");
//			System.out.println("Exceptions");
//			List<String> exceptions = ExceptionMiner.findExceptions(body);
//			for (String exception : exceptions) {
//				System.out.println(exception);
//			}
//		}
	}

}
