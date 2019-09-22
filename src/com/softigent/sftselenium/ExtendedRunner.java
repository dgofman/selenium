package com.softigent.sftselenium;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class ExtendedRunner extends BlockJUnit4ClassRunner {
	
	protected RunnerBuilder runner;
	protected boolean canUseSuiteMethod;

    public ExtendedRunner(Class<?> klass, RunnerBuilder runner) throws InitializationError {
        super(klass);
        this.runner = runner;
        
        if (runner instanceof AllDefaultPossibilitiesBuilder) {
        	try {
		        Field f = runner.getClass().getDeclaredField("canUseSuiteMethod");
		        f.setAccessible(true);
		        this.canUseSuiteMethod = (Boolean) f.get(runner);
        	} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}
        }
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
    	if (canUseSuiteMethod) {
	    	Class<?> clazz = getTestClass().getJavaClass();
	    	Annotation annotation = null;
	    	try {
				Method thisMethod = clazz.getMethod(method.getName());
				annotation = thisMethod.getAnnotation(DisplayName.class);
			} catch (NoSuchMethodException | SecurityException e) {}
	    	if (annotation == null) {
	    		annotation = method.getAnnotation(DisplayName.class);
	    	}
	        if (annotation != null) {
	        	return Description.createTestDescription(clazz, ((DisplayName)annotation).value(), method.getAnnotations());
	
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