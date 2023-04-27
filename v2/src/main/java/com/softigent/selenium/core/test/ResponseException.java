package com.softigent.selenium.core.test;

import java.io.IOException;
import java.io.InputStream;

import com.mashape.unirest.http.HttpResponse;

/**
 * Throw HTTP error with the status and result body 
 *
 * @author  dgofman
 * @since   1.0
 */
public class ResponseException extends Exception {
	private static final long serialVersionUID = 1L;
	public final HttpResponse<InputStream> res;
	public final int statusCode;
	public final String body;
		
	public ResponseException(HttpResponse<InputStream> res, String method, String url) throws IOException {
		this.res = res;
		this.statusCode = res.getStatus();
		this.body = TestHttpClient.responseToString(res);
	}
	
	@Override
	public String toString() {
		return "Status Code: " + statusCode + ", Body: " + body;
	}
}
