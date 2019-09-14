package com.softigent.sftselenium;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import com.google.common.reflect.ClassPath;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

@RunWith(PackageSuite.SuiteTests.class)
public abstract class PackageSuite {
	
	public static TestSuite suite() {
		final TestSuite suite = new TestSuite();
		final String pkgName = SuiteTests.suiteClass.getPackage().getName();
		final Map<Class<?>, Boolean> ignoreClasses = new HashMap<>();
		ignoreClasses.put(SuiteTests.suiteClass, true);
		try {
			Field ignoreList = SuiteTests.suiteClass.getDeclaredField("IGNORE_LIST");
			Class<?>[] klasses = (Class<?>[]) ignoreList.get(null);
			for (Class<?> klass : klasses) {
				ignoreClasses.put(klass, true);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		}
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
				  if (info.getName().startsWith(pkgName)) {
				    final Class<?> clazz = info.load();
				    if (!ignoreClasses.containsKey(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
				    	suite.addTest(new JUnit4TestAdapter(clazz));
				    }
				  }
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return suite;
	}
	
	public static class SuiteTests extends AllTests {
		
		static Class<?> suiteClass;
		
	    public SuiteTests(Class<?> klass) throws Throwable {
	        super(suiteClass = klass);
	    }
	}
}