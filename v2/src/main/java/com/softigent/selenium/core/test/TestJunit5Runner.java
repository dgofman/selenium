package com.softigent.selenium.core.test;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Runs JUnit and TestNG tests and generates the Cucumber report for Zephyr
 * Scale
 *
 * @author dgofman
 * @since 1.0
 */
public class TestJunit5Runner extends BaseRunner {

	public void run(String reportName, String cycleName) throws Exception {
		run(reportName, null, cycleName);
	}

	public void run(String reportName, String projectKey, String cycleName) throws Exception {
		run(reportName, projectKey, cycleName, new Class<?>[0]);
	}

	public void run(String reportName, String cycleName, Class<?>... tests) throws Exception {
		run(reportName, null, cycleName, tests);
	}

	public void run(String reportName, String projectKey, String cycleName, Class<?>... tests) throws Exception {
		super.run(reportName);
		final List<DiscoverySelector> selectors = new ArrayList<>();
		List<IClassTest> classTests = new ArrayList<>();
		for (Class<?> testClass : tests) {
			selectors.add(selectClass(testClass));
			classTests.add(new TesJUnitClass(testClass));
		}
		final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(selectors).build();

		Map<String, List<IRunnerResult>> results = new HashMap<>();
		LauncherConfig launcherConfig = LauncherConfig.builder().addTestExecutionListeners(new TestExecutionListener() {
			private long startTime;

			@Override
			public void executionStarted(TestIdentifier testIdentifier) {
				startTime = System.currentTimeMillis();
			}

			@Override
			public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
				if (testIdentifier.getSource().isPresent()) {
					if (testIdentifier.getSource().get() instanceof MethodSource) {
						List<Segment> segments = testIdentifier.getUniqueIdObject().getSegments();
						List<Segment> feature = segments.stream().filter(key -> key.getType().equals("feature"))
								.collect(Collectors.toList());
						MethodSource source = (MethodSource) testIdentifier.getSource().get();
						String key = source.getClassName() + "::"
								+ (feature.isEmpty() ? source.getMethodName() : feature.get(0).getValue());
						List<IRunnerResult> rr = results.get(key);
						if (rr == null) {
							rr = new ArrayList<>();
							results.put(key, rr);
						}
						rr.add(new JunitRunnerResult(result.getThrowable(), startTime, System.currentTimeMillis(),
								source.getMethodName()));
					}
				}
			}
		}).build();

		Long startTime = System.currentTimeMillis();
		Launcher launcher = LauncherFactory.create(launcherConfig);
		SummaryGeneratingListener listener = new SummaryGeneratingListener();
		launcher.registerTestExecutionListeners(listener);
		launcher.execute(request);
		TestExecutionSummary summary = listener.getSummary();
		logger.info(
				"\n------------------------------------------" + "\nTests started: " + summary.getTestsStartedCount()
						+ "\nTests failed: " + summary.getTestsFailedCount() + "\nTests succeeded: "
						+ summary.getTestsSucceededCount() + "\n------------------------------------------");

		if (summary.getTestsFailedCount() > 0) {
			for (TestExecutionSummary.Failure f : summary.getFailures()) {
				logger.error(f.getTestIdentifier().getSource() + "\nException " + f.getException());
			}
		}

		Long endTime = System.currentTimeMillis();
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

		TesJUnitClass(Class<?> testClass) {
			this.testClass = testClass;
		}

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
		private Optional<Throwable> failure;
		private long startTime;
		private long endTime;
		private String methodName;

		public JunitRunnerResult(Optional<Throwable> failure, long startTime, long endTime, String methodName) {
			this.failure = failure;
			this.startTime = startTime;
			this.endTime = endTime;
			this.methodName = methodName;
		}

		@Override
		public boolean isFailed() {
			return failure.isPresent();
		}

		@Override
		public Throwable getThrowable() {
			return failure.get();
		}

		@Override
		public long getStartMillis() {
			return startTime;
		}

		@Override
		public long getEndMillis() {
			return endTime;
		}

		public String getMethodName() {
			return methodName;
		}
	}
}