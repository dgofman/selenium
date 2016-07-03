package com.softigent.sftselenium;

@SuppressWarnings("rawtypes")
public class TestRunnerInfo {
	
	private String fileName;
	private String title;
	private Class testSuite;
	
	public TestRunnerInfo (String fileName, String title, Class testSuite) {
		this.fileName = fileName;
		this.title = title;
		this.testSuite = testSuite;
	}

	public String getFileName() {
		return fileName;
	}

	public String getTitle() {
		return title;
	}

	public Class getTestSuite() {
		return testSuite;
	}
}
