package com.softigent.sftselenium;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumUtils {

	public static boolean acceptNextAlert = true;

	public static WebDriver getDriver(String name, String maximize) {
		WebDriver diver = null;
		if (name.equals("Firefox")) {
			diver = new FirefoxDriver();
		} else if (name.equals("chrome")) {
			ChromeOptions options = new ChromeOptions();
			if ("true".equals(maximize)) {
				options.addArguments("--start-maximized");
			}
			diver = new ChromeDriver(options);
		} else if (name.equals("safari")) {
			diver = new SafariDriver();
		} else if (name.equals("ie")) {
			diver = new InternetExplorerDriver();
		}

		if ("true".equals(maximize) && !name.equals("chrome")) {
			diver.manage().window().maximize();
		}
		return diver;
	}

	public static void wait(WebDriver driver, long timeoutSec, Pattern urlPath) {
		String currentUrl = driver.getCurrentUrl();
		new WebDriverWait(driver, timeoutSec).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				if (urlPath != null) {
					return urlPath.matcher(driver.getCurrentUrl()).matches();
				} else {
					return !driver.getCurrentUrl().equals(currentUrl);
				}
			}
		});
	}
	
	public static WebElement waitAndFindElement(WebDriver driver, By locator, long timeoutSec) {
	  WebDriverWait wait = new WebDriverWait(driver, timeoutSec);
	  return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
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

			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			driver.switchTo().activeElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
