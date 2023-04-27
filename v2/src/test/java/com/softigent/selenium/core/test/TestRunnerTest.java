package com.softigent.selenium.core.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;

import java.io.StringWriter;

public class TestRunnerTest
{
	static final String ENV_NAME = "RunException";
	
    @org.junit.Test
    public void testRunClass() throws Exception
    {
    	StringWriter sw = new StringWriter();
    	System.setProperty(ENV_NAME, "true");
    	TestNgRunner runner = new TestNgRunner();
    	runner.run("mytestreport", "selenium-CORE", "CoreCycleName", FakeTestClass.class);
    	runner.save(sw);
    	String out = String.join(",\n", sw.toString());
    	JObject json = new JArray(out).getJObject(0);
    	assertEquals(json.toString("keyword"), "Suite");
    	assertEquals(json.toString("projectKey"), "selenium-CORE");
    	assertEquals(json.toString("name"), "CoreCycleName");
    	assertEquals(json.toString("uri"), "com.softigent.selenium.core.test.CucumberReport");
    	
    	JArray elements = json.getJArray("elements");
    	json = elements.getJObject(0);
    	assertTrue(json.toString("start_timestamp").split("-").length == 3); //"2023-02-01T19:55:42.616Z"
    	assertEquals(json.toString("keyword"), "Class");
    	assertEquals(json.toString("type"), "test");
    	assertEquals(json.toString("name"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass");
    	
    	JArray steps = json.getJArray("steps");
    	json = steps.getJObject(0);
    	assertEquals(json.toString("keyword"), "Method");
    	assertEquals(json.toString("name"), "test1 (Test One description) - seleniumAGL-T1857");
    	assertEquals(json.getJObject("match").toString("location"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass::test1");
    	assertEquals(json.getJObject("result").toString("status"), "failed");
    	assertEquals(json.getJObject("result").toString("error_message"), "java lang AssertionError: My Test Exception");
    	assertEquals(elements.getJObject(0).getJArray("tags").getJObject(0).toString("name"), "@TestCaseKey=seleniumAGL-T1857");

    	json = elements.getJObject(1);
    	assertTrue(json.toString("start_timestamp").split("-").length == 3); //"2023-02-01T19:55:42.616Z"
    	assertEquals(json.toString("keyword"), "Class");
    	assertEquals(json.toString("type"), "test");
    	assertEquals(json.toString("name"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass");
    	
    	steps = json.getJArray("steps");
    	json = steps.getJObject(0);
    	assertEquals(json.toString("keyword"), "Method");
    	assertEquals(json.toString("name"), "test2 (Test Two description) - seleniumAGL-T1858");
    	assertEquals(json.getJObject("match").toString("location"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass::test2");
    	assertEquals(json.getJObject("result").toString("status"), "passed");
    	assertEquals(elements.getJObject(1).getJArray("tags").getJObject(0).toString("name"), "@TestCaseKey=seleniumAGL-T1858");
    	System.setProperty(ENV_NAME, "false");
    }
    
    @org.junit.Test
    public void testRunClassnfo() throws Exception
    {
    	StringWriter sw = new StringWriter();
    	TestNgRunner runner = new TestNgRunner();
    	assertEquals(System.getProperty("testActivityJsonFilename"), null);
    	assertEquals(System.getProperty("activityId"), null);
    	assertEquals(System.getProperty("psimProcessTimeToWait"), null);
    	assertEquals(System.getProperty("consumerTimeToWait"), null);

    	runner.addSuite("src/main/resources/sample_functional-suite.xml");
    	runner.run("mytestreport", "CoreCycleName");

    	runner.save(sw);
    	String out = String.join(",\n", sw.toString());
    	JObject json = new JArray(out).getJObject(0);
    	assertEquals(json.toString("keyword"), "Suite");
    	assertEquals(json.toString("projectKey"), "");
    	assertEquals(json.toString("name"), "CoreCycleName");
    	assertEquals(json.toString("uri"), "com.softigent.selenium.core.test.CucumberReport");

    	JArray elements = json.getJArray("elements");
    	json = elements.getJObject(0);
    	assertTrue(json.toString("start_timestamp").split("-").length == 3); //"2023-02-01T19:55:42.616Z"
    	assertEquals(json.toString("keyword"), "Class");
    	assertEquals(json.toString("type"), "test");
    	assertEquals(json.toString("name"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass");
    	
    	JArray steps = json.getJArray("steps");
    	json = steps.getJObject(0);
    	assertEquals(json.toString("keyword"), "Method");
    	assertEquals(json.toString("name"), "test1 (Test One description) - seleniumAGL-T1857");
    	assertEquals(json.getJObject("match").toString("location"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass::test1");
    	assertEquals(json.getJObject("result").toString("status"), "passed");
    	assertEquals(elements.getJObject(0).getJArray("tags").getJObject(0).toString("name"), "@TestCaseKey=seleniumAGL-T1857");

    	
    	json = elements.getJObject(1);
    	assertTrue(json.toString("start_timestamp").split("-").length == 3); //"2023-02-01T19:55:42.616Z"
    	assertEquals(json.toString("keyword"), "Class");
    	assertEquals(json.toString("type"), "test");
    	assertEquals(json.toString("name"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass");
    	
    	steps = json.getJArray("steps");
    	json = steps.getJObject(0);
    	assertEquals(json.toString("keyword"), "Method");
    	assertEquals(json.toString("name"), "test2 (Test Two description) - seleniumAGL-T1858");
    	assertEquals(json.getJObject("match").toString("location"), "com.softigent.selenium.core.test.TestRunnerTest$FakeTestClass::test2");
    	assertEquals(json.getJObject("result").toString("status"), "passed");
    	assertEquals(elements.getJObject(1).getJArray("tags").getJObject(0).toString("name"), "@TestCaseKey=seleniumAGL-T1858");
    }
    
    @org.junit.Test
    public void testPublishReport() throws Exception
    {
    	//Test BaseRunner thread termination
		Thread thread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						System.out.print(".");
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.setName("TestRunnerTest::testPublishReport");
		thread.start();

    	TestNgRunner runner = new TestNgRunner();
    	runner.run("mytestreport", "seleniumAGL", "selenium Core Test", FakeTestClass.class);
    	runner.save();
    }
    

    static public class FakeTestClass {
    	
    	@org.testng.annotations.Test(priority = 1, description="seleniumAGL-T1857::Test One description")
    	public void test1() {
    		if (System.getProperty(ENV_NAME) == "true") {
    			fail("My Test Exception");
    		}
    	}
    	
    	@org.testng.annotations.Test
    	@Description("seleniumAGL-T1858::Test Two description")
    	public void test2() {
    		assertTrue(true);
    	}
    }
}

