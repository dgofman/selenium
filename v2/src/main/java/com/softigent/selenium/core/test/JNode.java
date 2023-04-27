package com.softigent.selenium.core.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JNode is an unordered collection of name/value pairs.
 * Encapsulates <code>org.json.JSONObject</code> methods
 *
 * @author  dgofman
 * @since   1.0
 */
public class JNode {
	
	public static String NODE_IDENT = "  ";
	protected static Logger logger;

	static {
		logger = Logger.initRoot();
	}
	
	private final List<Object> keyvals;
	
	public JNode(Object ...keyval) {
		keyvals = new ArrayList<>();
		add(keyval);
	}
	
	public JNode add(Object ...keyval) {
		keyvals.addAll(Arrays.asList(keyval));
		return this;
	}
	
	public String toJson() {
		return toJson("");
	}
	
	public String toJson(String indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keyvals.size(); i++) {
			Object o = keyvals.get(i);
			if (o instanceof JNode) {
				sb.append(((JNode) o).toJson(indent + NODE_IDENT));
			} else if (keyvals.size() > i + 1) {
				sb.append(indent + NODE_IDENT);
				sb.append("\"" + o + "\": " + TestHttpClient.getVal(keyvals.get(++i), indent + NODE_IDENT)); 
			} else {
				break;
			}
			sb.append(i + 1 < keyvals.size() ? ",\n" : "\n");
		}
		return indent + "{\n" + sb.toString() + indent + "}";
	}
	
	@Override
	public String toString() {
		String json = toJson();
		logger.info("JSON: \n" + json);
		return json;
	}
}