package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestSuiteCucumberReport implements ITestSuiteReport {

	protected PrintWriter writer;
	protected List<String> results;

	@Override
	public void openDoc(TestRunnerInfo info, File reportDir) throws IOException {
		writer = new PrintWriter(new File(reportDir, info.getFileName() + "-cucumber.json"), "UTF-8");
		writer.println("[{\n" + 
				"	\"elements\": [");
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
			Collection<Annotation> annotations = description.getAnnotations();
			if (annotations != null) {
				annotations.forEach(annotation -> {
					if (DisplayName.class.getName().equals(annotation.annotationType().getName())) {
						DisplayName displayName = (DisplayName)annotation;
						String name = "";
						if (displayName.value() != null && !displayName.value().isEmpty()) {
							name = displayName.value();
						}
						if (displayName.key() != null && !displayName.key().isEmpty()) {
							String method = description.getClassName() + "." + description.getMethodName();
							results.add("		{\n" + 
							"			\"start_timestamp\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new Date()) + "\",\n" + 
							"			\"name\": \"" + name + " (" + method + ")\",\n" + 
							"			\"type\": \"scenario\",\n" + 
							"			\"keyword\": \"Scenario\",\n" + 
							"			\"steps\": [{\n" + 
							"				\"result\": {\n" + 
							"					\"duration\": " + time + ",\n" + 
							"					\"status\": \"" + (failures.size() == 0 ? "passed" : "failed")  + "\"\n" + 
							"				},\n" + 
							"				\"match\": {\n" + 
							"					\"location\": \"" + method + "\"\n" + 
							"				}\n" + 
							"			}],\n" + 
							"			\"tags\": [{\n" + 
							"				\"name\": \"@TestCaseKey\\u003d" + displayName.key() + "\"\n" + 
							"			}]\n" + 
							"		}");
						}
					}
				});
			}
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
		writer.println(String.join(",\n", results));
		writer.println("	]\n}]");
		writer.close();
	}
}
