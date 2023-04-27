package com.softigent.selenium.ui.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.testng.annotations.Test;

import com.softigent.selenium.core.test.Description;

public class ExtendedRunner extends BlockJUnit4ClassRunner {
	
	public static Class<?> RunnerClass;
	
	public static boolean useDisplayNameMethod = true;
	
	protected RunnerBuilder runner;

    public ExtendedRunner(Class<?> klass, RunnerBuilder runner) throws InitializationError {
        super(klass);
        this.runner = runner;
        ExtendedRunner.RunnerClass = klass;
    }

    @Override
    protected org.junit.runner.Description describeChild(FrameworkMethod method) {
    	if (useDisplayNameMethod) {
	    	Annotation[] annotations = method.getAnnotations();
			if (annotations != null) {
				for (Annotation annotation : annotations) {
			    	String description = null;
					if (Description.class.getName().equals(annotation.annotationType().getName())) {
						description = ((Description) annotation).value();
					} else if (Test.class.getName().equals(annotation.annotationType().getName())) {
						description = ((Test) annotation).description();
					}
					if (description != null && description.isEmpty()) {
						return org.junit.runner.Description.createTestDescription(getTestClass().getJavaClass(), description, method.getAnnotations());
					}
				}
			}
    	}
        return super.describeChild(method);
    }
    
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
    	List<FrameworkMethod> copy = new ArrayList<FrameworkMethod>(super.computeTestMethods());
    	Collections.sort(copy, new Comparator<FrameworkMethod>() {
            public int compare(FrameworkMethod m1, FrameworkMethod m2) {
                return extractInt(m1.getName()) - extractInt(m2.getName());
            }

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });
        return copy;
    }
}