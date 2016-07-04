package com.softigent.sftselenium;

public class TestRunnerInfo {
	
	private String fileName;
	private String description;
	private Class<?> testSuite;
	
	public TestRunnerInfo (String fileName, String description, Class<?> testSuite) {
		this.fileName = fileName;
		this.description = description;
		this.testSuite = testSuite;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDescription() {
		return description;
	}

	public Class<?> getTestSuite() {
		return testSuite;
	}
}
