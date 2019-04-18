package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.WebDriver;

public class TestSuiteRunner {

	private static final Logger log = CacheLogger.getLogger(TestSuiteRunner.class);
	
	private List<TestRunnerInfo> suites;
		
	public TestSuiteRunner(List<TestRunnerInfo> suites) {
		this.suites = suites;
	}

	public void run() throws IOException {

		for (TestRunnerInfo info : suites) {
			Date startTime = new Date();
			int totalTestCases = 0;
			int totalTests = 0;
			int totalIgnored = 0;
			int totalErrors = 0;
			int totalAsserts = 0;
			int totalSucceed = 0;
			int totalFailed = 0;
			Map<Class<?>, Result> failResults = new LinkedHashMap<Class<?>, Result>();

			Class<?> testSuite = info.getTestSuite();
			File file = info.getFile();
			PrintWriter writer = new PrintWriter(file, "UTF-8");

			writer.println("<!doctype html>");
			writer.println("<html lang='en'>");
			openHead(writer, info);
			addTitle(writer, info);
			addStyle(writer, info);
			addScript(writer, info);
			closeHead(writer, info);
			openBody(writer, info);
			addHeader(writer, info, startTime);
			openTests(writer, info);

			Suite.SuiteClasses annotation = (SuiteClasses) testSuite.getAnnotation(Suite.SuiteClasses.class);
			if (annotation != null) {
				Class<?>[] suiteClassLst = annotation.value();
				totalTestCases = suiteClassLst.length;
				for (Class<?> testCase : suiteClassLst) {
					log.info("Start: " + testCase.getName());
					try {
						Result result = getResult(writer, testCase, info);
						List<Failure> asserts = new ArrayList<>();
						List<Failure> errors = new ArrayList<>();
						for (Failure failure : result.getFailures()) {
							if (failure.getException() instanceof AssertionError) {
								asserts.add(failure);
							} else {
								errors.add(failure);
							}
						}
						for (Failure failure : result.getFailures()) {
							log.info(failure.toString());
						}
						log.info("End: " + testCase.getName() + " in " + getTime(result.getRunTime()) + "\nTests: "
								+ result.getRunCount() + "\nFailed: " + result.getFailureCount() 
								+ "\nIgnored: " + result.getIgnoreCount());

						List<Failure> testCaseAsserts = new ArrayList<>();
						List<Failure> testCaseErrors = new ArrayList<>();
						for (Failure failure : result.getFailures()) {
							if (failure.getException() instanceof AssertionError) {
								testCaseAsserts.add(failure);
							} else {
								testCaseErrors.add(failure);
							}
						}
						openTest(writer, info, testCase, result, testCaseAsserts, testCaseErrors);
						addResult(writer, info, testCase, result, testCaseAsserts, testCaseErrors);
						closeTest(writer, info, testCase, result);
		
						totalTests += result.getRunCount();
						totalAsserts += asserts.size();
						totalErrors += errors.size();
						totalIgnored += result.getIgnoreCount();
						if (result.wasSuccessful()) {
							totalSucceed++;
						} else {
							failResults.put(testCase, result);
							totalFailed++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			closeTests(writer, info);
			addReport(writer, info, startTime, totalTestCases, totalSucceed, totalFailed, totalTests, totalErrors, totalAsserts, totalIgnored);
			addLog(writer, info, startTime, totalTestCases, totalSucceed, totalFailed, totalTests, totalErrors, totalAsserts, totalIgnored);
			addFailures(writer, failResults);
			closeBody(writer, info);
			writer.println("</html>");
	
			close(writer, file);
		}
	}

	protected void close(PrintWriter writer, File file) throws IOException {
		writer.close();
	}
	
	protected Result getResult(PrintWriter writer, Class<?> testCase, TestRunnerInfo info) {
		return JUnitCore.runClasses(testCase);
	}
	
	protected WebDriver getDriver() {
		Connector con = Connector.instance;
		if (con != null && con.getConfig() != null && con.getDriver() != null) {
			return con.getDriver();
		}
		return null;
	}

	protected void openHead(PrintWriter writer, TestRunnerInfo info) {
		writer.println("<head>");
		writer.println("<meta charset='utf-8'>");
		writer.println("<meta http-equiv='Cache-control' content='no-cache'>");
	}

	protected void closeHead(PrintWriter writer, TestRunnerInfo info) {
		writer.println("</head>");
	}

	protected void openBody(PrintWriter writer, TestRunnerInfo info) {
		writer.println("<body>");
	}

	protected void closeBody(PrintWriter writer, TestRunnerInfo info) {
		writer.println("<script>window.complete = true;</script>");
		writer.println("</body>");
	}

	protected void openTests(PrintWriter writer, TestRunnerInfo info) {
		writer.println("<ul>");
	}

	protected void closeTests(PrintWriter writer, TestRunnerInfo info) {
		writer.println("</ul>");
	}

	protected void openTest(PrintWriter writer, TestRunnerInfo info, Class<?> testCase, Result result, List<Failure> asserts, List<Failure> errors) {
		writer.println("<li id='" + testCase.getName() + "' style='background: " + (result.wasSuccessful() ? "lightgreen" : 
			errors.size() == 0 ? "orange" : "lightcoral") + "'>");
		writer.println("<a href='#" + testCase.getName() + "_image'>");
		writer.println("<h3>" + testCase.getName() + "</h3>");
	}

	protected void closeTest(PrintWriter writer, TestRunnerInfo info, Class<?> testCase, Result result) {
		writer.println("</a>");
		writer.println("</li>");
		writer.println("<script>window.testcase='" + testCase.getName()  + "'</script>");
		writer.flush();
	}

	protected void addTitle(PrintWriter writer, TestRunnerInfo info) {
		writer.println("<title>" + info.getDescription() + "</title>");
	}

	protected void addStyle(PrintWriter writer, TestRunnerInfo info) {
		writer.println("<style>");
		writer.println("li, div {padding: 2px 5px; margin: 2px 0}");
		writer.println("li a { text-decoration: none; color: black }");
		writer.println("p, h3 {padding: 3px; margin: 0;}");
		writer.println("pre { white-space: pre-wrap;}");
		writer.println(".accordion input {display: none;}");
		writer.println(".accordion label {background: #eee; cursor: pointer; display: block; margin-bottom: .125em; padding: .25em 1em;}");
		writer.println(".accordion label:hover {background: #ccc;}");
		writer.println(".accordion input:checked + label {background: #ccc; color: white;}");
		writer.println(".accordion article {background: #f7f7f7; height:0px;overflow:hidden;}");
		writer.println(".accordion input:checked ~ article {height: auto;}");
		writer.println("</style>");
	}

	protected void addScript(PrintWriter writer, TestRunnerInfo info) {
		writer.println("<script>");
		writer.println("window.testcase = '';");
		writer.println("function reload() {");
		writer.println("	if (window.complete != true) {");
		writer.println("		var url = location.origin + location.pathname + '#' + window.testcase");
		writer.println("		if (url != location.href) location.replace(url);");
		writer.println("		location.reload(true);");
		writer.println("	} else {");
		writer.println("		location.replace(location.origin + location.pathname + '#report');");
		writer.println("	}");
		writer.println("}\nsetTimeout(reload, 3000);");
		writer.println("</script>");
	}

	protected void addHeader(PrintWriter writer, TestRunnerInfo info, Date startTime) {
		writer.println("<h1>" + info.getDescription() + "</h1>");
		writer.println("<p>Start Time: " + DateFormat.getDateTimeInstance().format(startTime) + "</p>");
		writer.flush();
	}

	protected void addResult(PrintWriter writer, TestRunnerInfo info, Class<?> testCase, Result result, List<Failure> asserts, List<Failure> errors) {
		log.trace("<<<<<<<<<<<<<<<<<<<<<<<<<<<< ADD RESULT >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		writer.println("<p><b>Run Time</b>: " + getTime(result.getRunTime()) + "</p>");
		writer.println("<p><b>Tests Succeed</b>: " + result.getRunCount() + "</p>");
		writer.println("<p><b>Tests Ignored</b>: " + result.getIgnoreCount() + "</p>");
		writer.println("<p><b>Tests Failed</b>: " + result.getFailureCount() + "</p>");
		writer.println("<p><b>Errors</b>: " + errors.size() + "</p>");
		writer.println("<p><b>Asserts</b>: " + asserts.size() + "</p>");
	}

	protected void addReport(PrintWriter writer, TestRunnerInfo info, Date startTime, int totalTestCases, int totalSucceed, int totalFailed, int totalTests, int totalErrors, int totalAsserts, int totalIgnored) {
		writer.println("<div id='report' style='background:lightskyblue; margin-top: 40px;'>");
		writer.println("<p><b>Total Time</b>: " + getTime(new Date().getTime() - startTime.getTime()) + "</p></br>");
		writer.println("<p><b>Total TestCases</b>: " + totalTestCases + "</p>");
		writer.println("<p><b>Total TestCases Succeed</b>: " + totalSucceed + "</p>");
		writer.println("<p><b>Total TestCases Failed</b>: " + totalFailed + "</p><br/>");
		writer.println("<p><b>Total Tests</b>: " + totalTests + "</p>");
		writer.println("<p><b>Total Tests Ignored</b>: " + totalIgnored + "</p>");
		writer.println("<p><b>Total Tests Errors</b>: " + totalErrors + "</p>");
		writer.println("<p><b>Total Tests Asserts</b>: " + totalAsserts + "</p>");
		writer.println("</div>");
	}
	
	protected void addLog(PrintWriter writer, TestRunnerInfo info, Date startTime, int totalTestCases, int totalSucceed, int totalFailed, int totalTests, int totalErrors, int totalAsserts, int totalIgnored) {
		log.trace("COMPLETED " + info.getDescription() + ",  in : " + getTime(new Date().getTime() - startTime.getTime()) 
			+ "\nTotal TestCases: " + totalTestCases
			+ "\nTotal TestCases Succeed: " + totalSucceed 
			+ "\nTotal TestCases Failed: " + totalFailed
			+ "\nTotal Tests: " + totalTests 
			+ "\nTotal Tests Ignored: " + totalIgnored
			+ "\nTotal Tests Errors: " + totalErrors
			+ "\nTotal Tests Asserts: " + totalAsserts
			+ "\nReport File: " + info.getFile().getAbsolutePath());
	}

	protected void addFailures(PrintWriter writer, Map<Class<?>, Result> failResults) {
		writer.println("<div style='margin-top: 40px;'>");		
		writer.println("<h2>Failures:</h2>");
		int index = 0;
		for (Class<?> testCase : failResults.keySet()) {
			writer.println("<h3 id='" + testCase.getName() + "_image'><a href='#" + testCase.getName() + "'>" + testCase.getName() + "</a></h3>");
			writer.println("<section class='accordion'>");
			Result result = failResults.get(testCase);
			for (Failure failure : result.getFailures()) {
				writer.println("<div>");
				writer.println("<input type='checkbox' id='check-" + ++index + "'/>");
				writer.println("<label for='check-" + index + "'>" + failure.getTestHeader() + "</label>");
				writer.println("<article >");
				writer.println("<pre>" + failure.getTrace() + "</pre>");
				writer.println("</article>");
				writer.println("</div>");
			}
			writer.println("</section>");
		}
		writer.println("</div>");
	}

	protected static String getTime(long millis) {
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
}