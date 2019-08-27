import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import com.equinix.etiming.ETimingBaseTest;
import com.softigent.sftselenium.ITestSuiteReport;
import com.softigent.sftselenium.TestError;
import com.softigent.sftselenium.TestRunner;
import com.softigent.sftselenium.TestRunnerInfo;
import com.softigent.sftselenium.TestSuiteHTMLReport;
import com.softigent.sftselenium.TestSuiteXMLReport;

import %PACKAGE%.%PROJECT%BaseTest;

@RunWith(%PROJECT%TestRunner.class)
public class %PROJECT%TestRunner extends TestRunner {
	
	//Application Contractor
	public %PROJECT%TestRunner(String[] args) throws Exception {
		super(args, new ITestSuiteReport[] {new TestSuiteCustomHTMLReport(), new TestSuiteXMLReport()});
	}
	
	//JUnit Contractor
	public %PROJECT%TestRunner(Class<?> klass) {
		super(klass, "%PROJECT%TestRunner");
	}
	
	@Override
	public void onExit(int numOfErrors) {
		System.exit(numOfErrors);
	}

	public static void main(String[] args) throws Exception {
		%PACKAGE%.%PROJECT%Config.initLogs();
		new %PROJECT%TestRunner(args);
	}

	protected List<TestRunnerInfo> initialize(String[] args) {
		List<TestRunnerInfo> suites = new ArrayList<TestRunnerInfo>();
		suites.add(new TestRunnerInfo("report", "%PROJECT% Report", %PACKAGE%.suites.%PROJECT%Suite.class));
		return suites;
	}
	
	static public class TestSuiteCustomHTMLReport extends TestSuiteHTMLReport {

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
					writer.println("<article>");
					
					TestError error = %PROJECT%BaseTest.TEST_ERRORS.get(failure.getDescription().getDisplayName());
					if (error != null) {
						File snapshot = error.getSnaphotFile();
						if (snapshot != null && snapshot.exists()) {
							try {
								FileInputStream fis = new FileInputStream(snapshot);
								byte imageData[] = new byte[(int) fis.available()];
								fis.read(imageData);
								String base64Image = Base64.getEncoder().encodeToString(imageData);
								fis.close();
								writer.println("<img src=\"data:image/png;base64, " + base64Image + "\"/>");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						writer.println("<h4>Last Logs:</h4>");
						writer.println("<pre>" + error.getLastLogs() + "</pre>");
						writer.println("<hr/>");
					}

					writer.println("<pre>" + failure.getTrace() + "</pre>");
					writer.println("</article>");
					writer.println("</div>");
				}
				writer.println("</section>");
			}
			writer.println("</div>");
		}
	}
}