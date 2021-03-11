package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestSuiteTM4JReport implements ITestSuiteReport {

	protected File reportFile;
	protected List<String> results;

	@Override
	public void openDoc(TestRunnerInfo info, File reportDir) throws IOException {
		reportFile = new File(reportDir, info.getFileName() + "-tm4j.json");
		results = new ArrayList<>();
	}
	

	@Override
	public void openBody(TestRunnerInfo info) {
	}

	public void addProperties(TestRunnerInfo info, Date startTime) {
	}

	@Override
	public void addHeader(TestRunnerInfo info, Date startTime) {
	}

	@Override
	public void openTest(TestRunnerInfo info) {
	}

	@Override
	public void addTest(TestRunnerInfo info, long time, List<Failure> failures, boolean ignored,
			Description description) {
		if (!ignored) {
			StringBuffer result = new StringBuffer();
			result.append("  {\n" + 
					"    \"source\" : \"" + description.getClassName() + "." + description.getMethodName() + "\",\n" + 
					"    \"result\" : \"" + (failures.size() == 0 ? "Passed" : "Failed") + "\"");
			Collection<Annotation> annotations = description.getAnnotations();
			if (annotations != null) {
				annotations.forEach(annotation -> {
					if (DisplayName.class.getName().equals(annotation.annotationType().getName())) {
						DisplayName displayName = (DisplayName)annotation;
						if (displayName.value() != null && !displayName.value().isEmpty()) {
							result.append(",\n      \"name\" : \"" + displayName.value() + "\"");
						}
						if (displayName.key() != null && !displayName.key().isEmpty()) {
							result.append(",\n      \"key\" : \"" + displayName.key() + "\"");
						}
					}
				});
			}
			
		
			results.add(result.toString() + "\n    }");
		}
	}

	@Override
	public void closeTest(TestRunnerInfo info) {
	}

	@Override
	public void addResult(TestRunnerInfo info, long time, Class<?> testCase, Result result, List<Failure> asserts,
			List<Failure> errors) {
	}

	@Override
	public void addReport(TestRunnerInfo info, long time, int totalTestCases, int totalSucceed, int totalFailed,
			int totalTests, int totalErrors, int totalAsserts, int totalIgnored) {
	}

	@Override
	public void addFailures(Map<Class<?>, Result> failResults) {
	}

	@Override
	public void closeBody(TestRunnerInfo info) {
	}

	
	@Override
	public void closeDoc() throws IOException {
		if (results.size() > 0) {
			PrintWriter writer = new PrintWriter(reportFile, "UTF-8");
			writer.println("{\n" + 
					"  \"version\" : 1,\n" + 
					"  \"executions\" : [ ");
			writer.println(String.join(",\n", results));
			writer.println("  ]\n}");
			writer.close();
		}
	}
}
