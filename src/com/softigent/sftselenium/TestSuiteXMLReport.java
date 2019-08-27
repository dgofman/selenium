package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import junit.framework.AssertionFailedError;

public class TestSuiteXMLReport implements ITestSuiteReport {

	protected PrintWriter writer;
	protected List<String> results;

	private Pattern TRIM = Pattern.compile("\\r\\n|\\n", Pattern.MULTILINE);

	@Override
	public void openDoc(TestRunnerInfo info, File reportDir) throws IOException {
		writer = new PrintWriter(new File(reportDir, info.getFileName() + ".xml"), "UTF-8");
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
	}

	@Override
	public void openBody(TestRunnerInfo info) {
		writer.println("<testsuites>");
	}

	public void addProperties(TestRunnerInfo info, Date startTime) {
		writer.println("    <property name=\"description\" value=\"" + info.getDescription() + "\" />");
		writer.println("    <property name=\"start_time\" value=\"" + DateFormat.getDateTimeInstance().format(startTime) + "\" />");
	}

	@Override
	public void addHeader(TestRunnerInfo info, Date startTime) {
		writer.println("  <properties>");
		addProperties(info, startTime);
		writer.println("  </properties>");
		writer.flush();
	}

	@Override
	public void openTest(TestRunnerInfo info) {
		results = new ArrayList<>();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addTest(TestRunnerInfo info, long time, List<Failure> failures, boolean ignored,
			Description description) {
		results.add("      <testcase classname=\"" + description.getClassName() + "\" name=\""
				+ description.getMethodName() + "\" time=\"" + ((float) time) / 1000 + "\">");
		if (ignored) {
			results.add("        <skipped />");
		}
		for (Failure failure : failures) {
			Throwable exception = failure.getException();
			String msg = failure.getMessage();
			if (msg != null) {
				msg = "message=\"" + TRIM.matcher(org.apache.commons.lang3.StringEscapeUtils.escapeXml(msg)).replaceAll(" ") + "\" ";
			} else {
				msg = "";
			}
			if (exception instanceof AssertionFailedError || exception instanceof AssertionError) {
				results.add("        <failure " + msg + "type=\"" + exception.getClass().getTypeName() + "\"><![CDATA["
						+ failure.getTrace() + "]]></failure>");
			} else {
				results.add("        <error " + msg + "type=\"" + exception.getClass().getTypeName() + "\"><![CDATA["
						+ failure.getTrace() + "]]></error>");
			}
		}
		results.add("      </testcase>");
	}

	@Override
	public void closeTest(TestRunnerInfo info) {
	}

	@Override
	public void addResult(TestRunnerInfo info, long time, Class<?> testCase, Result result, List<Failure> asserts,
			List<Failure> errors) {
		writer.println("  <testsuite errors=\"" + errors.size() + "\" failures=\"" + asserts.size() + "\" skipped=\""
				+ result.getIgnoreCount() + "\" tests=\"" + result.getRunCount() + "\" name=\""
				+ testCase.getSimpleName() + "\" package=\"" + testCase.getPackage() + "\" time=\""
				+ ((float) time) / 1000 + "\" timestamp=\""
				+ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()) + "\">");
		for (String testcase : results) {
			writer.println(testcase);
		}
		writer.println("  </testsuite>");
		writer.flush();
	}

	@Override
	public void addReport(TestRunnerInfo info, long time, int totalTestCases, int totalSucceed, int totalFailed,
			int totalTests, int totalErrors, int totalAsserts, int totalIgnored) {
	}

	@Override
	public void addFailures(Map<Class<?>, Result> failResults) {
		writer.println("<system-out></system-out>");
		writer.println("<system-err></system-err>");
	}

	@Override
	public void closeBody(TestRunnerInfo info) {
		writer.println("</testsuites>");
	}

	@Override
	public void closeDoc() throws IOException {
		writer.close();
	}
}
