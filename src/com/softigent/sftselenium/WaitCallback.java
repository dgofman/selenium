package com.softigent.sftselenium;

import org.openqa.selenium.WebElement;

/**
	WaitCallback waitCallBack = new WaitCallback() {
		public boolean isTrue() {
			value = "Hello World!";
			return true;
		}
	};
	body.waitWhenTrue(waitCallBack);
	System.out.println((String)waitCallBack.getValue());
*/

public abstract class WaitCallback implements IWaitCallback {
	
	protected Object value;
	
	public boolean isTrue(WebElement element) {
		value = element;
		return isTrue();
	}
	
	public boolean isTrue() {
		return value != null;
	}
	
	public Object getValue() {
		return value;
	}
}