package com.softigent.sftselenium;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
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
			int failed = 0;
			int ignored = 0;
			int countSucceed = 0;
			int countFailed = 0;
			Map<Class<?>, Result> failResults = new LinkedHashMap<Class<?>, Result>();
			
			Class<?> testSuite = info.getTestSuite();
			String description = info.getDescription();
			String fileName = info.getFileName();
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			
			Suite.SuiteClasses annotation = (SuiteClasses) testSuite.getAnnotation(Suite.SuiteClasses.class);
			writer.println("<!doctype html>");
			writer.println("<html lang='en'>");
			writer.println("<head>");
			writer.println("<meta charset='utf-8'>");
			addTitle(writer, description);
			writer.println("<style>");
			writer.println("li, div {padding: 2px 5px; margin: 2px 0}");
			writer.println("p, h3 {padding: 3px; margin: 0;}");
			writer.println("pre { white-space: pre-wrap;}");
			writer.println(".accordion input {display: none;}");
			writer.println(".accordion label {background: #eee; cursor: pointer; display: block; margin-bottom: .125em; padding: .25em 1em;}");
			writer.println(".accordion label:hover {background: #ccc;}");
			writer.println(".accordion input:checked + label {background: #ccc; color: white;}");
			writer.println(".accordion article {background: #f7f7f7; height:0px;overflow:hidden;}");
			writer.println(".accordion input:checked ~ article {height: auto;}");
			writer.println("</style>");
			writer.println("<script>function reload() { ");
			writer.println("	if (window.complete != true) {");
			writer.println("		if (window.new_test) window.scrollBy(0, 130);");
			writer.println("		location.reload();");
			writer.println("	}");
			writer.println("}setTimeout(reload, 3000);");
			writer.println("</script>");
			writer.println("</head>\n");
			writer.println("<body>");
			addHeader(writer, description, startTime);
			writer.flush();
			writer.println("<ul>");
			
			WebDriver driver;
	
			if (annotation != null) {
				Class<?>[] suiteClassLst = annotation.value();
				totalTestCases = suiteClassLst.length;
				for (Class<?> testCase : suiteClassLst) {
					log.info("Start: " + testCase.getName());
					try {
						Result result = JUnitCore.runClasses(testCase);
						for (Failure failure : result.getFailures()) {
							log.info(failure.toString());
						}
						log.info("End: " + testCase.getName() + " in " + getTime(result.getRunTime()) + "\nTests: "
								+ result.getRunCount() + "\nFailed: " + result.getFailureCount() 
								+ "\nIgnored: " + result.getIgnoreCount());
		
						addResult(writer, testCase, result);
		
						totalTests += result.getRunCount();
						failed += result.getFailureCount();
						ignored += result.getIgnoreCount();
						if (result.wasSuccessful()) {
							countSucceed++;
						} else {
							failResults.put(testCase, result);
							countFailed++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			Connector con = Connector.instance;
			if ((driver = getDriver()) != null && "true".equals(con.getConfig().getProperty("close_browser"))) {
				driver.close();
			}
			
			log.trace("COMPLETED " + description + ",  in : " + getTime(new Date().getTime() - startTime.getTime()) 
					+ "\nTotal TestCases: " + totalTestCases
					+ "\nTotal TestCases Succeed: " + countSucceed 
					+ "\nTotal TestCases Failed: " + countFailed
					+ "\nTotal Tests: " + totalTests 
					+ "\nTotal Tests Failed: " + failed
					+ "\nTotal Tests Ignored: " + ignored
					+ "\nReport File: " + fileName);
	
			writer.println("</ul>");
	
			addReport(writer, startTime, totalTestCases, countSucceed, countFailed, totalTests, failed, ignored);
	
			addFailures(writer, failResults);
	
			writer.println("<script>window.complete = true;</script>");
			writer.println("</body>");
			writer.println("</html>");
	
			writer.close();
		}
	}
	
	protected WebDriver getDriver() {
		Connector con = Connector.instance;
		if (con != null && con.getConfig() != null && con.getDriver() != null) {
			return con.getDriver();
		}
		return null;
	}
	
	protected void addTitle(PrintWriter writer, String description) {
		writer.println("<title>" + description + "</title>");
	}
	
	protected void addHeader(PrintWriter writer, String description, Date startTime) {
		writer.println("<h1>" + description + "</h1>");
		writer.println("<p>Start Time: " + DateFormat.getDateTimeInstance().format(startTime) + "</p>");
	}
	
	protected void addResult(PrintWriter writer, Class<?> testCase, Result result) {
		log.trace("<<<<<<<<<<<<<<<<<<<<<<<<<<<< ADD RESULT >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		writer.println("<li style='background: " + (result.wasSuccessful() ? "lightgreen" : "lightcoral") + "'>");
		writer.println("<h3>" + testCase.getName() + "</h3>");
		writer.println("<p><b>Run Time</b>: " + getTime(result.getRunTime()) + "</p>");
		writer.println("<p><b>Tests</b>: " + result.getRunCount() + "</p>");
		writer.println("<p><b>Tests Failed</b>: " + result.getFailureCount() + "</p>");
		writer.println("<p><b>Tests Ignored</b>: " + result.getIgnoreCount() + "</p>");
		writer.println("</li>");
		writer.println("<script>window.new_test=true</script>");
		writer.flush();
	}
	
	protected void addReport(PrintWriter writer, Date startTime, int totalTestCases, int countSucceed, int countFailed, int totalTests, int failed, int ignored) {
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
	
	protected void addFailures(PrintWriter writer, Map<Class<?>, Result> failResults) {
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