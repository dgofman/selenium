package com.softigent.selenium.core.test;

import org.junit.Test;

public class TestJiraReport {
	
	private final String jiraApiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
	
	@Test
	public void testGetCycleNameFromReport() throws Exception {
		JiraReport report = new JiraReport(jiraApiKey); //Get projectKey and cycle name from the json
		report.submit("src/main/resources/sample_cucumber_report.json");
	}

	@Test
	public void testSetCycleName() throws Exception {
		JiraReport report = new JiraReport(jiraApiKey, "seleniumAGL", "CucumberTestCycle"); //Specify the cycle name
		report.submit("src/main/resources/sample_cucumber_report.json");
	}
}
