package %PACKAGE%;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;

import com.softigent.sftselenium.BaseTest;
import com.softigent.sftselenium.CacheLogger;
import com.softigent.sftselenium.Config;
import com.softigent.sftselenium.Element;
import com.softigent.sftselenium.SeleniumUtils;
import com.softigent.sftselenium.TestError;

public class %PROJECT%BaseTest extends BaseTest {

	public static final double MIN_GIT_VERSION = 7.0;
	
	public static final Map<String, TestError> TEST_ERRORS = new HashMap<String, TestError>();

	private static final Logger httpLog;
	private static final Logger httpErrorLog;
	
	private final File screenshotsDir;
	
	static {
		verifySeleniumVersion(MIN_GIT_VERSION);

		%PACKAGE%.%PROJECT%Config.initLogs();
		httpLog = Logger.getLogger("httplog");
		httpErrorLog = Logger.getLogger("httperror");
	}

	
	@Override
	protected void logTrafficResponse(List<?> res) {
		Long status = (Long) res.get(0);
		String info = res.get(1) + "::" + res.get(2) + " - " + status;

		if (res.get(2).toString().startsWith("/api/resources/translation")) {
			return;
		}
		Object header = res.get(4);
		Object request = SeleniumUtils.prettyJson(res.get(3));
		Object reponse = SeleniumUtils.prettyJson(res.get(5));
		httpLog.debug(info + "\n<b>Request Body:</b>" + request + "\n\n<b>Response Header:</b>" + header + "\n<b>Response Body:</b>" + reponse);
		if (status < 200 || status > 206) {
			httpErrorLog.error(info + "\n<b>Request Body</b>:" + request + "\n\n<b>Response Header:</b>" + header + "\n<b>Response Body:</b>" + reponse);
			System.err.println("\u001b[1;35mREST " + info + "\n" +
			"\u001b[1;34mRequest Body:\u001b[0;30m " + request + "\n" + 
			"\u001b[1;31mResponse Body:\u001b[0;30m " + reponse + "\n" + 
			"\u001b[1;34mResponse Header:\u001b[0;30m " + header);
		} else {
			System.out.println("\u001b[1;35mREST " + info);
		}
	}

	public %PROJECT%BaseTest() {
		this(%PACKAGE%.%PROJECT%Config.getInstance());
	}

	public %PROJECT%BaseTest(Config config) {
		super(config);
		screenshotsDir = new File("screenshots");
		if (!screenshotsDir.exists()) {
			screenshotsDir.mkdirs();
		}
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		Element.assertErrorCollector = new ArrayList<>();
		doCloseDriver = System.getProperty("driver") == null ? CloseDriver.PASSED : CloseDriver.ALWAYS;
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void openPage(String url) {
		super.openPage(url, true);
	}

	@Override
	protected String doFailed(Throwable e, Description description) {
		CacheLogger.SKIP_LOGS = false;
		String fileName = description.getClassName() + '_' + description.getMethodName() + new Date().getTime() + ".png";
		fileName = fileName.replaceAll(" ", "").replaceAll("-", "_").replaceAll("::", "_");
		log.info("Creating an error snapshot: " + fileName);
		String logs = super.doFailed(e, description);
		File file = SeleniumUtils.screenshot(connector.getDriver(), new File(screenshotsDir, fileName));
		TestError error = new TestError(logs, file, description);
		TEST_ERRORS.put(description.getDisplayName(), error);
		if (doCloseDriver == CloseDriver.ALWAYS) {
			closeDriver();
		}
		return logs;
	}
}