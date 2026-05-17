package net.balmir.injector;


import net.balmir.core.BeanDefinition;
import net.balmir.core.BeanFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstructorInjector {
    private BeanFactory beanFactory;

    public ConstructorInjector(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public Object createBean(BeanDefinition definition) throws Exception {
        Class<?> clazz = Class.forName(definition.getClassName());
        List<Object> args = new ArrayList<>();
        List<Class<?>> argTypes = new ArrayList<>();

        for (Map.Entry<String, String> entry : definition.getConstructorArgs().entrySet()) {
            String value = entry.getValue();
            Object argValue;
            Class<?> argType;

            if (value.startsWith("#{") && value.endsWith("}")) {
                String refBeanName = value.substring(2, value.length() - 1);
                argValue = beanFactory.getBean(refBeanName);
                argType = argValue.getClass();
            } else {
                argValue = value;
                argType = String.class;
            }

            args.add(argValue);
            argTypes.add(argType);
        }

        Constructor<?> constructor = clazz.getConstructor(argTypes.toArray(new Class<?>[0]));
        return constructor.newInstance(args.toArray());
    }
}
