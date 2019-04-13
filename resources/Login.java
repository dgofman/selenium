package %PACKAGE%;

import org.junit.Before;
import org.junit.Test;

import com.softigent.sftselenium.Container;
import com.softigent.sftselenium.Element;

public class Login extends %PROJECT%BaseTest {
	
	private final String url = "https://github.com/dgofman/selenium";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		openPage(url);
		waitPageLoad(url);
	}

	@Test
	public void Test1() throws Exception {
		Container body = createContainer(".logged-out");
		Element signIn = body.find("header a.mr-3");
		signIn.click();
		waitPageLoadByTitle("Sign in to GitHub · GitHub");
		Element username = body.find("#login_field");
		username.setText("username");
		Element password = body.find("#password");
		password.setText("password");
		Element submit = body.find("[name=commit]");
		submit.click();
		body.assertText(".flash-error", "Incorrect username or password.", true); //multiple lines in Edge
	}
}