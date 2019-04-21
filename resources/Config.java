package %PACKAGE%;

import com.softigent.sftselenium.Config;

public class %PROJECT%Config extends Config {

	private static final long serialVersionUID = 1L;

	private static %PROJECT%Config instance;

	private %PROJECT%Config(String propertyFile) {
		super(propertyFile);
	}
	
	public static %PROJECT%Config getInstance() {
		if (instance == null) {
			initLogs();
			instance = new %PROJECT%Config("src/config.properties");
			instance.setDefaultProperties();
		}
		return instance;
	}

	public static void initLogs() {
		System.setProperty("test.log", "./logs/tests.log");
		System.setProperty("httplog.log", "./logs/http.log");
		System.setProperty("httperror.log", "./logs/http-errors.log");
	}
	
	@Override
	public String getDriverPath(String driverName, String driverPathKey) {
		return super.getDriverPath(driverName, os() + '_' + driverPathKey);
	}
}
