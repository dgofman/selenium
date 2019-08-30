package com.softigent.sftselenium;

import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class ExtendedRunner extends BlockJUnit4ClassRunner {

    public ExtendedRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        if (method.getAnnotation(DisplayName.class) != null) {
            return Description.createTestDescription(getTestClass().getJavaClass(), method.getAnnotation(DisplayName.class).value(), method.getAnnotations());
        }
        return super.describeChild(method);
    }
}