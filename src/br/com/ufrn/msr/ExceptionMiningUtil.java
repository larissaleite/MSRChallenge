package br.com.ufrn.msr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionMiningUtil {

	private static String exc_regExpression = "(\\s)?([a-z](([\\w\\.]+)\\.([A-Z]([\\w]+))(Exception\\b[^\\.a-z]))|((([\\w\\.]+)\\.([A-Z][\\w]+)Error\\b)))|(([A-Z]([\\w]+))(Exception\\p{Space}+))";

	private static String exc_st_regexp_log = "((\\p{Graph}|\\p{Blank}|)*(Exception|Error).*((\\p{Space})+(\\p{Graph}|\\p{Blank})*at\\p{Space}+.+\\s*.+\\((\\p{Alnum}|\\.|\\:|\\p{Space}|\\[|\\])+\\))+)";

	private static String exception_in_stack = "(([a-zA-Z0-9\\$]+)\\.)*([a-zA-Z0-9\\$]+)\\.(([A-Z][a-zA-Z0-9]+)*)(Exception|Error)";

	private static String exception_in_stack_sem_pacote = "(([A-Z][a-zA-Z0-9]+)*)(Exception|Error)";

	private static String stack_frame_regexp_clean = "(\\p{Space}|[0-9]|\\p{Punct})+at\\p{Space}+\\p{Alnum}(\\.|\\p{Alnum}|\\_|\\>|\\<|\\p{Space})+\\((\\p{Alnum}|\\.|\\:|\\p{Space}|\\[|\\])+\\)";

	private static String stack_frame_regexp_new = "(([a-z]+)\\.)(\\p{Alpha})+\\.(\\p{Alnum}|\\_|\\>|\\<|\\.)+\\((\\p{Alnum}|\\.|\\:|\\p{Space}|\\[|\\])+\\)";

	private static String pre_signaler_package = "\\p{Alnum}(\\.|\\p{Alnum}|\\_|\\>|\\<|\\p{Space})+\\(";

	public static String getExceptionRegexp() {
		return exc_regExpression;
	}

	public static String getErrStackRegexp() {
		return exc_st_regexp_log;
	}

	public static String getExcStackRegexp() {
		return exc_st_regexp_log;
	}

	public static ArrayList<String> extractStacktraces(String text,
			boolean cleanChars, boolean joinCause) {

		if (cleanChars) {
			text = cleanOddCharacters(text);
		}

		// Applying heuristic based on the format of Android core logs (to sove
		// the missclassification of NullPointers):

		// TODO: ANTES ESTAVA HASHSET PRECISAMOS PRESERVAR A ORDEM DO TRACE....
		ArrayList<String> stacks = new ArrayList<String>();

		String orignal = text;

		String text1 = orignal;

		ArrayList<String> stacksExc = ExceptionMiningUtil
				.extractStacktracePerExp(text1, exc_st_regexp_log);

		if (stacksExc != null) {

			if (joinCause) {
				stacksExc = ExceptionMiningUtil.joinCauses(stacksExc);
			}

			if (stacksExc.size() > 0) {
				if (text1.contains("java.lang.RuntimeException: Unable to")
						|| text1.contains("java.lang.RuntimeException: Failure")) {
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

	public static boolean isACause(String current) {
		Scanner scanner = new Scanner(current);
		String topLine = scanner.nextLine();
		if (topLine.contains("Caused by:")) {
			return true;
		}

		return false;
	}

	public static int contNumberOfCaused(String trace) {
		int cont = 0;

		while (trace.contains("Caused by")) {
			cont++;
			trace = trace.replaceFirst("Caused by", "---");
		}
		return cont;

	}

	public static ArrayList<String> joinCauses(ArrayList<String> stacksExc) {

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

	private static String cleaExceptionName(String text) {
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

	public static ArrayList<String> extractStacktracePerExp(String text,
			String expression) {

		String trace = getTestString();
		// System.out.println("Expression: "+expression);
		text = cleanOddCharacters(text);
		Pattern pattern = Pattern.compile(expression);

		ArrayList<String> exceptions = null;

		try {
			if (text != null && !text.isEmpty()) {
				exceptions = new ArrayList<String>();

				// testando regexp:
				Matcher matcher_test = pattern.matcher(trace);

				// System.out.println("Pattern: "+matcher_test.pattern());

				while (matcher_test.find()) {
					matcher_test.group();
				}

				Matcher matcher = pattern.matcher(text);

				while (matcher.find()) {
					String exceptionPattern = matcher.group();
					exceptions.add(exceptionPattern);
					// System.out.println("Exception Stack: "+exceptionPattern);

					// if(exceptionPattern.contains(" at ")){
					// String cleanStack = cleanStack(exceptionPattern);
					// exceptions.add(cleanStack);
					// }
				}
			}
		} catch (java.lang.StackOverflowError err) {
			err.printStackTrace();
			System.out.println("Nao encontrou padrao...");
		}
		return exceptions;

	}

	public static String phase2extractException(String stackTrace) {

		Pattern pattern = Pattern.compile(exception_in_stack);
		String exception = null;

		if (stackTrace != null && !stackTrace.isEmpty()) {

			Matcher matcher = pattern.matcher(stackTrace);
			if (matcher.find()) {

				exception = matcher.group();
				System.out.println("Excecao encontrada: " + exception);

			}
		}
		return exception;

	}

	public static String phase2extractExceptionSemPacote(String stackTrace) {

		Pattern pattern = Pattern.compile(exception_in_stack_sem_pacote);
		String exception = null;

		if (stackTrace != null && !stackTrace.isEmpty()) {

			Matcher matcher = pattern.matcher(stackTrace);
			if (matcher.find()) {

				exception = matcher.group();
				System.out.println("Excecao encontrada: " + exception);

			}
		}
		return exception;

	}

	public static Vector<List<String>> phase2extractFramesPerCauses(String trace) {

		ArrayList<String> setstacks = extractStacktraces(trace, false, false);

		System.out.println("TRACES RETORNADOS: " + setstacks);

		Vector<List<String>> completeSet = new Vector<List<String>>();
		ArrayList<String> frames_per_cause;
		String exception = null;

		Iterator<String> it = setstacks.iterator();

		while (it.hasNext()) {
			String smallStack = it.next();
			frames_per_cause = phase2extractFrames(smallStack);
			exception = phase2extractException(smallStack);

			System.out.println("EXCEPTION: " + exception);
			System.out.println("FRAMES: " + frames_per_cause);

			// Heuristica: se a excecao vier nula entao procura por uma excecao
			// sem pacote.
			if (exception == null && frames_per_cause.size() > 0) {
				exception = phase2extractExceptionSemPacote(smallStack);
			}

			if (exception != null && frames_per_cause != null
					&& frames_per_cause.size() > 0
					&& !exception.trim().equals("")) {
				ArrayList<String> frames_per_cause_with_exception = new ArrayList<String>();
				frames_per_cause_with_exception.add(exception);
				frames_per_cause_with_exception.addAll(frames_per_cause);
				completeSet.add(frames_per_cause_with_exception);
			}

		}

		return completeSet;
	}

	/**
	 * TODO: Importante!! Esta funcao que encontra frames esta unindo os frames
	 * de cause e de excecao final. O ideal é nao juntar excecao e causa mais...
	 * guardar a excecao e as causas num vetor de causas associado...
	 * 
	 * @param stackTrace
	 * @return
	 */

	protected static ArrayList<String> phase2extractFramesPrevious(
			String stackTrace) {

		Pattern pattern = Pattern.compile(stack_frame_regexp_clean);

		ArrayList<String> frames = new ArrayList<String>();

		if (stackTrace != null && !stackTrace.isEmpty()) {

			Matcher matcher = pattern.matcher(stackTrace);

			while (matcher.find()) {

				String exceptionPattern = matcher.group();

				System.out.println("Adicionando frame: " + exceptionPattern);

				frames.add(exceptionPattern);

			}
		}
		return frames;

	}

	/**
	 * A versao anterior estava extraindo frames nao considera os frames do tipo
	 * at > nome do metodo > at > nome do metodo
	 * 
	 * @param stackTrace
	 * @return
	 */

	protected static ArrayList<String> phase2extractFrames(String stackTrace) {

		Pattern pattern = Pattern.compile(stack_frame_regexp_new);

		ArrayList<String> frames = new ArrayList<String>();

		if (stackTrace != null && !stackTrace.isEmpty()) {

			Matcher matcher = pattern.matcher(stackTrace);

			while (matcher.find()) {

				String exceptionPattern = matcher.group();

				System.out.println("Adicionando frame: " + exceptionPattern);

				frames.add(exceptionPattern);

			}
		}
		return frames;

	}

	public static int countFramesOnStack(String stackTrace) {

		Pattern pattern = Pattern.compile(stack_frame_regexp_new);

		int cont = 0;

		if (stackTrace != null && !stackTrace.isEmpty()) {

			Matcher matcher = pattern.matcher(stackTrace);

			while (matcher.find()) {

				cont++;

			}
		}
		return cont;

	}

	public static String cleanStack(String stack) {
		// TODO implementar a limpeza
		return stack;
	}

	public static List<String> extractExceptionsStr(String text) {

		Pattern pattern = Pattern.compile(exc_regExpression);
		Set<String> exceptions = null;
		List<String> except_list = new ArrayList<String>();

		if (text != null && !text.isEmpty()) {
			exceptions = new HashSet<String>();

			// this piece of code checks whether the text matches to the regular
			// expression.
			Matcher matcher = pattern.matcher(text);

			while (matcher.find()) {
				String exceptionPattern = matcher.group();
				exceptionPattern = cleaExceptionName(exceptionPattern);
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

	public static String getRegexStackLog() {
		return exc_st_regexp_log;
	}

	public static String getTestString() {
		String test = " \n ava.lang.OutOfMemoryError: bitmap size exceeds VM budget"
				+ " \n		at android.graphics.BitmapFactory.nativeDecod eAsset(Native Method)"
				+ " \n		 at android.graphics.BitmapFactory.decodeStream(BitmapFactory.java:563)"
				+ " \n		at android.graphics.BitmapFactory.decodeResourceStream(BitmapFactory.java:439)"
				+ " \n		at android.graphics.drawable.Drawable.createFromResourceStream(Drawable.java:697)"
				+ " \n		at android.content.res.Resources.loadDrawable(Resources.java:1981)"
				+ " \n      at android.content.res.Resources.getDrawable(Resources.java:601)"
				+ " \n        at android.widget.TextView HandleView.setOrientation(TextView.java:8599)"
				+ " \n        at android.widget.TextView HandleView.<init>(TextView.java:8568)"
				+ " \n        at android.widget.TextView InsertionPointCursorController.<init>(TextView.java:8881)"
				+ " \n        at android.widget.TextView.getInsertionController(TextView.java:9460)"
				+ " \n        at android.widget.TextView.onTouchEvent(TextView.java:7260)"
				+ " \n        at android.view.View.dispatchTouchEvent(View.java:3938)"
				+ " \n        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:869)"
				+ " \n        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:869)"
				+ " \n        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:869)"
				+ " \n        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:869)"
				+ " \n        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:869)"
				+ " \n        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:869)"
				+ " \n        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:869)"
				+ " \n        at com.android.internal.policy.impl.PhoneWindow DecorView.superDispatchTouchEvent(PhoneWindow.java:1746)"
				+ " \n        at com.android.internal.policy.impl.PhoneWindow.superDispatchTouchEvent(PhoneWindow.java:1152)"
				+ " \n        at android.app.Activity.dispatchTouchEvent(Activity.java:2102)"
				+ " \n        at com.android.internal.policy.impl.PhoneWindow DecorView.dispatchTouchEvent(PhoneWindow.java:1730)"
				+ " \n        at android.view.ViewRoot.deliverPointerEvent(ViewRoot.java:2218)"
				+ " \n        at android.view.ViewRoot.handleMessage(ViewRoot.java:1889)"
				+ " \n        at android.os.Handler.dispatchMessage(Handler.java:99)"
				+ " \n        at android.os.Looper.loop(Looper.java:130)"
				+ " \n        at android.app.ActivityThread.main(ActivityThread.java:3691)"
				+ " \n        at java.lang.reflect.Method.invokeNative(Native Method)"
				+ " \n        at java.lang.reflect.Method.invoke(Method.java:507)"
				+ " \n        at com.android.internal.os.ZygoteInit MethodAndArgsCaller.run(ZygoteInit.java:907)"
				+ " \n        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:665)"
				+ " \n        at dalvik.system.NativeStart.main(Native Method)"
				+ " \n"
				+ " \n"
				+ " \njava.lang.RuntimeException: Unable to start activity ComponentInfo{com.ianhanniballake.contractiontimer com.ianhanniballake.contractiontimer.ui.MainActivity}: android.view.InflateException: Binary XML file line  41: Error inflating class <unknown>"
				+ " \n        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:1659)"
				+ " \n        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:1675)"
				+ " \n        at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:2853)"
				+ " \n        at android.app.ActivityThread.access 1600(ActivityThread.java:121)"
				+ " \n        at android.app.ActivityThread H.handleMessage(ActivityThread.java:947)"
				+ " \n        at android.os.Handler.dispatchMessage(Handler.java:99)"
				+ " \n        at android.os.Looper.loop(Looper.java:130)"
				+ " \n        at android.app.ActivityThread.main(ActivityThread.java:3701)"
				+ " \n        at java.lang.reflect.Method.invokeNative(Native Method)"
				+ " \n        at java.lang.reflect.Method.invoke(Method.java:507)"
				+ " \n        at com.android.internal.os.ZygoteInit MethodAndArgsCaller.run(ZygoteInit.java:866)"
				+ " \n        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:624)"
				+ " \n        at dalvik.system.NativeStart.main(Native Method)"
				+ " \nCaused by: android.view.InflateException: Binary XML file line  41: Error inflating class <unknown>"
				+ " \n        at android.view.LayoutInflater.createView(LayoutInflater.java:518)"
				+ " \n        at com.android.internal.policy.impl.PhoneLayoutInflater.onCreateView(PhoneLayoutInflater.java:56)"
				+ " \n        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:568)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:623)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:626)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:408)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:320)"
				+ " \n        at com.ianhanniballake.contractiontimer.ui.ContractionListFragment.void bindView(android.view.View android.database.Cursor)(SourceFile:303)"
				+ " \n                                                                           android.view.View onCreateView(android.view.LayoutInflater android.view.ViewGroup android.os.Bundle)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(android.support.v4.app.Fragment int int int)(SourceFile:870)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int int int boolean)(SourceFile:1080)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int boolean)(SourceFile:1062)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void dispatchActivityCreated()(SourceFile:1810)"
				+ " \n        at android.support.v4.app.FragmentActivity.void onStart()(SourceFile:501)"
				+ " \n        at com.ianhanniballake.contractiontimer.analytics.AnalyticTrackingFragmentActivity.void onStart()(SourceFile:23)"
				+ " \n        at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1129)"
				+ " \n        at android.app.Activity.performStart(Activity.java:3791)"
				+ " \n        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:1632)"
				+ " \n        ... 12 more"
				+ " \nCaused by: java.lang.reflect.InvocationTargetException"
				+ " \n        at java.lang.reflect.Constructor.constructNative(Native Method)"
				+ " \n        at java.lang.reflect.Constructor.newInstance(Constructor.java:415)"
				+ " \n        at android.view.LayoutInflater.createView(LayoutInflater.java:505)"
				+ " \n        ... 28 more"
				+ " \nCaused by: java.lang.OutOfMemoryError: bitmap size exceeds VM budget"
				+ " \n        at android.graphics.BitmapFactory.nativeDecodeAsset(Native Method)"
				+ " \n        at android.graphics.BitmapFactory.decodeStream(BitmapFactory.java:494)"
				+ " \n        at android.graphics.BitmapFactory.decodeResourceStream(BitmapFactory.java:370)"
				+ " \n        at android.graphics.drawable.Drawable.createFromResourceStream(Drawable.java:715)"
				+ " \n        at android.content.res.Resources.loadDrawable(Resources.java:1720)"
				+ " \n        at android.content.res.Resources.getDrawable(Resources.java:585)"
				+ " \n        at android.widget.AbsListView.setOverScrollMode(AbsListView.java:702)"
				+ " \n        at android.view.View.<init>(View.java:1903)"
				+ " \n        at android.view.View.<init>(View.java:1945)"
				+ " \n        at android.view.ViewGroup.<init>(ViewGroup.java:320)"
				+ " \n        at android.widget.AdapterView.<init>(AdapterView.java:232)"
				+ " \n        at android.widget.AbsListView.<init>(AbsListView.java:629)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:167)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:163)"
				+ " \n        ... 31 more"
				+ " \nandroid.view.InflateException: Binary XML file line  41: Error inflating class <unknown>"
				+ " \n        at android.view.LayoutInflater.createView(LayoutInflater.java:518)"
				+ " \n        at com.android.internal.policy.impl.PhoneLayoutInflater.onCreateView(PhoneLayoutInflater.java:56)"
				+ " \n        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:568)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:623)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:626)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:408)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:320)"
				+ " \n        at com.ianhanniballake.contractiontimer.ui.ContractionListFragment.void bindView(android.view.View android.database.Cursor)(SourceFile:303)"
				+ " \n                                                                           android.view.View onCreateView(android.view.LayoutInflater android.view.ViewGroup android.os.Bundle)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(android.support.v4.app.Fragment int int int)(SourceFile:870)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int int int boolean)(SourceFile:1080)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int boolean)(SourceFile:1062)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void dispatchActivityCreated()(SourceFile:1810)"
				+ " \n        at android.support.v4.app.FragmentActivity.void onStart()(SourceFile:501)"
				+ " \n        at com.ianhanniballake.contractiontimer.analytics.AnalyticTrackingFragmentActivity.void onStart()(SourceFile:23)"
				+ " \n        at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1129)"
				+ " \n        at android.app.Activity.performStart(Activity.java:3791)"
				+ " \n        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:1632)"
				+ " \n        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:1675)"
				+ " \n        at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:2853)"
				+ " \n        at android.app.ActivityThread.access 1600(ActivityThread.java:121)"
				+ " \n        at android.app.ActivityThread H.handleMessage(ActivityThread.java:947)"
				+ " \n        at android.os.Handler.dispatchMessage(Handler.java:99)"
				+ " \n        at android.os.Looper.loop(Looper.java:130)"
				+ " \n        at android.app.ActivityThread.main(ActivityThread.java:3701)"
				+ " \n        at java.lang.reflect.Method.invokeNative(Native Method)"
				+ " \n        at java.lang.reflect.Method.invoke(Method.java:507)"
				+ " \n        at com.android.internal.os.ZygoteInit MethodAndArgsCaller.run(ZygoteInit.java:866)"
				+ " \n        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:624)"
				+ " \n        at dalvik.system.NativeStart.main(Native Method)"
				+ " \nCaused by: java.lang.reflect.InvocationTargetException"
				+ " \n        at java.lang.reflect.Constructor.constructNative(Native Method)"
				+ " \n        at java.lang.reflect.Constructor.newInstance(Constructor.java:415)"
				+ " \n        at android.view.LayoutInflater.createView(LayoutInflater.java:505)"
				+ " \n        ... 28 more"
				+ " \nCaused by: java.lang.OutOfMemoryError: bitmap size exceeds VM budget"
				+ " \n        at android.graphics.BitmapFactory.nativeDecodeAsset(Native Method)"
				+ " \n        at android.graphics.BitmapFactory.decodeStream(BitmapFactory.java:494)"
				+ " \n        at android.graphics.BitmapFactory.decodeResourceStream(BitmapFactory.java:370)"
				+ " \n        at android.graphics.drawable.Drawable.createFromResourceStream(Drawable.java:715)"
				+ " \n        at android.content.res.Resources.loadDrawable(Resources.java:1720)"
				+ " \n        at android.content.res.Resources.getDrawable(Resources.java:585)"
				+ " \n        at android.widget.AbsListView.setOverScrollMode(AbsListView.java:702)"
				+ " \n        at android.view.View.<init>(View.java:1903)"
				+ " \n        at android.view.View.<init>(View.java:1945)"
				+ " \n        at android.view.ViewGroup.<init>(ViewGroup.java:320)"
				+ " \n        at android.widget.AdapterView.<init>(AdapterView.java:232)"
				+ " \n        at android.widget.AbsListView.<init>(AbsListView.java:629)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:167)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:163)"
				+ " \n        ... 31 more"
				+ " \njava.lang.reflect.InvocationTargetException"
				+ " \n        at java.lang.reflect.Constructor.constructNative(Native Method)"
				+ " \n        at java.lang.reflect.Constructor.newInstance(Constructor.java:415)"
				+ " \n        at android.view.LayoutInflater.createView(LayoutInflater.java:505)"
				+ " \n        at com.android.internal.policy.impl.PhoneLayoutInflater.onCreateView(PhoneLayoutInflater.java:56)"
				+ " \n        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:568)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:623)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:626)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:408)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:320)"
				+ " \n        at com.ianhanniballake.contractiontimer.ui.ContractionListFragment.void bindView(android.view.View android.database.Cursor)(SourceFile:303)"
				+ " \n                                                                           android.view.View onCreateView(android.view.LayoutInflater android.view.ViewGroup android.os.Bundle)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(android.support.v4.app.Fragment int int int)(SourceFile:870)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int int int boolean)(SourceFile:1080)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int boolean)(SourceFile:1062)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void dispatchActivityCreated()(SourceFile:1810)"
				+ " \n        at android.support.v4.app.FragmentActivity.void onStart()(SourceFile:501)"
				+ " \n        at com.ianhanniballake.contractiontimer.analytics.AnalyticTrackingFragmentActivity.void onStart()(SourceFile:23)"
				+ " \n        at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1129)"
				+ " \n        at android.app.Activity.performStart(Activity.java:3791)"
				+ " \n        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:1632)"
				+ " \n        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:1675)"
				+ " \n        at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:2853)"
				+ " \n        at android.app.ActivityThread.access 1600(ActivityThread.java:121)"
				+ " \n        at android.app.ActivityThread H.handleMessage(ActivityThread.java:947)"
				+ " \n        at android.os.Handler.dispatchMessage(Handler.java:99)"
				+ " \n        at android.os.Looper.loop(Looper.java:130)"
				+ " \n        at android.app.ActivityThread.main(ActivityThread.java:3701)"
				+ " \n        at java.lang.reflect.Method.invokeNative(Native Method)"
				+ " \n        at java.lang.reflect.Method.invoke(Method.java:507)"
				+ " \n        at com.android.internal.os.ZygoteInit MethodAndArgsCaller.run(ZygoteInit.java:866)"
				+ " \n        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:624)"
				+ " \n        at dalvik.system.NativeStart.main(Native Method)"
				+ " \nCaused by: java.lang.OutOfMemoryError: bitmap size exceeds VM budget"
				+ " \n        at android.graphics.BitmapFactory.nativeDecodeAsset(Native Method)"
				+ " \n        at android.graphics.BitmapFactory.decodeStream(BitmapFactory.java:494)"
				+ " \n        at android.graphics.BitmapFactory.decodeResourceStream(BitmapFactory.java:370)"
				+ " \n        at android.graphics.drawable.Drawable.createFromResourceStream(Drawable.java:715)"
				+ " \n        at android.content.res.Resources.loadDrawable(Resources.java:1720)"
				+ " \n        at android.content.res.Resources.getDrawable(Resources.java:585)"
				+ " \n        at android.widget.AbsListView.setOverScrollMode(AbsListView.java:702)"
				+ " \n        at android.view.View.<init>(View.java:1903)"
				+ " \n        at android.view.View.<init>(View.java:1945)"
				+ " \n        at android.view.ViewGroup.<init>(ViewGroup.java:320)"
				+ " \n        at android.widget.AdapterView.<init>(AdapterView.java:232)"
				+ " \n        at android.widget.AbsListView.<init>(AbsListView.java:629)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:167)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:163)"
				+ " \n        ... 31 more"
				+ " \njava.lang.OutOfMemoryError: bitmap size exceeds VM budget"
				+ " \n        at android.graphics.BitmapFactory.nativeDecodeAsset(Native Method)"
				+ " \n        at android.graphics.BitmapFactory.decodeStream(BitmapFactory.java:494)"
				+ " \n        at android.graphics.BitmapFactory.decodeResourceStream(BitmapFactory.java:370)"
				+ " \n        at android.graphics.drawable.Drawable.createFromResourceStream(Drawable.java:715)"
				+ " \n        at android.content.res.Resources.loadDrawable(Resources.java:1720)"
				+ " \n        at android.content.res.Resources.getDrawable(Resources.java:585)"
				+ " \n        at android.widget.AbsListView.setOverScrollMode(AbsListView.java:702)"
				+ " \n        at android.view.View.<init>(View.java:1903)"
				+ " \n        at android.view.View.<init>(View.java:1945)"
				+ " \n        at android.view.ViewGroup.<init>(ViewGroup.java:320)"
				+ " \n        at android.widget.AdapterView.<init>(AdapterView.java:232)"
				+ " \n        at android.widget.AbsListView.<init>(AbsListView.java:629)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:167)"
				+ " \n        at android.widget.ListView.<init>(ListView.java:163)"
				+ " \n        at java.lang.reflect.Constructor.constructNative(Native Method)"
				+ " \n        at java.lang.reflect.Constructor.newInstance(Constructor.java:415)"
				+ " \n        at android.view.LayoutInflater.createView(LayoutInflater.java:505)"
				+ " \n        at com.android.internal.policy.impl.PhoneLayoutInflater.onCreateView(PhoneLayoutInflater.java:56)"
				+ " \n        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:568)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:623)"
				+ " \n        at android.view.LayoutInflater.rInflate(LayoutInflater.java:626)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:408)"
				+ " \n        at android.view.LayoutInflater.inflate(LayoutInflater.java:320)"
				+ " \n        at com.ianhanniballake.contractiontimer.ui.ContractionListFragment.void bindView(android.view.View android.database.Cursor)(SourceFile:303)"
				+ " \n                                                                           android.view.View onCreateView(android.view.LayoutInflater android.view.ViewGroup android.os.Bundle)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(android.support.v4.app.Fragment int int int)(SourceFile:870)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int int int boolean)(SourceFile:1080)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void moveToState(int boolean)(SourceFile:1062)"
				+ " \n        at android.support.v4.app.FragmentManagerImpl.void dispatchActivityCreated()(SourceFile:1810)"
				+ " \n        at android.support.v4.app.FragmentActivity.void onStart()(SourceFile:501)"
				+ " \n        at com.ianhanniballake.contractiontimer.analytics.AnalyticTrackingFragmentActivity.void onStart()(SourceFile:23)"
				+ " \n        at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1129)"
				+ " \n        at android.app.Activity.performStart(Activity.java:3791)"
				+ " \n        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:1632)"
				+ " \n        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:1675)"
				+ " \n        at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:2853)"
				+ " \n        at android.app.ActivityThread.access 1600(ActivityThread.java:121)"
				+ " \n        at android.app.ActivityThread H.handleMessage(ActivityThread.java:947)"
				+ " \n        at android.os.Handler.dispatchMessage(Handler.java:99)"
				+ " \n        at android.os.Looper.loop(Looper.java:130)"
				+ " \n        at android.app.ActivityThread.main(ActivityThread.java:3701)"
				+ " \n        at java.lang.reflect.Method.invokeNative(Native Method)"
				+ " \n        at java.lang.reflect.Method.invoke(Method.java:507)"
				+ " \n        at com.android.internal.os.ZygoteInit MethodAndArgsCaller.run(ZygoteInit.java:866)"
				+ " \n        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:624)"
				+ " \n        at dalvik.system.NativeStart.main(Native Method)";

		return test;
	}

	public static String getSignalerPackageName(String name) {
		Pattern pattern = Pattern.compile(pre_signaler_package);
		String sig_package = null;

		if (name != null && !name.isEmpty()) {

			Matcher matcher = pattern.matcher(name);
			if (matcher.find()) {
				sig_package = matcher.group();
				int index = sig_package.indexOf('(');
				if (index != -1) {
					sig_package = sig_package.substring(0, index);
					index = sig_package.lastIndexOf('.');
					// Tira nome do metodo
					if (index != -1) {
						sig_package = sig_package.substring(0, index);
						index = sig_package.lastIndexOf('.');
						// Tira nome da classe
						if (index != -1) {
							sig_package = sig_package.substring(0, index);
							index = sig_package.lastIndexOf(' ');
							// Tira nome da classe
							if (index != -1) {
								sig_package = sig_package.substring(index,
										sig_package.length());
								sig_package = sig_package.trim();
							}
						}
					}
				}

			}
		}
		return sig_package;

	}

	public static String getClassPackageName(String name) {

		if (name != null && !name.isEmpty()) {

			int index = name.lastIndexOf('.');
			if (index != -1) {
				name = name.substring(0, index);
			}
		}
		return name;

	}

	public static String getSignalerClassName(String name) {
		Pattern pattern = Pattern.compile(pre_signaler_package);
		String sig_class = null;

		if (name != null && !name.isEmpty()) {

			Matcher matcher = pattern.matcher(name);
			if (matcher.find()) {
				sig_class = matcher.group();
				int index = sig_class.indexOf('(');
				if (index != -1) {
					sig_class = sig_class.substring(0, index);
					index = sig_class.lastIndexOf('.');
					// Tira nome do metodo
					if (index != -1) {
						sig_class = sig_class.substring(0, index);
						index = sig_class.lastIndexOf(' ');
						// Tira nome da classe
						if (index != -1) {
							sig_class = sig_class.substring(index,
									sig_class.length());
							sig_class = sig_class.trim();
						}
					}
				}

			}
		}
		return sig_class;

	}

	public static String getSignalerMethodName(String name) {
		Pattern pattern = Pattern.compile(pre_signaler_package);
		String sig_met = null;

		if (name != null && !name.isEmpty()) {

			Matcher matcher = pattern.matcher(name);
			if (matcher.find()) {
				sig_met = matcher.group();
				int index = sig_met.indexOf('(');
				if (index != -1) {
					sig_met = sig_met.substring(0, index);
					index = sig_met.lastIndexOf(' ');
					// Tira nome da classe
					if (index != -1) {
						sig_met = sig_met.substring(index, sig_met.length());
						sig_met = sig_met.trim();
					}// IF 1
				}// if 2

			}// if 3
		}
		return sig_met;

	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

		String trace = "com.github.adlyticsproject.exception.InvalidJSONResponseException: Invalid JSON response json:  EX 2 1  ʺcom.google.gwt.user.client.rpc.IncompatibleRemoteServiceException 3936916533ʺ ʺType 'com.google.gwt.user.client.rpc.XsrfToken' was not assignable to 'com.google.gwt.user.client.rpc.IsSerializable' and did not have a custom field serializer. For security purposes  this type will not be deserialized.ʺ  0 7         at com.github.andlyticsproject.DeveloperConsole.testIfJsonIsValid(DeveloperConsole.java:268)        at com.github.andlyticsproject.DeveloperConsole.parseAppStatisticsResponse(DeveloperConsole.java:226)        at com.github.andlyticsproject.DeveloperConsole.getFullAssetListRequest(DeveloperConsole.java:137)        at com.github.andlyticsproject.DeveloperConsole.getAppDownloadInfos(DeveloperConsole.java:127)        at com.github.andlyticsproject.Main LoadRemoteEntries.doInBackground(Main.java:296)        at com.github.andlyticsproject.Main LoadRemoteEntries.doInBackground(Main.java:1)        at android.os.AsyncTask 2.call(AsyncTask.java:264)        at java.util.concurrent.FutureTask Sync.innerRun(FutureTask.java:305)        at java.util.concurrent.FutureTask.run(FutureTask.java:137)        at android.os.AsyncTask SerialExecutor 1.run(AsyncTask.java:208)        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1076)        at java.util.concurrent.ThreadPoolExecutor Worker.run(ThreadPoolExecutor.java:569)        at java.lang.Thread.run(Thread.java:856)";

		List<String> lista = phase2extractFrames(trace);

		Iterator it = lista.iterator();

		while (it.hasNext()) {
			System.out.println("FRAME: " + it.next());
		}

	}

}
