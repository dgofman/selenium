package com.softigent.selenium.core.test;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.InvalidArgumentException;

/**
 * Runs JUnit and TestNG tests and generates the Cucumber report for Zephyr Scale
 *
 * @author dgofman
 * @since 1.0
 */
public class BaseRunner {	
	protected CucumberReport report;
	protected String reportName;
	
	protected static Logger logger;
	
	static {
		logger = Logger.initRoot();
	}
	
	protected void run(String reportName) throws Exception {
		this.reportName = reportName;
		String token = System.getProperty("jiraApiKey");
		if (token != null) {
			String[] chunks = token.split("\\.");
			if (chunks.length != 3) {
				throw new InvalidArgumentException("Invalid Jira API token");
			}
			Base64.Decoder decoder = Base64.getUrlDecoder();
			String payload = new String(decoder.decode(chunks[1]));
			logger.info("JwT Payload: " + payload);
			JObject jPayload = new JObject(payload);
			Date exp = new Date(jPayload.toLong("exp") * 1000);
			logger.info("JwT Exp Date: " + exp);
			if (exp.getTime() < new Date().getTime()) {
				throw new Exception("Jira API token expired");
			}
		}
		logger.info(
				"\n'########:'########::'######::'########::::'########::'##::::'##:'##::: ##:'##::: ##:'########:'########::\r\n"
				+ "... ##..:: ##.....::'##... ##:... ##..::::: ##.... ##: ##:::: ##: ###:: ##: ###:: ##: ##.....:: ##.... ##:\r\n"
				+ "::: ##:::: ##::::::: ##:::..::::: ##::::::: ##:::: ##: ##:::: ##: ####: ##: ####: ##: ##::::::: ##:::: ##:\r\n"
				+ "::: ##:::: ######:::. ######::::: ##::::::: ########:: ##:::: ##: ## ## ##: ## ## ##: ######::: ########::\r\n"
				+ "::: ##:::: ##...:::::..... ##:::: ##::::::: ##.. ##::: ##:::: ##: ##. ####: ##. ####: ##...:::: ##.. ##:::\r\n"
				+ "::: ##:::: ##:::::::'##::: ##:::: ##::::::: ##::. ##:: ##:::: ##: ##:. ###: ##:. ###: ##::::::: ##::. ##::\r\n"
				+ "::: ##:::: ########:. ######::::: ##::::::: ##:::. ##:. #######:: ##::. ##: ##::. ##: ########: ##:::. ##:\r\n"
				+ ":::..:::::........:::......::::::..::::::::..:::::..:::.......:::..::::..::..::::..::........::..:::::..::");
	}
	
	public void save() throws Exception {
		save(false);
	}

	@SuppressWarnings("deprecation")
	public void save(boolean killRunningThreads) throws Exception {
		File reportDir = new File("reports");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}
		File file = new File(reportDir, reportName + "-cucumber.json");
		save(new PrintWriter(file, "UTF-8"));
		if (System.getProperty("jiraApiKey") != null) {
			JiraReport report = new JiraReport(System.getProperty("jiraApiKey"));
			report.submit(file.getAbsolutePath());
			System.out.println(report.getUrl());
			System.out.println("\nJIRA Report published: " + file.getAbsolutePath());
		}
		
		if (killRunningThreads) {
			//Terminate Running threads
			for (Thread t : Thread.getAllStackTraces().keySet()) { 
				if (t != Thread.currentThread() && t.getState() == Thread.State.RUNNABLE) {
					t.interrupt(); 
				}
			}
			
			//Terminate wait termination threads
			for (Thread t : Thread.getAllStackTraces().keySet()) { 
				if (t.getState() == Thread.State.TIMED_WAITING) {
					t.stop(); 
				}
			}
	
			if (report.getNumSkipped() > 0) {
				logger.info("Total tests skips: " + report.getNumSkipped());
			}
			if (report.missingTags().size() > 0) {
				logger.warn("Missing Tags: " + report.missingTags());
			}
			if (report.getNumFailures() > 0) {
				throw new AssertionError("Report Failures: " + report.getNumFailures());
			}
		}
	}

	public void save(Writer writer) throws Exception {
		report.save(writer);
	}
	
	public List<String> getElements() {
		return report.getElements();
	}
}
