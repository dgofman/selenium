package com.softigent.sftselenium;

import org.openqa.selenium.WebElement;

/**
body.waitWhenTrue(new IWaitCallback() {
	public boolean isTrue(WebElement element) {
		return element != null;
	}
});
*/

public interface IWaitCallback {
	public boolean isTrue(WebElement element);
}