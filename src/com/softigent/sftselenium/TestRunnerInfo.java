package com.softigent.sftselenium;

import java.io.File;

public class TestRunnerInfo {
	
	private File file;
	private String description;
	private Class<?> testSuite;
	
	public TestRunnerInfo (File file, String description, Class<?> testSuite) {
		this.file = file;
		this.description = description;
		this.testSuite = testSuite;
	}

	public File getFile() {
		return file;
	}

	public String getDescription() {
		return description;
	}

	public Class<?> getTestSuite() {
		return testSuite;
	}
}
