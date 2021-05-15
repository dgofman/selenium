package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

public class TestSuiteRunner {

	private static final Logger log = CacheLogger.getLogger(TestSuiteRunner.class);

	private List<TestRunnerInfo> suites;

	protected ITestSuiteReport[] reports;
	protected File reportDir;

	public TestSuiteRunner(List<TestRunnerInfo> suites) {
		this(suites, new ITestSuiteReport[] { new TestSuiteHTMLReport(), new TestSuiteXMLReport() });
	}

	public TestSuiteRunner(List<TestRunnerInfo> suites, ITestSuiteReport[] reports) {
		this.suites = suites;
		this.reports = reports;
		reportDir = new File("reports");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}
	}
	
	public File getReportDir() {
		return reportDir;
	}

	public int run() throws IOException {
		int suitesErrors = 0;
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

			Class<?> testSuiteRef = info.getTestSuite();

			List<ITestSuiteReport> openReports = new ArrayList<>();
			for (ITestSuiteReport report : reports) {
				try {
					report.openDoc(info, getReportDir());
				} catch (IOException e) {
					log.error(e.getMessage());
					continue;
				}
				openReports.add(report);
				report.openBody(info);
				report.addHeader(info, startTime);
				report.openTest(info);
			}

			Class<?>[] suiteClassLst = null;
			Suite.SuiteClasses annotation = (SuiteClasses) testSuiteRef.getAnnotation(Suite.SuiteClasses.class);
			if (annotation != null) {
				suiteClassLst = annotation.value();
			} else {
				Method method;
				try {
					method = testSuiteRef.getMethod("suite");
					if (method != null) {
						suiteClassLst = (Class<?>[]) method.invoke(null);
					}
				} catch (Exception e) {}
			}
			
			if (suiteClassLst != null) {
				totalTestCases = suiteClassLst.length;
				for (Class<?> testSuite : suiteClassLst) {
					log.info("Start: " + testSuite.getName());
					try {
						Result result = getResult(openReports, testSuite, info);
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
						log.info("End: " + testSuite.getName() + " in " + getTime(result.getRunTime()) + "\nTests: "
								+ result.getRunCount() + "\nFailed: " + result.getFailureCount() + "\nIgnored: "
								+ result.getIgnoreCount());

						List<Failure> testCaseAsserts = new ArrayList<>();
						List<Failure> testCaseErrors = new ArrayList<>();
						for (Failure failure : result.getFailures()) {
							if (failure.getException() instanceof AssertionError) {
								testCaseAsserts.add(failure);
							} else {
								testCaseErrors.add(failure);
							}
						}
						for (ITestSuiteReport report : openReports) {
							report.addResult(info, result.getRunTime(), testSuite, result, testCaseAsserts,
									testCaseErrors);
						}

						totalTests += result.getRunCount();
						totalAsserts += asserts.size();
						totalErrors += errors.size();
						totalIgnored += result.getIgnoreCount();
						if (result.wasSuccessful()) {
							totalSucceed++;
						} else {
							failResults.put(testSuite, result);
							totalFailed++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			long time = new Date().getTime() - startTime.getTime();
			for (ITestSuiteReport report : openReports) {
				report.closeTest(info);
				report.addReport(info, time, totalTestCases, totalSucceed, totalFailed, totalTests, totalErrors,
						totalAsserts, totalIgnored);
				report.addFailures(failResults);
				report.closeBody(info);
				try {
					report.closeDoc();
				} catch (IOException e) {
					log.error(e.getMessage());
					continue;
				}
			}

			addLog(info, time, totalTestCases, totalSucceed, totalFailed, totalTests, totalErrors, totalAsserts,
					totalIgnored);
			suitesErrors += totalFailed;
		}
		return suitesErrors;
	}

	protected Result getResult(List<ITestSuiteReport> openReports, Class<?> testSuite, TestRunnerInfo info) {
		return new JUnitRunListener(openReports, testSuite, info).run();
	}

	protected void addLog(TestRunnerInfo info, long time, int totalTestCases, int totalSucceed, int totalFailed,
			int totalTests, int totalErrors, int totalAsserts, int totalIgnored) {
		log.trace("COMPLETED " + info.getDescription() + ",  in : " + getTime(time) + "\nTotal TestCases: "
				+ totalTestCases + "\nTotal TestCases Succeed: " + totalSucceed + "\nTotal TestCases Failed: "
				+ totalFailed + "\nTotal Tests: " + totalTests + "\nTotal Tests Ignored: " + totalIgnored
				+ "\nTotal Tests Errors: " + totalErrors + "\nTotal Tests Asserts: " + totalAsserts);
	}

	public static String getTime(long millis) {
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
}

class JUnitRunListener extends RunListener {

	protected List<ITestSuiteReport> openReports;
	protected Class<?> testSuite;
	protected TestRunnerInfo info;
	protected JUnitCore junit;
	
	private long startTime;
	private List<Failure> failures;
	
	public JUnitRunListener(List<ITestSuiteReport> openReports, Class<?> testSuite, TestRunnerInfo info) {
		this.openReports = openReports;
		this.testSuite = testSuite;
		this.info = info;
		
		junit = new JUnitCore();
		junit.addListener(this);
	}
	
	public Result run() {
		return junit.run(testSuite);
	}

	@Override
	public void testStarted(Description description) throws Exception {
		startTime = System.currentTimeMillis();
		failures = new ArrayList<Failure>();
		for (ITestSuiteReport report : openReports) {
			report.testStarted(info, startTime, description);
		}
    }
	
	@Override
	public void testFinished(Description description) throws Exception {
		long time = System.currentTimeMillis() - startTime;
		for (ITestSuiteReport report : openReports) {
			report.testFinished(info, time, failures, false, description);
		}
	}
	
	@Override
	public void testFailure(Failure failure) throws Exception {
		failures.add(failure);
    }
	
	@Override
	public void testAssumptionFailure(Failure failure) {
		failures.add(failure);
    }
	
	@Override
    public void testIgnored(Description description) throws Exception {
		long time = System.currentTimeMillis() - startTime;
		for (ITestSuiteReport report : openReports) {
			report.testFinished(info, time, new ArrayList<Failure>(), true, description);
		}
    }
}