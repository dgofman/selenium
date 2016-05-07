package com.softigent.sftselenium;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("rawtypes")
public class TestRunner {

	private static final Logger log = Logger.getLogger(TestRunner.class);
	
	private String title;
	private Class testSuite;
	
	protected PrintWriter writer;
	protected Date startTime;
	protected int totalTestCases = 0;
	protected int totalTests = 0;
	protected int failed = 0;
	protected int ignored = 0;
	protected int countSucceed = 0;
	protected int countFailed = 0;
	protected Map<Class<?>, Result> failResults;
	
	public TestRunner(String fileName, String title, Class testSuite) throws IOException {
		this.title = title;
		this.testSuite = testSuite;
		this.writer = new PrintWriter(fileName, "UTF-8");
	}

	public void run() {
		startTime = new Date();

		@SuppressWarnings("unchecked")
		Suite.SuiteClasses annotation = (SuiteClasses) testSuite.getAnnotation(Suite.SuiteClasses.class);
		writer.println("<!doctype html>");
		writer.println("<html lang='en'>");
		writer.println("<head>");
		writer.println("<meta charset='utf-8'>");
		addTitle(title);
		writer.println("<style>");
		writer.println("li, div {padding: 2px 5px; margin: 2px 0}");
		writer.println("p, h3 {padding: 3px; margin: 0;}");
		writer.println(".accordion input {display: none;}");
		writer.println(".accordion label {background: #eee; cursor: pointer; display: block; margin-bottom: .125em; padding: .25em 1em;}");
		writer.println(".accordion label:hover {background: #ccc;}");
		writer.println(".accordion input:checked + label {background: #ccc; color: white;}");
		writer.println(".accordion article {background: #f7f7f7; height:0px;overflow:hidden;}");
		writer.println(".accordion input:checked ~ article {height: auto;}");
		writer.println("</style>");
		writer.println("</head>\n");
		writer.println("<body>");
		addHeader(title);
		writer.println("<ul>");

		failResults = new LinkedHashMap<Class<?>, Result>();

		if (annotation != null) {
			Class<?>[] suiteClassLst = annotation.value();
			totalTestCases = suiteClassLst.length;
			for (Class<?> testCase : suiteClassLst) {
				log.info("Start: " + testCase.getName());
				Result result = JUnitCore.runClasses(testCase);
				for (Failure failure : result.getFailures()) {
					log.info(failure.toString());
				}
				log.info("End: " + testCase.getName() + " in " + getTime(result.getRunTime()) + "\nTests: "
						+ result.getRunCount() + "\nFailed: " + result.getFailureCount() 
						+ "\nIgnored: " + result.getIgnoreCount());

				addResult(testCase, result);

				totalTests += result.getRunCount();
				failed += result.getFailureCount();
				ignored += result.getIgnoreCount();
				if (result.wasSuccessful()) {
					countSucceed++;
				} else {
					failResults.put(testCase, result);
					countFailed++;
				}
			}
		}
		log.trace("COMPLETED in : " + getTime(new Date().getTime() - startTime.getTime()) 
				+ "\nTotal TestCases: " + totalTestCases
				+ "\nTotal TestCases Succeed: " + countSucceed 
				+ "\nTotal TestCases Failed: " + countFailed
				+ "\nTotal Tests: " + totalTests 
				+ "\nTotal Tests Failed: " + failed
				+ "\nTotal Tests Ignored: " + ignored);

		writer.println("</ul>");

		addReport();

		addFailures();

		writer.println("</body>");
		writer.println("</html>");

		writer.close();
	}
	
	protected void addTitle(String title) {
		writer.println("<title>" + title + "</title>");
	}
	
	protected void addHeader(String header) {
		writer.println("<h1>" + header + "</h1>");
		writer.println("<p>Start Time: " + DateFormat.getDateTimeInstance().format(startTime) + "</p>");
	}
	
	protected void addResult(Class testCase, Result result) {
		writer.println("<li style='background: " + (result.wasSuccessful() ? "lightgreen" : "lightcoral") + "'>");
		writer.println("<h3>" + testCase.getName() + "</h3>");
		writer.println("<p><b>Run Time</b>: " + getTime(result.getRunTime()) + "</p>");
		writer.println("<p><b>Tests</b>: " + result.getRunCount() + "</p>");
		writer.println("<p><b>Tests Failed</b>: " + result.getFailureCount() + "</p>");
		writer.println("<p><b>Tests Ignored</b>: " + result.getIgnoreCount() + "</p>");
		writer.println("</li>");
	}
	
	protected void addReport() {
		writer.println("<div style='background:lightskyblue; margin-top: 40px;'>");
		writer.println("<p><b>Total Time</b>: " + getTime(new Date().getTime() - startTime.getTime()) + "</p>");
		writer.println("<p><b>Total TestCases</b>: " + totalTestCases + "</p>");
		writer.println("<p><b>Total TestCases Succeed</b>: " + countSucceed + "</p>");
		writer.println("<p><b>Total TestCases Failed</b>: " + countFailed + "</p>");
		writer.println("<p><b>Total Tests</b>: " + totalTests + "</p>");
		writer.println("<p><b>Total Tests Failed</b>: " + failed + "</p>");
		writer.println("<p><b>Total Tests Ignored</b>: " + ignored + "</p>");
		writer.println("</div>");
	}
	
	protected void addFailures() {
		writer.println("<div style='margin-top: 40px;'>");		
		writer.println("<h2>Failures:</h2>");
		int index = 0;
		for (Class<?> testCase : failResults.keySet()) {
			writer.println("<h3>" + testCase.getName() + "</h3>");
			writer.println("<section class='accordion'>");
			Result result = failResults.get(testCase);
			for (Failure failure : result.getFailures()) {
				writer.println("<div>");
				writer.println("<input type='checkbox' id='check-" + ++index + "'/>");
				writer.println("<label for='check-" + index + "'>" + failure.getTestHeader() + "</label>");
				writer.println("<article>");
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