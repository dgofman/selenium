package com.softigent.selenium.core.test;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JiraReport extends TestHttpHelper {
	
	protected static Logger logger;

	static {
		logger = Logger.initRoot();
	}
	
	// https://softigentjira.atlassian.net/plugins/servlet/ac/com.kanoah.test-manager/api-access-tokens?project.key={{PROJECT_KEY}}
	private final String jiraApiKey;
	private final String jiraFolderName;
	private final String jiraProjectKey;
	private final String jiraCycleName;
	
	private final static String DEFAULTFOLDERNAME = "Automated tests";
	private final static String BASEURL = "https://api.zephyrscale.smartbear.com/v2";
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private final static int MAXRESULTS = 15000;
	
	private String projectKey = null;
	private String jiraAccountId  = null;
	private Integer folderId  = null;
	private JObject testCycle = null;
	
	static {
		SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public JiraReport(String jiraApiKey) throws Exception {
		this(jiraApiKey, null);
	}
	
	public JiraReport(String jiraApiKey, String projectKey) throws Exception {
		this(jiraApiKey, projectKey, null);
	}
	
	public JiraReport(String jiraApiKey, String projectKey, String cycleName) throws Exception {
		this(jiraApiKey, projectKey, cycleName, null);
	}

	public JiraReport(String jiraApiKey, String projectKey, String cycleName, String folderName) throws Exception {
		if (folderName == null) {
			folderName = System.getenv("selenium_CORETEST_FOLDERNAME");
		}
		if (folderName == null) {
			folderName = System.getProperty("jiraFolderName", DEFAULTFOLDERNAME);
		}
		this.jiraApiKey = jiraApiKey;
		this.jiraProjectKey = projectKey;
		this.jiraCycleName = cycleName;
		this.jiraFolderName = folderName;
		if (getJWTPayload(jiraApiKey) != null && getJWTPayload(jiraApiKey).has("context") &&
				getJWTPayload(jiraApiKey) != null && getJWTPayload(jiraApiKey).getJObject("context").has("user")) {
			this.jiraAccountId = getJWTPayload(jiraApiKey).getJObject("context").getJObject("user").toString("accountId");
		}
	}
	
	/**
	 * Execute Report
	 * @param args[0] - @required Path to the cucumber report file
	 * @param args[1] - @required jiraApiKey - JIRA -> Account -> Zephyr Scale API Access Tokens
	 * @param args[2] - jiraProjectKey - JIRA Project Key
	 * @param args[3] - jiraCycleName  - JIRA Cycle Name
	 * @param args[4] - jiraFolderName  - JIRA Folder Name
 	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new JiraReport(args[1], args[2], args[3], args[4]).submit(args[0]);
	}
	
	public JObject getJWTPayload(String jwtToken) {
		String[] chunks = jwtToken.split("\\.");
		Base64.Decoder decoder = Base64.getDecoder();
		String payload = new String(decoder.decode(chunks[1]));
		return new JObject(payload);
	}
	
	public void setAccountId(String id) {
		this.jiraAccountId = id;
	}
	
	@Override
	protected Map<String, String> getDefaultHeader() {
		Map<String, String> header = super.getDefaultHeader();
		header.put("Authorization", "Bearer " + jiraApiKey);
		return header;
	}
	
	public Integer getFolderId(String projectKey) throws Exception {
		JObject node = getJSON(BASEURL + "/folders?projectKey=" + projectKey + "&folderType=TEST_CYCLE&maxResults=" + MAXRESULTS + "&startAt=0", 200);
		JArray values = node.getJArray("values");
		Integer folderId = null;
		for (int i = 0; i < values.length(); i++) {
			JObject folder = values.getJObject(i);
			if (folder.toString("name").equals(jiraFolderName)) {
				folderId = folder.toInt("id");
				break;
			}
		}
		if (folderId == null) {
			folderId = postJSON(BASEURL + "/folders", node(
				"name", jiraFolderName,
			    "projectKey", projectKey,
			    "folderType", "TEST_CYCLE"
			    ).toString(), 201).toInt("id");
		}
		return folderId;
	}
	
	public String getUrl() throws Exception {
		return "https://softigentjira.atlassian.net/projects/" + projectKey + "?selectedItem=com.atlassian.plugins.atlassian-connect-plugin:com.kanoah.test-manager__main-project-page#!/testPlayer/" + testCycle.toString("key");
	}
	
	public void submit(String reportPath) throws Exception {
		File reportFile = new File(reportPath);
		if (!reportFile.exists()) {
			throw new Exception("Invalid path: " + reportFile.getAbsolutePath());
		}
		JsonArray json = (JsonArray) JsonParser.parseReader(new FileReader(reportFile));
		if (json.size() == 0) {
			throw new Exception("Cannot find suites");
		}
		JsonObject suite = json.get(0).getAsJsonObject();
		
		projectKey = this.jiraProjectKey;
		if (projectKey == null) {
			projectKey = suite.get("projectKey").getAsString();
		}
		if (projectKey == null) {
			throw new Exception("Missing JIRA Project Key");
		}
		
		String cycleName = this.jiraCycleName;
		if (cycleName == null) {
			cycleName = suite.get("name").getAsString();
		}
		if (cycleName == null) {
			throw new Exception("Missing JIRA Cycle Name");
		}
		
		if (folderId == null) {
			folderId = getFolderId(projectKey);
		}
		
		JObject node = getJSON(BASEURL + "/testcycles?projectKey=" + projectKey + "&folderId=" + folderId + "&maxResults=" + MAXRESULTS + "&startAt=0", 200);
		JArray values = node.getJArray("values");
		for (int i = 0; i < values.length(); i++) {
			JObject cycle = values.getJObject(i);
			if (cycle.toString("name").equals(cycleName)) {
				testCycle = cycle;
				break;
			}
		}
		
		if (testCycle == null || !cycleName.equals(testCycle.toString("name"))) {
			testCycle = postJSON(BASEURL + "/testcycles", node(
				"name", cycleName,
			    "projectKey", projectKey,
			    "plannedStartDate", SDF.format(new Date()),
			    "plannedEndDate", SDF.format(new Date()),
			    "statusName", "DONE",
			    "folderId", folderId,
			    "ownerId", jiraAccountId
			).toString(), 201);
		}
		
		final Properties props = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("jira_report.properties");
		props.load(new InputStreamReader(is));
		JsonArray elements = suite.getAsJsonArray("elements");
		for (int e = 0; e < elements.size(); e++) {
			JsonObject test = (JsonObject) elements.get(e);
			String testCaseKey = null;
			JsonArray tags = test.getAsJsonArray("tags");
			boolean isFail = false;
			long totalDuration = 0;
			final StringBuffer bf = new StringBuffer();
			bf.append("<b>" + test.get("keyword").getAsString() + ": " + test.get("name").getAsString() + "</b><br>");
			bf.append("<u>Start Time: " + test.get("start_timestamp").getAsString() + "</u><br>");
			bf.append("<table width='100%'><tbody>");
			bf.append(props.getProperty("header"));
			for (int i = 0; i < tags.size(); i++) {
				JsonObject tagNode = (JsonObject) tags.get(i);
				if (tagNode.has("name")) {
					String tagName = tagNode.get("name").getAsString();
					if (tagName.startsWith("@TestCaseKey")) {
						testCaseKey = tagName.split("=")[1];
					}
				}
			}
			if (testCaseKey != null) {
				JsonArray steps = test.getAsJsonArray("steps");
				for (int i = 0; i < steps.size(); i++) {
					JsonObject step = (JsonObject) steps.get(i);
					JsonObject result = step.getAsJsonObject("result");
					String status = result.get("status").getAsString();
					long duration = result.get("duration").getAsLong();
					long millis = duration % 1000;
					long second = (duration / 1000) % 60;
					long minute = (duration / (1000 * 60)) % 60;
					long hour = (duration / (1000 * 60 * 60)) % 24;
					totalDuration += duration;
					
					String stepTr = props.getProperty("infoRow")
							.replace("%STEP%", step.get("name").getAsString())
							.replace("%LINE%", step.get("line").getAsString())
							.replace("%LOCATION%", step.getAsJsonObject("match").get("location").getAsString())
							.replace("%DURATION%", String.format("%02d:%02d:%02d.%d", hour, minute, second, millis));
					if (status.equals(CucumberReport.PASSED)) {
						bf.append(stepTr.replace("%STATUS%", props.getProperty("passTd")));
					} else if (status.equals(CucumberReport.SKIPPED)) {
						bf.append(stepTr.replace("%STATUS%", props.getProperty("skipTd")));
					} else if (status.equals(CucumberReport.FAILED)) {
						isFail = true;
						bf.append(stepTr.replace("%STATUS%", props.getProperty("failTd")));
						JsonElement error_message = result.get("error_message");
						if (error_message != null) {
							bf.append(props.getProperty("errorRow")
								.replace("%ERROR%", error_message.getAsString().replaceAll("\"", "\\u0022")));
						}
					}
				}
				bf.append("</tbody></table>");
				
				try {
					postJSON(BASEURL + "/testexecutions", node(
							"projectKey", projectKey,
							"testCycleKey", testCycle.toString("key"),
							"testCaseKey", testCaseKey,
							"statusName", isFail ? "fail" : "pass",
							"actualEndDate", SDF.format(new Date()),
							"executionTime", totalDuration,
							"executedById", jiraAccountId,
							"assignedToId", jiraAccountId,
							"comment", bf.toString()
							).toString(), 201);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
