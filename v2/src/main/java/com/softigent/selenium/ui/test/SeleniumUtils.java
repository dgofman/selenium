package com.softigent.selenium.ui.test;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sun.jna.Platform;

public class SeleniumUtils {

	public static boolean acceptNextAlert = true;

	private static String XMLHTTP_TRAFFIC_SCRIPT = "var d=window.SeleniumXMLHttpRequest=window.XMLHttpRequest;d.b=[];d.get=function(){var a=d.b.splice(0);d.b=[];return a};window.XMLHttpRequest=function(){this.a=new d};'open abort setRequestHeader send addEventListener removeEventListener getResponseHeader getAllResponseHeaders dispatchEvent overrideMimeType'.split(' ').forEach(function(a){window.XMLHttpRequest.prototype[a]=function(){'open'==a&&(this.open=arguments);'send'==a&&(this.send=arguments);return this.a[a].apply(this.a,arguments)}});" + 
			"'onabort onerror onload onloadstart onloadend onprogress readyState responseText responseType responseXML status statusText upload withCredentials DONE UNSENT HEADERS_RECEIVED LOADING OPENED'.split(' ').forEach(function(a){Object.defineProperty(window.XMLHttpRequest.prototype,a,{get:function(){return this.a[a]},set:function(b){this.a[a]=b}})});" + 
			"Object.defineProperty(window.XMLHttpRequest.prototype,'onreadystatechange',{get:function(){return this.a.onreadystatechange},set:function(a){var b=this;b.a.onreadystatechange=function(){if(4==b.a.readyState){d.b.push([b.a.status,b.open[0],b.open[1],b.send[0],b.a.getAllResponseHeaders(),b.a.responseText],b.a.getAllResponseHeaders())}a.call(b.a)}}});";

	public static void createXMLHTTPTrafficListener(JavascriptExecutor js, Runnable runnable) {
		js.executeScript(XMLHTTP_TRAFFIC_SCRIPT);
		new Thread(runnable).start();
	}

	public static Object prettyJson(Object value) {
		try {
			ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			scriptEngine.put("jsonString", value);
			scriptEngine.eval("result = JSON.stringify(JSON.parse(jsonString), null, 4)");
			return ((String) scriptEngine.get("result")).replaceAll(" {4}", "\t");
		} catch (Exception e) {
			return value;
		}
	}

	public static void wait(WebDriver driver, long timeoutSec, String urlPath, boolean isRegExp) {
		String currentUrl = driver.getCurrentUrl();
		new WebDriverWait(driver, Duration.ofSeconds(timeoutSec)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				String url = d.getCurrentUrl();
				if (urlPath != null) {
					if (isRegExp) {
						return Pattern.compile(urlPath).matcher(url).matches();
					} else {
						return urlPath.equals(url);
					}
				} else {
					return !url.equals(currentUrl);
				}
			}
		});
	}

	public static WebElement waitAndFindElement(WebDriver driver, By locator, long timeoutSec) {
		return waitAndFindElement(driver, locator, timeoutSec, true);
	}

	public static WebElement waitAndFindElement(WebDriver driver, By locator, long timeoutSec, boolean isVisible) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
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

	public static File screenshot(WebDriver driver, File file) {
		try {
			if (driver.getWindowHandles().size() == 1) {
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(scrFile, file);
				return scrFile;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			ImageIO.write(image, "png", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	public static void fileBrowseDialog(Element element, WebDriver driver, String path) {
		StringSelection ss = new StringSelection(path);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		Robot robot = Element.getRobot(200);
		try {
			Thread.sleep(2000);
			driver.switchTo().activeElement();
		} catch (Exception e) {
				System.err.println(e.getMessage());
		}
		if (Platform.isWindows()) {
			Element.keyPress(new int[] {KeyEvent.VK_CONTROL, KeyEvent.VK_V}, robot);
			Element.keyPress(KeyEvent.VK_ENTER, robot);
		} else if (Platform.isLinux()) {
			Element.keyPress(new int[] {KeyEvent.VK_DOWN}, robot);
			Element.keyPress(new int[] {KeyEvent.VK_CONTROL, KeyEvent.VK_V}, robot);
			Element.keyPress(KeyEvent.VK_ENTER, robot);
		} else {
			element.sendKeys("[type=file]", path);
		}
	}
	
	public static void beep() {
		java.awt.Toolkit.getDefaultToolkit().beep();
	}
	
	public static List<String> getVMArguments() {
		return ManagementFactory.getRuntimeMXBean().getInputArguments();
	}
	
	public static String[] getCmdArguments() {
		String command = System.getProperty("sun.java.command");
		Matcher m = Pattern.compile("^(.*?) (.*?)-(.*)").matcher(command);
		while(m.find()) {
			return m.group(2).split(" ");
		}
		return new String[]{};
	}
	
	public static String fillString(int repeatTimes) {
		return fillString(repeatTimes, null);
	}

	public static String fillString(int repeatTimes, String pattern) {
		StringBuffer sf = new StringBuffer();
		Random ran = new Random();
		for (int i = 0; i < repeatTimes; i++) {
			if (pattern == null) {
				sf.append(ran.nextInt(10));
			} else {
				sf.append(pattern);
			}
		}
		return sf.toString();
	}

	//connector.getDriverName()
	public static void resetWindow(String driverName) {
		if ("Chrome".equals(driverName)) {
			//Close Chrome download bar
			Element.keyPress(new int[] {KeyEvent.VK_CONTROL, KeyEvent.VK_J, KeyEvent.VK_W}, Element.getRobot(500));
		}
	}
	
	public static HttpResponse getHttpResponse(String url) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		return client.execute(request);
	}
	

	public static Dimension getScreenSize() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return new Dimension(
			(int) toolkit.getScreenSize().getWidth(), 
			(int) toolkit.getScreenSize().getHeight());
	}
	
	public static void scrollIntoView(Element container, String selector) {
		scrollIntoView(container, container.findElement(selector));
	}

	public static void scrollIntoView(Element container, Element element) {
		scrollIntoView(container, element.getElement());
	}

	public static void scrollIntoView(Element container, WebElement element) {
		container.executeScript("arguments[0].scrollIntoView(true)", element);
	}
		
	public static void scrollBy(Element container, WebElement element, int offset) {
		scrollIntoView(container, element);
		container.executeScript("window.scrollBy(0, " + offset + ")");
	}
}

