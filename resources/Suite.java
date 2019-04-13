package %PACKAGE%.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import %PACKAGE%.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	Login.class
})
public class  %PROJECT%Suite {}