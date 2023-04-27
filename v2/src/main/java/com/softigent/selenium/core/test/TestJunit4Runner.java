package com.softigent.selenium.core.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Runs JUnit and TestNG tests and generates the Cucumber report for Zephyr Scale
 *
 * @author dgofman
 * @since 1.0
 */
public class TestJunit4Runner extends BaseRunner {
	
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
		JUnitCore junit = new JUnitCore();
		junit.addListener(new RunListener() {
			private long startTime;
			private List<Failure> failures;
			
			@Override
			public void testStarted(Description description) throws Exception {
				startTime = System.currentTimeMillis();
				failures = new ArrayList<>();
		    }
			
			@Override
			public void testFinished(Description description) throws Exception {
				String key = description.getClassName() + "::" + description.getMethodName();
				List<IRunnerResult> rr = results.get(key);
				if (rr == null) { 
					rr = new ArrayList<>();
					results.put(key, rr);
				}
				rr.add(new JunitRunnerResult(failures, startTime, System.currentTimeMillis()));
			}
			
			@Override
			public void testFailure(Failure failure) throws Exception {
				failures.add(failure);
		    }
			
			@Override
			public void testAssumptionFailure(Failure failure) {
				failures.add(failure);
		    }
		});
		Long startTime = System.currentTimeMillis();
		junit.run(tests);
		Long endTime = System.currentTimeMillis();
		List<IClassTest> classTests = new ArrayList<>();
		for (Class<?> testClass : tests) {
			classTests.add(new TesJUnitClass(testClass));
		}
		IResultTest suiteResult = new IResultTest() {
			@Override
			public List<IClassTest> getTestClasses() {
				return classTests;
			}
			@Override
			public long getStartMillis() {
				return startTime;
			}
			@Override
			public long getEndMillis() {
				return endTime;
			}
	    };
		
		List<ISuiteTest> suites = Arrays.asList(new ISuiteTest() {
			@Override
			public List<IResultTest> getResults() {
				return Arrays.asList(suiteResult);
			}
		});
		report = new CucumberReport(projectKey, cycleName);
		report.generateReport(suites, results);
	}
	
	class TesJUnitClass implements IClassTest {
		private final Class<?> testClass;
		TesJUnitClass(Class<?> testClass) { this.testClass = testClass; }
		
		@Override
		public Class<?> getRealClass() {
			return testClass;
		}
		@Override
		public List<Method> getTestMethods() {
			return Arrays.asList(testClass.getMethods());
		}
		@Override
		public String getDescription(Annotation annotation) {
			String description = null;
			if (com.softigent.selenium.core.test.Description.class.getName().equals(annotation.annotationType().getName())) {
				description = ((com.softigent.selenium.core.test.Description) annotation).value();
			}
			return description;
		}
	}
	
	class JunitRunnerResult extends IRunnerResult {
		private List<Failure> failures;
		private long startTime;
		private long endTime;
		
		public JunitRunnerResult(List<Failure> failures, long startTime, long endTime) { 
			this.failures = failures;
			this.startTime = startTime;
			this.endTime = endTime;
		}
		
		@Override
		public boolean isFailed() {
			return failures.size() != 0;
		}
		
		@Override
		public Throwable getThrowable() {
			return failures.get(0).getException();
		}
		
		@Override
		public long getStartMillis() {
			return startTime;
		}
		
		@Override
		public long getEndMillis() {
			return endTime;
		}
	}
}