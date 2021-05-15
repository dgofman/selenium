package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public interface ITestSuiteReport {
	
	void openDoc(TestRunnerInfo info, File file) throws IOException;
	void openBody(TestRunnerInfo info);
	void addHeader(TestRunnerInfo info, Date startTime);
	void openTest(TestRunnerInfo info);
	void testStarted(TestRunnerInfo info, long startTime, Description description);
	void testFinished(TestRunnerInfo info, long time, List<Failure> failures, boolean ignored, Description description);
	void closeTest(TestRunnerInfo info);
	void addResult(TestRunnerInfo info, long time, Class<?> testCase, Result result, List<Failure> asserts, List<Failure> errors);
	void addReport(TestRunnerInfo info, long time, int totalTestCases, int totalSucceed, int totalFailed, int totalTests, int totalErrors, int totalAsserts, int totalIgnored);
	void addFailures(Map<Class<?>, Result> failResults);
	void closeBody(TestRunnerInfo info);
	void closeDoc() throws IOException;
}