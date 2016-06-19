package com.softigent.sftselenium;

import org.openqa.selenium.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumUtils {

	public static boolean acceptNextAlert = true;

	public static WebDriver getDriver(String name, Config config) {
		WebDriver driver = null;
		boolean isFullScreen = "true".equals(config.getProperty("open_fullscreen"));
		if (name.equals("Firefox")) {
			FirefoxProfile ffProfile = new FirefoxProfile();
			ffProfile.setPreference("layout.css.devPixelsPerPx", "1.0");
			driver = new FirefoxDriver(ffProfile);
		} else if (name.equals("Chrome")) {
			ChromeOptions options = new ChromeOptions();
			if (isFullScreen) {
				options.addArguments("--start-maximized");
			}
			driver = new ChromeDriver(options);
		} else if (name.equals("Safari")) {
			driver = new SafariDriver();
		} else if (name.equals("IE")) {
			DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
			dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			driver = new InternetExplorerDriver(dc);
		}

		if (isFullScreen) {
			if (!name.equals("Chrome")) {
				driver.manage().window().maximize();
			}
		} else if (config.getProperty("window_dimension") != null) {
			String[] wh = config.getProperty("window_dimension").split("x");
			if (wh.length == 2) {
				Dimension d = new Dimension(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
				driver.manage().window().setSize(d);
			}
		}
		return driver;
	}

	public static void wait(WebDriver driver, long timeoutSec, Pattern urlPath) {
		String currentUrl = driver.getCurrentUrl();
		new WebDriverWait(driver, timeoutSec).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				if (urlPath != null) {
					return urlPath.matcher(d.getCurrentUrl()).matches();
				} else {
					return !d.getCurrentUrl().equals(currentUrl);
				}
			}
		});
	}

	public static WebElement waitAndFindElement(WebDriver driver, By locator, long timeoutSec) {
		return waitAndFindElement(driver, locator, timeoutSec, true);
	}

	public static WebElement waitAndFindElement(WebDriver driver, By locator, long timeoutSec, boolean isVisible) {
		WebDriverWait wait = new WebDriverWait(driver, timeoutSec);
		try {
			if (isVisible) {
				return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			} else {
				return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			}
		} catch (NoSuchElementException nsee) {
			throw new NoSuchElementException("NoSuchElementException: Locator not found:" + locator);
		} catch (TimeoutException toe) {
			throw new TimeoutException("TimeoutException: Locator not visible:" + locator);
		}
	}

	public static File screenshot(WebDriver driver, String fileName) {
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scrFile;
	}

	public static void sleep(float seconds) {
		try {
			if (seconds != 0) {
				Thread.sleep((int) seconds * 1000);
			}
		} catch (InterruptedException e) {
		}
	}

	public static boolean isElementPresent(WebDriver driver, By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public static boolean isAlertPresent(WebDriver driver) {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}

	public static String closeAlertAndGetItsText(WebDriver driver) {
		try {
			Alert alert = driver.switchTo().alert();
			String alertText = alert.getText();
			if (acceptNextAlert) {
				alert.accept();
			} else {
				alert.dismiss();
			}
			return alertText;
		} finally {
			acceptNextAlert = true;
		}
	}

	public static void fileBrowseDialog(WebDriver driver, String path) {
		StringSelection ss = new StringSelection(path);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		try {
			Thread.sleep(500);
			driver.switchTo().activeElement();
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			Thread.sleep(500);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
