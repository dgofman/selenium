package com.softigent.sftselenium;

import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(TestRunner.class)
public abstract class TestRunner extends Runner {

	protected Class<?> testClass;
	protected Description description;

	// Application Contractor
	public TestRunner(String[] args) {
		List<TestRunnerInfo> suites = this.initialize(args);
		try {
			System.exit(getTestSuiteRunner(suites).run());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public TestRunner(Class<?> testClass, String description) {
		this.testClass = testClass;
		this.description = Description.createSuiteDescription(description);
	}

	public TestSuiteRunner getTestSuiteRunner(List<TestRunnerInfo> suites) {
		return new com.softigent.sftselenium.TestSuiteRunner(suites);
	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public void run(final RunNotifier notifier) {
		getResult(notifier);
	}

	public Result getResult() {
		return getResult(new RunNotifier());
	}

	public Result getResult(final RunNotifier notifier) {
		final Result result = new Result();
		final RunListener listener = new TestRunListener(this, result);
		notifier.addFirstListener(listener);
		Suite.SuiteClasses annotation = (SuiteClasses) testClass.getAnnotation(Suite.SuiteClasses.class);
		if (annotation != null) { // TestSuite
			boolean isFailed = false;
			notifier.fireTestStarted(description);
			Class<?>[] suiteClassLst = annotation.value();
			for (Class<?> testCase : suiteClassLst) {
				if (testCaseResult(notifier, testCase, result)) {
					isFailed = true;
				}
			}
			if (isFailed) {
				notifier.fireTestFailure(new Failure(description, new Exception()));
			} else {
				notifier.fireTestFinished(description);
			}
		} else {
			testCaseResult(notifier, testClass, result);
		}
		notifier.removeListener(listener);
		return result;
	}

	public boolean testCaseResult(final RunNotifier notifier, Class<?> testCase, Result result) {
		Description desc_test = Description.createSuiteDescription(testCase.getName());
		notifier.fireTestStarted(desc_test);
		Request request = Request.aClass(testCase);
		Runner runner = request.getRunner();
		try {
			notifier.fireTestRunStarted(desc_test);
			runner.run(notifier);
			notifier.fireTestRunFinished(result);
		} catch (Exception e) {

		}
		if (result.getFailures().isEmpty()) {
			notifier.fireTestFinished(desc_test);
		} else {
			notifier.fireTestFailure(new Failure(desc_test, new Exception()));
			return true;
		}
		return false;
	}

	protected abstract List<TestRunnerInfo> initialize(String[] args);

	public void testRunStarted(Description description) throws Exception {
	}

	public void testRunFinished(Result result) throws Exception {
	}

	public void testStarted(Description description) throws Exception {
	}

	public void testFinished(Description description) throws Exception {
	}

	public void testFailure(Failure failure) throws Exception {
	}

	public void testAssumptionFailure(Failure failure) {
	}

	public void testIgnored(Description description) throws Exception {
	}
}

class TestRunListener extends RunListener {

	final TestRunner runner;
	final RunListener listener;

	public TestRunListener(TestRunner runner, Result result) {
		this.runner = runner;
		this.listener = result.createListener();
	}

	public void testRunStarted(Description description) throws Exception {
		listener.testRunStarted(description);
		runner.testRunStarted(description);
	}

	public void testRunFinished(Result result) throws Exception {
		listener.testRunFinished(result);
		runner.testRunFinished(result);
	}

	public void testStarted(Description description) throws Exception {
		listener.testStarted(description);
		runner.testStarted(description);
	}

	public void testFinished(Description description) throws Exception {
		listener.testFinished(description);
		runner.testFinished(description);
	}

	public void testFailure(Failure failure) throws Exception {
		listener.testFailure(failure);
		runner.testFailure(failure);
	}

	public void testAssumptionFailure(Failure failure) {
		listener.testAssumptionFailure(failure);
		runner.testAssumptionFailure(failure);
	}

	public void testIgnored(Description description) throws Exception {
		listener.testIgnored(description);
		runner.testIgnored(description);
	}
}