package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestSuiteHTMLReport implements ITestSuiteReport {

	protected PrintWriter writer;

	@Override
	public void openDoc(TestRunnerInfo info, File reportDir) throws IOException {
		writer = new PrintWriter(new File(reportDir, info.getFileName() + ".html"), "UTF-8");
		writer.println("<!doctype html>");
		writer.println("<html lang='en'>");
	}
	
	public void openHead(TestRunnerInfo info) {
		writer.println("<head>");
		writer.println("<meta charset='utf-8'>");
		writer.println("<meta http-equiv='Cache-control' content='no-cache'>");
	}
	
	public void addTitle(TestRunnerInfo info) {
		writer.println("<title>" + info.getDescription() + "</title>");
	}
	
	public void addStyle(TestRunnerInfo info) {
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
	
	public void addScript(TestRunnerInfo info) {
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
	
	public void closeHead(TestRunnerInfo info) {
		writer.println("</head>");
	}
	
	@Override
	public void openBody(TestRunnerInfo info) {
		openHead(info);
		addTitle(info);
		addStyle(info);
		addScript(info);
		closeHead(info);
		writer.println("<body>");
	}
	
	@Override
	public void addHeader(TestRunnerInfo info, Date startTime) {
		writer.println("<h1>" + info.getDescription() + "</h1>");
		writer.println("<p>Start Time: " + DateFormat.getDateTimeInstance().format(startTime) + "</p>");
		writer.flush();
	}
	
	@Override
	public void openTest(TestRunnerInfo info) {
		writer.println("<ul>");
	}
	
	@Override
	public void testStarted(TestRunnerInfo info, long startTime, Description description) {
	}
	
	@Override
	public void testFinished(TestRunnerInfo info, long time, List<Failure> failures, boolean ignored, Description description) {
	}

	@Override
	public void closeTest(TestRunnerInfo info) {
		writer.println("</ul>");
	}
	
	@Override
	public void addResult(TestRunnerInfo info, long time, Class<?> testSuite, Result result, List<Failure> asserts, List<Failure> errors) {
		writer.println("<li id='" + testSuite.getName() + "' style='background: " + (result.wasSuccessful() ? "lightgreen" : 
			errors.size() == 0 ? "orange" : "lightcoral") + "'>");
		writer.println("<a href='#" + testSuite.getName() + "_image'>");
		writer.println("<h3>" + testSuite.getName() + "</h3>");
		
		writer.println("<p><b>Run Time</b>: " + TestSuiteRunner.getTime(time) + "</p>");
		writer.println("<p><b>Tests Run Count</b>: " + result.getRunCount() + "</p>");
		writer.println("<p><b>Tests Ignored</b>: " + result.getIgnoreCount() + "</p>");
		writer.println("<p><b>Tests Failed</b>: " + result.getFailureCount() + "</p>");
		writer.println("<p><b>Errors</b>: " + errors.size() + "</p>");
		writer.println("<p><b>Asserts</b>: " + asserts.size() + "</p>");

		writer.println("</a>");
		writer.println("</li>");
		writer.println("<script>window.testcase='" + testSuite.getName()  + "'</script>");
		writer.flush();
	}
	
	@Override
	public void addReport(TestRunnerInfo info, long time, int totalTestCases, int totalSucceed, int totalFailed, int totalTests, int totalErrors, int totalAsserts, int totalIgnored) {
		writer.println("<div id='report' style='background:lightskyblue; margin-top: 40px;'>");
		writer.println("<p><b>Total Time</b>: " + TestSuiteRunner.getTime(time) + "</p></br>");
		writer.println("<p><b>Total TestCases</b>: " + totalTestCases + "</p>");
		writer.println("<p><b>Total TestCases Succeed</b>: " + totalSucceed + "</p>");
		writer.println("<p><b>Total TestCases Failed</b>: " + totalFailed + "</p><br/>");
		writer.println("<p><b>Total Tests</b>: " + totalTests + "</p>");
		writer.println("<p><b>Total Tests Ignored</b>: " + totalIgnored + "</p>");
		writer.println("<p><b>Total Tests Errors</b>: " + totalErrors + "</p>");
		writer.println("<p><b>Total Tests Asserts</b>: " + totalAsserts + "</p>");
		writer.println("</div>");
	}
	
	@Override
	public void addFailures(Map<Class<?>, Result> failResults) {
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
	
	@Override
	public void closeBody(TestRunnerInfo info) {
		writer.println("<script>window.complete = true;</script>");
		writer.println("</body>");
	}
	
	@Override
	public void closeDoc() throws IOException {
		writer.println("</html>");
		writer.close();
	}
}
