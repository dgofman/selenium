package com.softigent.sftselenium;

import java.io.File;

import org.junit.runner.Description;

public class TestError {

	private String lastLogs;
	private File snaphotFile;
	private Description description;

	public TestError(String lastLogs, File snaphotFile, Description description) {
		this.lastLogs = lastLogs;
		this.snaphotFile = snaphotFile;
		this.description = description;
	}

	public File getSnaphotFile() {
		return snaphotFile;
	}
	public String getLastLogs() {
		return lastLogs;
	}
	public Description getDescription() {
		return description;
	}
}