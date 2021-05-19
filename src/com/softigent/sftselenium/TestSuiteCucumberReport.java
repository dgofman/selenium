package com.softigent.sftselenium;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

class TestCase {
	final long startTime;
	final List<String> tests;
	final List<String> tags;
	final Description description;
	
	TestCase(long time, Description desc) {
		startTime = time;
		description = desc;
		tests = new ArrayList<>();
		tags  = new ArrayList<>();
	}
}

public class TestSuiteCucumberReport implements ITestSuiteReport {

	protected File reportFile;
	protected PrintWriter writer;
	protected List<String> testsuites;
	protected Map<String, TestCase> testcases;
	

	@Override
	public void openDoc(TestRunnerInfo info, File reportDir) throws IOException {
		reportFile = new File(reportDir, info.getFileName() + "-cucumber.json");
		writer = new PrintWriter(reportFile, "UTF-8");
		testsuites = new ArrayList<>();
		testcases  = new LinkedHashMap<>();
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
	public void testStarted(TestRunnerInfo info, long startTime, Description description) {
		if (!testcases.containsKey(description.getClassName())) {
			testcases.put(description.getClassName(), new TestCase(startTime, description));
		}
	}

	@Override
	public void testFinished(TestRunnerInfo info, long time, List<Failure> failures, boolean ignored,
			Description description) {
		ClassPool pool = ClassPool.getDefault();
		TestCase testcase =  testcases.get(description.getClassName());
		if (!ignored) {
			Collection<Annotation> annotations = description.getAnnotations();
			if (annotations != null) {
				annotations.forEach(annotation -> {
					if (DisplayName.class.getName().equals(annotation.annotationType().getName())) {
						DisplayName displayName = (DisplayName)annotation;
						String method = description.getMethodName();
						String name = method;
						if (displayName.value() != null && !displayName.value().isEmpty()) {
							name += " (" + displayName.value() + ")";
						}
						if (displayName.key() != null && !displayName.key().isEmpty()) {
							String stepLinenumber = "0";
							String lookup = description.getClassName();
							try {
						        CtClass cc = pool.get(description.getClassName());
						        String[] ccInfo = getMethodInfo(cc, description.getMethodName());
						        lookup = ccInfo[0];
						        stepLinenumber = ccInfo[1];
							} catch (NotFoundException e) {
								e.printStackTrace();
							}
							if (displayName.key() != null && !displayName.key().equals("")) {
								name +=  " - " + displayName.key();
								String tag = "			{\n" + 
										"				\"name\": \"@TestCaseKey\\u003d" + displayName.key() + "\"\n" + 
										"			}";
								if (!testcase.tags.contains(tag)) {
									testcase.tags.add(tag);
								}
							}
							String step = "\t\t\t{\n" + 
							"				\"keyword\": \"Method\",\n" +
							"				\"name\": \"" + name + "\",\n" + 
							"				\"line\": " + stepLinenumber  + ",\n" +
							"				\"match\": {\n" + 
							"					\"location\": \"" + lookup + "\"\n" + 
							"				},\n" + 
							"				\"result\": {\n";
							if (failures.size() > 0) {
								step += "					\"error_message\": " + failures.get(0).getMessage().replaceAll("\n",  "\\n").replaceAll("\"",  "'") + ",\n";
							}
							step += "					\"duration\": " + time + ",\n" + 
							"					\"status\": \"" + (failures.size() == 0 ? "passed" : "failed")  + "\"\n" +
							"				}\n" + 
							"\t\t\t}";
							testcase.tests.add(step);
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
	public void addResult(TestRunnerInfo info, long time, Class<?> testSuite, Result result, List<Failure> asserts,
			List<Failure> errors) {
		ClassPool pool = ClassPool.getDefault();
		List<String> elements = new ArrayList<>();
		for (String className : testcases.keySet()) {
			TestCase testcase = testcases.get(className);
			int classLinenumber = 0;
			try {
				
		        CtClass cc = pool.get(className);
		        if (cc.getConstructors().length > 0) {
		        	classLinenumber = cc.getConstructors()[0].getMethodInfo().getLineNumber(0);
		        }
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
			elements.add("		{\n" + 
			"\t\t\t\"start_timestamp\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(testcase.startTime) + "\",\n" + 
			"\t\t\t\"keyword\": \"Class\",\n" +
			"\t\t\t\"type\": \"test\",\n" +
			"\t\t\t\"name\": \"" + className + "\",\n" + 
			"\t\t\t\"line\": " + classLinenumber  + ",\n" +
			"\t\t\t\"steps\": [\n" +
					String.join(",\n", testcase.tests) +
					"\n		],\n" +
					"		\"tags\": [\n" + 
					String.join(",\n", testcase.tags) +
					"\n		]}");			
		}
		
		
		testsuites.add("\t{\n" +
				"\t\t\"keyword\": \"Suite\",\n" +
				"\t\t\"name\": \"" + testSuite.getSimpleName() + "\",\n" +
				"\t\t\"uri\": \"" +  testSuite.getName() + "\",\n" +
				"\t\t\"elements\": [\n" +
			String.join(",\n", elements) +
		"\n\t]}");
		testcases.clear();
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
		writer.println("[");
		writer.println(String.join(",\n", testsuites));
		writer.println("]");
		writer.close();
	}
	
	String[] getMethodInfo(CtClass cc, String methodName) throws NotFoundException {
		try {
	        CtMethod methodX = cc.getDeclaredMethod(methodName);
	        return new String[] {cc.getName(), String.valueOf(methodX.getMethodInfo().getLineNumber(0) - 1) };
		} catch (NotFoundException e) {
			return getMethodInfo(cc.getSuperclass(), methodName);
		}
	}
}
