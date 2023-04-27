package com.softigent.selenium.core.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class TestHttpHelperTest extends TestHttpHelper {
	
	@Override
	public Map<String, String> getDefaultHeader() {
		Map<String, String> header = super.getDefaultHeader();
		header.put("X-AUTH-APIKEY", "ABC123");
		return header;
	}
	
	@Test
	public void testGetDefaultHeader() {
		Map<String, String> header = getDefaultHeader();
		assertEquals(header.get("X-AUTH-APIKEY"), "ABC123");
	}
	
	@Test
	public void testnode() {
		JNode json = node("child1", "val1", "child2", "val2");
		assertEquals(json.toJson(), "{\n"
				+ "  \"child1\": \"val1\",\n"
				+ "  \"child2\": \"val2\"\n"
				+ "}");
		json.add(node("child3", "val3").add("child4", node("child5", "val5")), "child6", "val6");
		assertEquals(json.toJson(), "{\n"
				+ "  \"child1\": \"val1\",\n"
				+ "  \"child2\": \"val2\",\n"
				+ "  {\n"
				+ "    \"child3\": \"val3\",\n"
				+ "    \"child4\": \n"
				+ "    {\n"
				+ "      \"child5\": \"val5\"\n"
				+ "    }\n"
				+ "  },\n"
				+ "  \"child6\": \"val6\"\n"
				+ "}");
	}
	
	@Test
	public void testCreateJson() {
		String json = createJson(
				 "key1", "val1", 
				 "key2", true, 
				 "key3", new String[] {"ENUM1", "ENUM2", "ENUM3" },
				 "key4", node("child1", "val1", "child2", "val2"),
				 "key5", new JNode[] {node("child1", "val1"), node("child2", "val2")});
		assertEquals(json, "{\n"
				+ "  \"key1\": \"val1\",\n"
				+ "  \"key2\": true,\n"
				+ "  \"key3\": [\"ENUM1\",\"ENUM2\",\"ENUM3\"],\n"
				+ "  \"key4\": \n"
				+ "  {\n"
				+ "    \"child1\": \"val1\",\n"
				+ "    \"child2\": \"val2\"\n"
				+ "  },\n"
				+ "  \"key5\": [\n"
				+ "  {\n"
				+ "    \"child1\": \"val1\"\n"
				+ "  },  {\n"
				+ "    \"child2\": \"val2\"\n"
				+ "  }]\n"
				+ "}");
	}
	
	@Test
	public void testCreateJsonArray() {
		String json = createJsonArray(node("child1", "val1"), node("child2", "val2"));
		assertEquals(json, "[\n"
				+ "{\n"
				+ "  \"child1\": \"val1\"\n"
				+ "},\n"
				+ "{\n"
				+ "  \"child2\": \"val2\"\n"
				+ "}]");
		json = createJsonArray(100, 1.0, true, "Hello World!", new String[] {"Hello", "World"});
		assertEquals(json, "[100,1.0,true,\"Hello World!\",[\"Hello\",\"World\"]]");
	}
	
	@Test
	public void testSystemProp() {
		setProp("MYKEY", "myvalue");
		assertEquals(getProp("MYKEY"), "myvalue");
		delProp("MYKEY");
		assertEquals(getProp("MYKEY"), null);
	}
}
