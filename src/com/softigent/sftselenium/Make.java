package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.softigent.sftselenium.rest.Client;
import com.softigent.sftselenium.rest.HostConfig;

public final class Make {
	
	@SuppressWarnings("serial")
	public static final Map<String, String[]> DRIVERS = new LinkedHashMap<String, String[]>() {{
		put("Chrome", new String[] {" - Google Chrome", "Selenium.WebDriver.ChromeDriver"});
		put("Firefox", new String[] {" - Mozilla Firefox", "Selenium.WebDriver.GeckoDriver"});
		put("IE", new String[] {" - Microsoft Internet Explorer", "Selenium.WebDriver.IEDriver"});
		put("Edge", new String[] {" - Microsoft Edge", "Selenium.WebDriver.MicrosoftWebDriver"});
		put("Phantom", new String[] {"- PhantomJS", "Selenium.PhantomJS.WebDriver"});
	}};
	
	/**
	 * @param args  First argument is config properties, Second argument is target (update/create - default), 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		prop.setProperty("log4j.rootLogger", "WARN");
		PropertyConfigurator.configure(prop);

		Properties config = new Properties();
		boolean isUpdate = false;
		for (String val : args) {
			if ("update".equals(val)) {
				isUpdate = true;
			} else {
				try {
					File propFile = new File(val);
					if (propFile.exists()) {
						config.load(new FileInputStream(propFile));
						if (propFile.getName().equals("config.properties") && config.getProperty("projectDir") == null) {
							config.setProperty("projectDir", propFile.getParentFile().getParent());
						}
					}
				} catch (Exception  e) {}
			}
		}
		if (isUpdate) {
			update(config);
		} else {
			create(config);
		}

	}
	
	private static void create(Properties config) throws Exception {
		Scanner in = new Scanner(System.in);
		if (config.getProperty("projectName") == null || config.getProperty("projectName").trim().isEmpty()) {
			config.setProperty("projectName", getProjectName(in));
		}
		if (config.getProperty("packageName") == null || config.getProperty("packageName").trim().isEmpty()) {
			config.setProperty("packageName", getPackageName(in));
		}
		if (config.getProperty("projectDir") == null || !new File(config.getProperty("projectDir")).exists()) {
			config.setProperty("projectDir", getProjectDir(in, config, "Output directory: "));
		}
		if (config.getProperty("driver") == null || !DRIVERS.containsKey(config.getProperty("driver"))) {
			config.setProperty("driver", getDriver(in));
		}
		File outputDir =  loadDrivers(in, config);
		File driverFile = selectDriver(in, outputDir, config);
		config.setProperty("driverPath", new File(config.getProperty("projectDir")).toURI().relativize(driverFile.toURI()).getPath());
		File resources = new File(config.getProperty("projectDir"), "resources");
		resources.mkdirs();
		copy(config, new File("resources/compile.bat"), new File(resources, "compile.bat"));
		copy(config, new File("resources/compile.sh"), new File(resources, "compile.sh"));
		copy(config, new File("resources/kill_browers.cmd"), new File(resources, "kill_browers.cmd"));
		copy(config, new File("resources/kill_drivers.cmd"), new File(resources, "kill_drivers.cmd"));
		copy(config, new File("resources/logviewer.html"), new File(resources, "logviewer.html"));

		copy(config, new File("resources/pom.xml"), new File(config.getProperty("projectDir"), "pom.xml"));
		copy(config, new File("resources/project"), new File(config.getProperty("projectDir"), ".project"));
		copy(config, new File("resources/classpath"), new File(config.getProperty("projectDir"), ".classpath"));
		copy(config, new File("resources/gitignore"), new File(config.getProperty("projectDir"), ".gitignore"));
		File src = new File(config.getProperty("projectDir"), "src");
		src.mkdirs();
		copy(config, new File("resources/TestRunner.java"), new File(src, config.getProperty("projectName").trim() + "TestRunner.java"));
		copy(config, new File("resources/config.properties"), new File(src, "config.properties"), true);
		copy(config, new File("resources/log4j.properties"), new File(src, "log4j.properties"));
		File pkg = new File(src, config.getProperty("packageName").trim().replaceAll("\\.", "/"));
		pkg.mkdirs();
		copy(config, new File("resources/BaseTest.java"), new File(pkg, config.getProperty("projectName").trim() + "BaseTest.java"));
		copy(config, new File("resources/Config.java"), new File(pkg, config.getProperty("projectName").trim() + "Config.java"));
		copy(config, new File("resources/Login.java"), new File(pkg, "Login.java"));
		File suites = new File(pkg, "suites");
		suites.mkdirs();
		copy(config, new File("resources/Suite.java"), new File(suites, config.getProperty("projectName").trim() + "Suite.java"));
		in.close();
		System.out.println("Thanks for installing Selenium Automation");
	}
	
	private static void update(Properties config) throws Exception {
		Scanner in = new Scanner(System.in);
		if (config.getProperty("projectDir") == null || !new File(config.getProperty("projectDir")).exists()) {
			config.setProperty("projectDir", getProjectDir(in, null, "Project directory: "));
		}
		if (config.getProperty("driver") == null || !DRIVERS.containsKey(config.getProperty("driver"))) {
			config.setProperty("driver", getDriver(in));
		}
		File outputDir =  loadDrivers(in, config);
		File driverFile = selectDriver(in, outputDir, config);
		config.setProperty("driverPath", new File(config.getProperty("projectDir")).toURI().relativize(driverFile.toURI()).getPath());
		File src = new File(config.getProperty("projectDir"), "src");
		copy(config, new File(src, "config.properties"), new File(src, "config.properties"), true);
		System.out.println("Dirver path updated");
	}
	
	private static void copy(final Properties config, File input, File output) throws IOException {
		copy(config, input, output, false);
	}

	private static void copy(final Properties config, File input, File output, boolean isProperites) throws IOException {
		String driver = config.getProperty("driver").trim();
		String content = IOUtils.toString(input.toURI(), Charset.defaultCharset());
		content = content.replaceAll("%PACKAGE%", config.getProperty("packageName", "").trim())
				.replaceAll("%PROJECT%", config.getProperty("projectName", "").trim());
		if (isProperites) {
			content = content.replaceAll("(driver=)(.*)", "$1" + driver)
				.replaceAll("(driverOS=)(.*)", "$1" + config.getProperty("driverOS"))
				.replaceAll("(" + config.getProperty("driverOS").substring(0, 3) + "_" + driver.toLowerCase() + "_driver_path=)(.*)", "$1" + config.getProperty("driverPath").trim());
		}
		FileOutputStream fos = new FileOutputStream(output);
		IOUtils.write(content, fos, Charset.defaultCharset());
		fos.close();
	}

	private static String getProjectName(final Scanner in) {
		System.out.print("Project name: ");
		return in.nextLine();
	}

	private static String getPackageName(final Scanner in) {
		System.out.print("Package name: ");
		return in.nextLine();
	}

	private static String getProjectDir(final Scanner in, Properties config, String message) {
		System.out.print(message);
		try {
			File dir = new File(in.nextLine());
			if (dir.exists() && dir.isDirectory()) {
				if (config != null) {
					dir = new File(dir, config.getProperty("projectName"));
				}
				return dir.getAbsolutePath();
			}
		} catch (Exception e) {}
		return getProjectDir(in, config, message);
	}

	private static String getDriver(final Scanner in) {
		Object[] drivers = DRIVERS.keySet().toArray();
		for (int i = 0; i < drivers.length; i++) {
			System.out.println((i + 1) + ". " + drivers[i] + DRIVERS.get(drivers[i])[0]);
		}
		System.out.print("Select driver (1 - " + drivers.length + "): ");
		try {
			int index = Integer.parseInt(in.nextLine());
			if (index >= 1 && index <= drivers.length) {
				return drivers[index - 1].toString();
			}
		} catch (Exception e) {
		}
		return getDriver(in);
	}

	public static File loadDrivers(final Scanner in, final Properties config) throws UnirestException, IOException {
		String dirverId = DRIVERS.get(config.getProperty("driver"))[1];
		System.out.println("Please wait loading " + dirverId + " ...");
		JSONArray versions = getVersions(dirverId);
		JSONObject item = null;
		if (config.getProperty("driverVersion") != null) {
			if ("latest".equals(config.getProperty("driverVersion"))) {
				item = versions.getJSONObject(versions.length() - 1);
			} else {
				for (int i = 0; i < versions.length(); i++) {
					if (config.getProperty("driverVersion").equals(versions.getJSONObject(i).getString("version"))) {
						item = versions.getJSONObject(i);
						break;
					}
				}
			}
		}
		if (item == null) {
			item = selectVersion(in, versions);
		}
		String version = item.getString("version");

		File driversDir = new File(config.getProperty("projectDir"), "drivers");
		driversDir.mkdirs();
		File outputDir = new File(driversDir, dirverId + File.separator + version);
		outputDir.mkdirs();
		downloadDrivers(item.getString("@id"), outputDir);
		return outputDir;
	}

	private static JSONArray getVersions(String dirverName) throws UnirestException {
		HttpResponse<JsonNode> res = Client.getRequest(new HostConfig("api-v2v3search-0.nuget.org", "443", true, null),
				"/query?q=" + dirverName);
		return res.getBody().getObject().getJSONArray("data").getJSONObject(0).getJSONArray("versions");
	}

	private static JSONObject selectVersion(final Scanner in, final JSONArray versions) {
		if (versions.length() == 1) {
			return versions.getJSONObject(0);
		}
		for (int i = 0; i < versions.length(); i++) {
			System.out.println((i + 1) + ". Version: " + versions.getJSONObject(i).getString("version"));
		}
		System.out.print("Select index (1 - " + versions.length() + "): ");
		try {
			int index = Integer.parseInt(in.nextLine());
			return versions.getJSONObject(index - 1);
		} catch (Exception e) {
		}
		return selectVersion(in, versions);
	}
	
	private static void downloadDrivers(final String path, final File outputDir) throws IOException, UnirestException {
		String packageContent = Client.getRequest(path, null, JsonNode.class).getBody().getObject()
				.getString("packageContent");
		File zipFile = new File(outputDir.getAbsolutePath() + ".zip");
		if (zipFile.createNewFile()) {
			FileOutputStream fos = new FileOutputStream(zipFile);
			IOUtils.copy(new URL(packageContent).openStream(), fos);
			fos.close();
			FileInputStream fis = new FileInputStream(zipFile);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				if (fileName.startsWith("driver")) {
					File newFile = new File(outputDir, fileName.substring(7));
					System.out.println("Unzipping " + newFile.getAbsolutePath());
					new File(newFile.getParent()).mkdirs();
					fos = new FileOutputStream(newFile);
					IOUtils.copy(zis, fos);
					fos.close();
					newFile.setExecutable(true);
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			fis.close();
			zipFile.delete();
		}
	}

	private static File selectDriver(final Scanner in, final File outputDir, final Properties config) {
		File[] envs = outputDir.listFiles();
		if (envs.length == 0) {
			System.out.print("WARN: Cannot find drivers for your browser");
		} else if (envs.length == 1) {
			return envs[0];
		}
		for (int i = 0; i < envs.length; i++) {
			if (envs[i].getName().equals(config.getProperty("driverOS"))) {
				File file = envs[i];
				if (file.isDirectory()) {
					return file.listFiles()[0];
				}
				return file;
			}
		}
		for (int i = 0; i < envs.length; i++) {
			System.out.println((i + 1) + ". " + envs[i].getName());
		}
		System.out.print("Select operating system (1 - " + envs.length + "): ");
		try {
			int index = Integer.parseInt(in.nextLine());
			File file = envs[index - 1];
			config.setProperty("driverOS", file.getName());
			if (file.isDirectory()) {
				return file.listFiles()[0];
			}
			return file;
		} catch (Exception e) {
		}
		return selectDriver(in, outputDir, config);
	}
}
