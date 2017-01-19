package com.softigent.sftselenium;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

public class SeleniumUtils {

	public static boolean acceptNextAlert = true;
	
	private static String browserName;

	public static WebDriver getDriver(String name, Config config) {
		WebDriver driver = null;
		browserName = name;
		boolean isPrivate = "true".equals(config.getProperty("open_as_private"));
		boolean isFullScreen = "true".equals(config.getProperty("open_fullscreen"));
		if (name.equals("Firefox")) {
			FirefoxProfile ffProfile = new FirefoxProfile();
			ffProfile.setPreference("layout.css.devPixelsPerPx", "1.0");
			if (isPrivate) {
				ffProfile.setPreference("browser.privatebrowsing.autostart", true);
			}
			driver = new FirefoxDriver(ffProfile);
		} else if (name.equals("Chrome")) {
			ChromeOptions options = new ChromeOptions();
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			if (isFullScreen) {
				options.addArguments("--start-maximized");
			}
			if (isPrivate) {
				capabilities.setCapability("chrome.switches", Arrays.asList("--incognito"));
			}
			options.addArguments("--disable-extensions");

			LoggingPreferences logPrefs = new LoggingPreferences();
			logPrefs.enable(LogType.BROWSER, Level.ALL);
			capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			driver = new ChromeDriver(capabilities);
		} else if (name.equals("Safari")) {
			driver = new SafariDriver();
		} else if (name.equals("IE")) {
			DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
			dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			dc.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			dc.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
			
			if (isPrivate) {
				dc.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true); 
				dc.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
			}
			driver = new InternetExplorerDriver(dc);
		}

		if (isFullScreen) {
			if (!name.equals("Chrome")) {
				driver.manage().window().maximize();
			}
		} else if (config.getProperty("window_dimension") != null) {
			if (name.equals("IE")) {
				config.getWindowOffset().move(10, 8); //IE browser border
			} else if (name.equals("Chrome")) {
				config.getWindowOffset().move(8, 8); //Chrome browser border
			} else {
				config.getWindowOffset().move(10, 5); //FireFox browser border
			}
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
		try {
			if (driver.getWindowHandles().size() == 1) {
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(scrFile, Config.getFile(fileName));
				return scrFile;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			ImageIO.write(image, "png", Config.getFile(fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	public static void fileBrowseDialog(Element element, WebDriver driver, String path) {
		if (Platform.isWindows()) {
			StringSelection ss = new StringSelection(path);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			try {
				Thread.sleep(200);
				driver.switchTo().activeElement();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			Robot robot = Element.getRobot(200);
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
	
	public static void resetWindow() {
		if (SeleniumUtils.getBrowserName().equals("Chrome")) {
			//Close Chrome download bar
			Element.keyPress(new int[] {KeyEvent.VK_CONTROL, KeyEvent.VK_J, KeyEvent.VK_W}, Element.getRobot(500));
		}
	}
	
	public static HttpResponse getHttpResponse(String url) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		return client.execute(request);
	}
	
	private interface User32 extends StdCallLibrary {
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
		boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);
		int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);
		HWND SetFocus(HWND hWnd);
	}
	
	private interface WindowsX11 {
	    void recurse(X11 x11, Display display, Window root);
	}
	
	public static List<?> getWin32Handle(String title) {
		if (Platform.isWindows()) {
			final User32 user32 = User32.INSTANCE;
			final List<HWND> hWnds = new ArrayList<HWND>();
			user32.EnumWindows(new WNDENUMPROC() {
				@Override
				public boolean callback(HWND hWnd, Pointer arg1) {
					byte[] windowText = new byte[512];
					user32.GetWindowTextA(hWnd, windowText, 512);
					String wText = Native.toString(windowText).trim();
					if (!wText.isEmpty() && Element.regExpString(wText, title)) {
						hWnds.add(hWnd);
					}
					return true;
				}
			}, null);
			return hWnds;
		} else if (Platform.isLinux()) {
			final X11 x11 = X11.INSTANCE;
			final Display display = x11.XOpenDisplay(null);
			final List<Window> hWnds = new ArrayList<Window>();

	        Window root = x11.XDefaultRootWindow(display);
	        new WindowsX11() {
	            @Override
	            public void recurse(X11 x11, Display display, Window root) {
	        	    X11.WindowByReference windowRef = new X11.WindowByReference();
	        	    X11.WindowByReference parentRef = new X11.WindowByReference();
	        	    PointerByReference childrenRef = new PointerByReference();
	        	    IntByReference childCountRef = new IntByReference();

	        	    x11.XQueryTree(display, root, windowRef, parentRef, childrenRef, childCountRef);
	        	    if (childrenRef.getValue() == null) {
	        	        return;
	        	    }

	        	    long[] ids;

	        	    if (Native.LONG_SIZE == Long.BYTES) {
	        	        ids = childrenRef.getValue().getLongArray(0, childCountRef.getValue());
	        	    } else if (Native.LONG_SIZE == Integer.BYTES) {
	        	        int[] intIds = childrenRef.getValue().getIntArray(0, childCountRef.getValue());
	        	        ids = new long[intIds.length];
	        	        for (int i = 0; i < intIds.length; i++) {
	        	            ids[i] = intIds[i];
	        	        }
	        	    } else {
	        	        return;
	        	    }
	        	    for (long id : ids) {
	        	        if (id != 0) {
	        		        Window window = new Window(id);
	        		        X11.XTextProperty name = new X11.XTextProperty();
	        		        x11.XGetWMName(display, window, name);
	        		        if (name.value != null) {
		        		        String wText = name.value.trim();
		        		        if (!wText.isEmpty() && Element.regExpString(wText, title)) {
		    						hWnds.add(window);
		    					}
	        		        }
	        		        x11.XFree(name.getPointer());
	        		        recurse(x11, display, window);
	        	        }
	        	    }
	            }
	        }. recurse(x11, display, root);
	        x11.XCloseDisplay(display);
	        
	        return hWnds;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	
	public static void setWindowFocus(Object win) {
		if (Platform.isWindows()) {
			User32.INSTANCE.SetFocus((HWND)win);
		} else {
			final X11 x11 = X11.INSTANCE;
			x11.XSelectInput(x11.XOpenDisplay(null), (Window)win, new NativeLong(X11.ExposureMask |
																				X11.VisibilityChangeMask |
																				X11.StructureNotifyMask |
																				X11.FocusChangeMask |
																				//X11.PointerMotionMask |
																				X11.EnterWindowMask |
																				X11.LeaveWindowMask));
		}
	}

	public static String getBrowserName() {
		return browserName;
	}
	
	public static Dimension getScreenSize() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return new Dimension(
			(int) toolkit.getScreenSize().getWidth(), 
			(int) toolkit.getScreenSize().getHeight());
	}
}