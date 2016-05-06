package com.softigent.sftselenium;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

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
		writer.println("</style>");
		writer.println("</head>\n");
		writer.println("<body>");
		addHeader(title);
		writer.println("<ul>");
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
						+ result.getRunCount() + "\nFailed: " + result.getFailureCount() + "\nIgnored: "
						+ result.getIgnoreCount() + "\nSucceed: " + result.wasSuccessful());

				addResult(testCase, result);

				totalTests += result.getRunCount();
				failed += result.getFailureCount();
				ignored += result.getIgnoreCount();
				if (result.wasSuccessful()) {
					countSucceed++;
				} else {
					countFailed++;
				}
			}
		}
		log.trace("COMPLETED in : " + getTime(new Date().getTime() - startTime.getTime()) 
				+ "\nTotal TestCases:" + totalTestCases
				+ "\nTotal Succeed: " + countSucceed + "\nTotal Failed: " + countFailed
				+ "\nTotal Tests: " + totalTests + "\nTotal Failed: " + failed
				+ "\nTotal Ignored: " + ignored);

		writer.println("</ul>");

		addComplete();

		writer.println("</body>\n");
		writer.println("</html>");

		writer.close();
	}
	
	public void addTitle(String title) {
		writer.println("<title>" + title + "</title>");
	}
	
	public void addHeader(String header) {
		writer.println("<h1>" + header + "</h1>");
		writer.println("<p>Start Time: " + DateFormat.getDateTimeInstance().format(startTime) + "</p>");
	}
	
	public void addResult(Class testCase, Result result) {
		writer.println("<li style='background: " + (result.wasSuccessful() ? "lightgreen" : "lightcoral") + "'>");
		writer.println("<h3>" + testCase.getName() + "</h3>");
		writer.println("<p><b>Run Time</b>: " + getTime(result.getRunTime()) + "</p>");
		writer.println("<p><b>Test Cases</b>: " + result.getRunCount() + "</p>");
		writer.println("<p><b>Failed</b>: " + result.getFailureCount() + "</p>");
		writer.println("<p><b>Ignored</b>: " + result.getIgnoreCount() + "</p>");
		writer.println("</li>");
	}
	
	public void addComplete() {
		writer.println("<div style='background:lightskyblue; margin-top: 40px;'>");
		writer.println("<p><b>Total Time</b>: " + getTime(new Date().getTime() - startTime.getTime()) + "</p>");
		writer.println("<p><b>Total TestCases</b>: " + totalTestCases + "</p>");
		writer.println("<p><b>Total TestCases Succeed</b>: " + countSucceed + "</p>");
		writer.println("<p><b>Total TestCases Failed</b>: " + countSucceed + "</p>");
		writer.println("<p><b>Total Run Tests</b>: " + totalTests + "</p>");
		writer.println("<p><b>Total Test Failed</b>: " + failed + "</p>");
		writer.println("<p><b>Total Test Ignored</b>: " + ignored + "</p>");
		writer.println("</div>");
	}

	public static String getTime(long millis) {
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
}