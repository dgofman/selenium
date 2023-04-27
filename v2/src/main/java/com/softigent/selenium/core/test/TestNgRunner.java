package com.softigent.selenium.core.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ISuite;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.TestRunner;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Runs JUnit and TestNG tests and generates the Cucumber report for Zephyr Scale
 *
 * @author dgofman
 * @since 1.0
 */
public class TestNgRunner  extends BaseRunner {
	private String[] testSuites;

	public TestNgRunner addSuite(String ...xmlSuitePaths) throws IOException {
		this.testSuites = xmlSuitePaths;
		return this;
	}
	
	public void run(String reportName, String cycleName) throws Exception {
		run(reportName, null, cycleName);
	}
	
	public void run(String reportName, String projectKey, String cycleName) throws Exception {
		run(reportName, projectKey, cycleName, new Class<?>[0]);
	}
	
	public void run(String reportName, String cycleName, Class<?> ...tests) throws Exception {
		run(reportName, null, cycleName, tests);
	}
	
	public void run(String reportName, String projectKey, String cycleName, Class<?> ...tests) throws Exception {
		super.run(reportName);
		Map<String, List<IRunnerResult>> results = new HashMap<>();

		ExtendTestNG testNG = new ExtendTestNG();
		testNG.setDefaultSuiteName(projectKey);
		testNG.setDefaultTestName(cycleName);
		testNG.addListener((ITestNGListener) new ITestListener() {
			public RunnerResult addResult(ITestResult result) {
				String key = result.getTestClass().getName() + "::" + result.getMethod().getMethodName();
				List<IRunnerResult> rr = results.get(key);
				if (rr == null) { 
					rr = new ArrayList<>();
					results.put(key, rr);
				}
				RunnerResult runnerResult = new RunnerResult(result);
				rr.add(runnerResult);
				return runnerResult;
			}
			
			@Override
			public void onTestSuccess(ITestResult result) {
				addResult(result);
			}

			@Override
			public void onTestFailure(ITestResult result) {
				addResult(result);

			}

			@Override
			public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
				addResult(result);
			}

			@Override
			public void onTestStart(ITestResult result) {
			}

			@Override
			public void onStart(ITestContext context) {
			}

			@Override
			public void onFinish(ITestContext context) {
				context.getFailedConfigurations().getAllResults().forEach(result -> {
					addResult(result).setFailedConfiguration(true);
				});
			}

			@Override
			public void onTestSkipped(ITestResult result) {
			}
		});

		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		if (testSuites != null) {
			for (String xmlSuitePath : testSuites) {
				Collection<XmlSuite> xmlSuites = new Parser(new FileInputStream(new File(xmlSuitePath))).parse();
				suites.addAll(xmlSuites);
			}
		}
		if (tests.length > 0) {
			XmlSuite suite = new XmlSuite();
			suite.setName(cycleName);
			for (Class<?> test : tests) {
				XmlTest testSuite = new XmlTest(suite);
				testSuite.setName(test.getName());
				testSuite.setXmlClasses(Arrays.asList(new XmlClass[] { new XmlClass(test.getName()) }));
			}
			suites.add(suite);
		}
		testNG.setXmlSuites(suites);
		testNG.run();
		report = new CucumberReport(projectKey, cycleName);
		report.generateReport(testNG.getSuites(), results);
	}
}

class ExtendTestNG extends TestNG {
	List<ISuite> suiteRunners;

	@Override
	protected List<ISuite> runSuites() {
		suiteRunners = super.runSuites();
		return suiteRunners;
	}
	
	public List<ISuiteTest> getSuites() {
		List<ISuiteTest> suites = new ArrayList<>();
		for (org.testng.ISuite suite : suiteRunners) {
			suites.add(new TestNgSuite(suite));
		}
		return suites;
	}
}

class TestNgSuite implements ISuiteTest {
	private final ISuite suite;
	TestNgSuite(ISuite suite) { this.suite = suite; }
	
	@Override
	public List<IResultTest> getResults() {
		List<IResultTest> results = new ArrayList<>();
		for (org.testng.ISuiteResult suiteResult : suite.getResults().values()) {
			results.add(new TestNgResult((TestRunner) suiteResult.getTestContext()));
		}
		return results;
	}
}

class TestNgResult implements IResultTest {
	private final TestRunner runner;
	TestNgResult(TestRunner runner) { this.runner = runner; }

	@Override
	public List<IClassTest> getTestClasses() {
		List<IClassTest> testClasses = new ArrayList<>();
		for (ITestClass testClasse : runner.getTestClasses()) {
			testClasses.add(new TestNgClass(testClasse));
		}
		return testClasses;
	}
	
	@Override
	public long getStartMillis() {
		return runner.getStartDate().getTime();
	}
	
	@Override
	public long getEndMillis() {
		return runner.getEndDate().getTime();
	}
}

class TestNgClass implements IClassTest {
	private final ITestClass testClass;
	TestNgClass(ITestClass testClass) { this.testClass = testClass; }
	
	@Override
	public Class<?> getRealClass() {
		return testClass.getRealClass();
	}
	@Override
	public List<Method> getTestMethods() {
		List<Method> methods = new ArrayList<>();
		for (ITestNGMethod method : testClass.getTestMethods()) {
			methods.add(method.getConstructorOrMethod().getMethod());
		}
		return methods;
	}

	@Override
	public String getDescription(Annotation annotation) {
		String description = null;
		if (Description.class.getName().equals(annotation.annotationType().getName())) {
			description = ((Description) annotation).value();
		} else if (Test.class.getName().equals(annotation.annotationType().getName())) {
			description = ((Test) annotation).description();
		}
		return description;
	}
}

class RunnerResult extends IRunnerResult {
	private final ITestResult result;
	
	public RunnerResult(ITestResult result) { this.result = result; }
	
	@Override
	public boolean isFailed() {
		return result.getStatus() == ITestResult.FAILURE;
	}
	
	@Override
	public Throwable getThrowable() {
		return result.getThrowable();
	}
	
	@Override
	public long getStartMillis() {
		return result.getStartMillis();
	}
	
	@Override
	public long getEndMillis() {
		return result.getEndMillis();
	}
}