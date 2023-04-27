package com.softigent.selenium.core.test;

import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class CucumberReport {
	
	private ClassPool pool;
	private String projectKey;
	private String cycleName;
	private List<String> elements;
	private int failures = 0;
	private int skipped = 0;
	private Set<String> missing = new HashSet<>();
	
	public static final String MISSING_TAG = "MISSING_TAG";
	public static final String PASSED = "passed";
	public static final String FAILED = "failed";
	public static final String SKIPPED = "skipped";
	
	private static final String testAnnotation = org.junit.Test.class.getName() + "|" +
												 org.junit.jupiter.api.Test.class.getName() + "|" +
												 org.testng.annotations.Test.class.getName();
	
	CucumberReport(String projectKey, String cycleName) {
		this.projectKey = projectKey;
		this.cycleName = cycleName;
		this.elements = new ArrayList<>();
		this.pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath(getClass()));
	}
	
	public int getNumFailures() {
		return failures;
	}
	
	public int getNumSkipped() {
		return skipped;
	}
	
	public Set<String> missingTags() {
		return missing;
	}

	protected void generateReport(List<ISuiteTest> suites, Map<String, List<IRunnerResult>> results) {
		for (ISuiteTest suite : suites) {
			for (IResultTest suiteResult : suite.getResults()) {
				Map<String, ClassInfo> classInfos = new HashMap<>();
				for (IClassTest test : suiteResult.getTestClasses()) {
					String className = test.getRealClass().getName();
					ClassInfo classInfo = classInfos.get(className);
					if (classInfo == null) {
						classInfo = new ClassInfo(test, className);
						classInfos.put(className, classInfo);
					}
					List<Method> methods = test.getTestMethods();
					for (Method method : methods) {
						Annotation[] annotations = method.getAnnotations();
						String methodName = method.getName();
						String[] tagName = getTagAndName(methodName, annotations, test);
						String tag = tagName[0];
						String name = tagName[1];

						String methodKey = className + "::" + methodName;
						List<IRunnerResult> testResults = results.get(methodKey);
						results.remove(methodKey);
						
						if (testResults == null) {
							for (Annotation a : annotations) {
								if (testAnnotation.contains(a.annotationType().getName())) {
									updateClassInfo(classInfo, methodName, null, name, SKIPPED, tag);
									break;
								}
							}
							continue;
						}
						
						for (IRunnerResult  testResult : testResults) {
							if (testResult.isFailedConfiguration()) {
								classInfo = updateClassInfo(classInfo, methodName, testResult, name, FAILED, tag);
							} else {
								classInfo = updateClassInfo(classInfo, methodName, testResult, name, (testResult.isFailed() ? FAILED : PASSED), tag);
							}
						}
					}
					
					Iterator<String> iterator = results.keySet().iterator();
					while(iterator.hasNext()) {
						String key = iterator.next();
						List<IRunnerResult> testResults = results.get(key);
						String[] classMethod = key.split("::");
						if (classMethod.length == 2 && className.equals(classMethod[0])) {
							iterator.remove();
							for (IRunnerResult testResult : testResults) {
								String methodName = classMethod[1];
								updateClassInfo(classInfo, methodName, testResult, methodName, FAILED, MISSING_TAG);
								break;
							}
						}
					}
					createTestReport(className, suiteResult, classInfos.get(className));
				}
			}
		}
		
		for (String key : results.keySet()) {
			System.out.println("Ignored test: " + key);
		}
	}
	
	private String[] getTagAndName(String name, Annotation[] annotations, IClassTest test) {
		String tag = MISSING_TAG;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				String description = test.getDescription(annotation);
				if (description == null || description.isEmpty()) {
					continue;
				}
				String[] key_name = description.split("::");
				if (key_name.length > 0) {
					tag = key_name[0];
					if (key_name.length > 1) {
						name += " (" + key_name[1] + ")";
					}
					name += " - " + tag;
					break;
				}
			}
		}
		return new String[]{tag, name};
	}
	
	private void createTestReport(String className, IResultTest suiteResult, ClassInfo classInfo) {
		if (classInfo != null) {
			int classLinenumber = 0;
			try {
				CtClass cc = pool.get(className);
				if (cc.getConstructors().length > 0) {
					classLinenumber = cc.getConstructors()[0].getMethodInfo().getLineNumber(0);
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
			
			//Inner sorting
			List<Step> firstSteps = new ArrayList<>();
			for (String tag : classInfo.tags.keySet()) {
				List<Step> steps = classInfo.tags.get(tag);
				Collections.sort(steps, stepSorting());
				if (steps.size() > 0) {
					firstSteps.add(steps.get(0));
				}
			}
			//Outside sorting
			Collections.sort(firstSteps, stepSorting());
			
			for (Step firstStep : firstSteps) {
				elements.add(createClass(suiteResult, className, classLinenumber, classInfo, classInfo.tags.get(firstStep.tagName), firstStep.tagName));
			}
		}
	}

	private Comparator<Step> stepSorting() {
		return new Comparator<Step>() {
		    @Override
		    public int compare(Step s1, Step s2) {
		    	Integer i1 = extractInt(s1.location);
		    	Integer i2 = extractInt(s2.location);
		    	if (i1 != null && i2 != null) {
		    		return i1 - i2;
		    	}
		    	return s1.location.compareTo(s2.location);
		    }
		    
		    Integer extractInt(String s) {
		        String num = s.replaceAll("\\D", "");
		        return num.isEmpty() ? null : Integer.parseInt(num);
		    }
		};
	}
	
	private ClassInfo updateClassInfo(ClassInfo classInfo, String methodName, IRunnerResult testResult, String name, String status, String tag) {
		String className = classInfo.className;
		String stepLinenumber = "0";
		try {
			CtClass cc = pool.get(className);
			String[] ccInfo = getMethodInfo(cc, methodName, className);
			className = ccInfo[0];
			stepLinenumber = ccInfo[1];
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		String location = className + "::" + methodName;
		
		if (FAILED.equals(status)) {
			failures++; //increment failure
		} else if (SKIPPED.equals(status)) {
			skipped++; //increment failure
		}

		if (MISSING_TAG == tag) {
			if (!classInfo.className.equals(className)) {
				name = className + "." + name;
			}
			
			//Get class description using annotation
			String[] tagName = getTagAndName(location, classInfo.test.getRealClass().getAnnotations(), classInfo.test);
			tag = tagName[0];
			name = tagName[1];
			
			if (MISSING_TAG == tag) {
				missing.add(name);
			}
		}
		
		List<Step> steps = classInfo.tags.get(tag);
		if (steps == null) {
			steps = new ArrayList<>();
		}
		
		Step step = new Step();
		step.tagName = tag;
		step.location = location;
				
		step.sb.append("\t\t\t\t\t{\n"
				+ "\t\t\t\t\t\t\"keyword\": \"Method\",\n"
				+ "\t\t\t\t\t\t\"name\": \"" + name + "\",\n"
				+ "\t\t\t\t\t\t\"line\": " + stepLinenumber + ",\n"
				+ "\t\t\t\t\t\t\"match\": {\n"
				+ "\t\t\t\t\t\t\t\"location\": \"" + step.location + "\"\n"
				+ "\t\t\t\t\t\t},\n" + 
				"\t\t\t\t\t\t\"result\": {\n");
		if (testResult != null && testResult.isFailed()) {
			String error = testResult.getThrowable().toString().replace("\n", "<br>").replaceAll("[^a-zA-Z0-9:-_<>]", " ").replace("\\", "\\\\");
			step.sb.append("\t\t\t\t\t\t\t\"error_message\": \"" + error + "\",\n");
		}
		if (testResult != null) {
			step.sb.append("\t\t\t\t\t\t\t\"duration\": "
					+ (testResult.getEndMillis() - testResult.getStartMillis()) + ",\n");
		} else {
			step.sb.append("\t\t\t\t\t\t\t\"duration\": 0,\n");
		}
		step.sb.append("\t\t\t\t\t\t\t\"status\": \"" + 
				status + 
				"\"\n" +
				"\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t}");
		
		steps.add(step);
		classInfo.tags.put(tag, steps);
		return classInfo;
	}
	
	private String createTag(String tagName) {
		if (tagName == MISSING_TAG) return "";
		return "\t\t\t\t\t{\n"
				+ "\t\t\t\t\t\t\"name\": \"@TestCaseKey\\u003d" + tagName+ "\"\n"
				+ "\t\t\t\t\t}";
	}
	
	private String createClass(IResultTest suiteResult, String name, int classLinenumber, ClassInfo classInfo, List<Step> steps, String tagName) {
		return "\t\t\t{\n" + 
				"\t\t\t\t\"start_timestamp\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(suiteResult.getStartMillis()) + "\",\n" + 
				"\t\t\t\t\"keyword\": \"Class\",\n" + 
				"\t\t\t\t\"type\": \"test\",\n" + 
				"\t\t\t\t\"name\": \"" + name + 
				"\",\n" + 
				"\t\t\t\t\"line\": " + 
				classLinenumber + 
				",\n" + 
				"\t\t\t\t\"steps\": [\n" + 
				String.join(",\n", steps.stream().map(Object::toString).toArray(String[]::new)) +
				"\n\t\t\t\t],\n" + 
				"\t\t\t\t\"tags\": [\n" + 
				createTag(tagName) + 
				"\n\t\t\t\t]" + 
				"\n\t\t\t}";
	}

	public void save(Writer writer) throws Exception {
		int classLinenumber = 0;
		try {
			CtClass cc = pool.get(getClass().getName());
			if (cc.getConstructors().length > 0) {
				classLinenumber = cc.getConstructors()[0].getMethodInfo().getLineNumber(0);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		writer.write("[\n");
		writer.write("\t{\n" + 
				"\t\t\"keyword\": \"Suite\",\n" + 
				"\t\t\"projectKey\": \"" + (this.projectKey != null ? projectKey : "") + "\",\n"+
				"\t\t\"name\": \"" + this.cycleName + "\",\n"+
				"\t\t\"uri\": \"" + getClass().getName() + "\",\n" + 
				"\t\t\"line\": " + classLinenumber + ",\n" +
				"\t\t\"elements\": [\n" + String.join(",\n", elements) + 
				"\n\t\t]\n\t}");
		writer.write("\n]");
		writer.close();
	}

	private String[] getMethodInfo(CtClass cc, String methodName, String className) throws NotFoundException {
		try {
			CtMethod methodX = cc.getDeclaredMethod(methodName);
			return new String[] { cc.getName(), String.valueOf(methodX.getMethodInfo().getLineNumber(0) - 1) };
		} catch (NotFoundException e) {
			if (cc.getSuperclass() != null) {
				return getMethodInfo(cc.getSuperclass(), methodName, className);
			} else {
				return new String[] { className, "-1" };
			}
		}
	}
	
	public List<String> getElements() {
		return elements;
	}
}

abstract class IRunnerResult {
	boolean isFailedConfiguration = false;
	
	public boolean isFailedConfiguration() {
		return isFailedConfiguration;
	}
	public void setFailedConfiguration(boolean value) {
		isFailedConfiguration = value;
	}
	
	abstract long getStartMillis();
	abstract long getEndMillis();
	abstract Throwable getThrowable();
	abstract boolean isFailed();
}

interface ISuiteTest {
	List<IResultTest> getResults();
}

interface IResultTest {
	List<IClassTest> getTestClasses();
	long getStartMillis();
	long getEndMillis();
}

interface IClassTest {
	Class<?> getRealClass();
	List<Method> getTestMethods();
	String getDescription(Annotation annotation);
}

class Step {
	StringBuffer sb = new StringBuffer();
	String location;
	String tagName;
	
	@Override
	public String toString() {
		return sb.toString();
	}
}

class ClassInfo {
	final IClassTest test;
	final String className;
	Map<String, List<Step>> tags = new HashMap<>();
	
	public ClassInfo(IClassTest test, String className) {
		this.test = test;
		this.className = className;
	}
}