package com.softigent.sftselenium;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	protected Description description;
	protected Map<TestRunnerInfo, Description> mapInfo;

	//Application Contractor
	public TestRunner(String[] args) {
		List<TestRunnerInfo> suites = this.initialize(args);
		try {
			new com.softigent.sftselenium.TestSuiteRunner(suites).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//JUnit Contractor
	public TestRunner(Class<?> klass, String description) {
		List<TestRunnerInfo> suites = this.initialize(SeleniumUtils.getCmdArguments());
		this.description = Description.createSuiteDescription(description);
		mapInfo = new LinkedHashMap<TestRunnerInfo, Description>(suites.size());
		for (TestRunnerInfo info : suites) {
			Description desc = Description.createSuiteDescription(info.getDescription());
			this.description.addChild(desc);
			mapInfo.put(info, desc);
		}
	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public void run(RunNotifier notifier) {
		for (Entry<TestRunnerInfo, Description> entry : mapInfo.entrySet()) {
			Description desc_suite = entry.getValue();
			notifier.fireTestStarted(desc_suite);
			Class<?> testSuite = entry.getKey().getTestSuite();
			Suite.SuiteClasses annotation = (SuiteClasses) testSuite.getAnnotation(Suite.SuiteClasses.class);
			Class<?>[] suiteClassLst = annotation.value();
			boolean isFailed = false;
			for (Class<?> testCase : suiteClassLst) {
				Description desc_test = Description.createSuiteDescription(testCase.getName());
				notifier.fireTestStarted(desc_test);
				Request request = Request.aClass(testCase);
				Runner runner = request.getRunner();
				Result result = new Result();
		        RunListener listener = result.createListener();
		        notifier.addFirstListener(listener);
		        try {
		        	notifier.fireTestRunStarted(desc_test);
		            runner.run(notifier);
		            notifier.fireTestRunFinished(result);
		        } finally {
		        	notifier.removeListener(listener);
		        }
		        if (result.getFailures().isEmpty()) {
		        	notifier.fireTestFinished(desc_test);
		        } else {
		        	isFailed = true;
					notifier.fireTestFailure(new Failure(desc_test, new Exception()));
		        }
			}
			if (isFailed) {
				notifier.fireTestFailure(new Failure(desc_suite, new Exception()));
			} else {
				notifier.fireTestFinished(desc_suite);
			}
		}
	}

	protected abstract List<TestRunnerInfo> initialize(String[] args);
}