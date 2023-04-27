package com.softigent.selenium.core.test;

import org.junit.Test;

import com.softigent.selenium.ui.test.BasedTestUI;
import com.softigent.selenium.ui.test.Config;

public class TestUI extends BasedTestUI {
	
	private final String BASE_URL = "https://www.google.com/";

	public TestUI() {
		super(new Config().initDefaultProperties()
				// Set the Firefox in headless mode
				//.setProperty("driver", Connector.FIREFOX_DRIVER)
				//.setProperty("headless", "true")
		);
	}

	@Test
	public void Test1() throws Exception {
		connector.getDriver().get(getURL(BASE_URL + "/imghp"));
		waitPageLoad(BASE_URL + ".*");
		body.wait(1);
		config.createSnapshot();
		body.wait(1);
	}
}
