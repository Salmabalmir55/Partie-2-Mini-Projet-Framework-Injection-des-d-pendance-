package net.balmir.injector;


import net.balmir.core.BeanDefinition;
import net.balmir.core.BeanFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class SetterInjector {
    private BeanFactory beanFactory;

    public SetterInjector(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void inject(Object bean, BeanDefinition definition) throws Exception {
        for (Map.Entry<String, Object> entry : definition.getProperties().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            if (propertyValue instanceof String &&
                    ((String) propertyValue).startsWith("#{") &&
                    ((String) propertyValue).endsWith("}")) {
                String refBeanName = ((String) propertyValue).substring(2, ((String) propertyValue).length() - 1);
                propertyValue = beanFactory.getBean(refBeanName);
            }

            String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            Method setter = findSetter(bean.getClass(), setterName, propertyValue.getClass());
            if (setter != null) {
                setter.invoke(bean, propertyValue);
            }
        }
    }

    private Method findSetter(Class<?> clazz, String setterName, Class<?> paramType) {
        try {
            return clazz.getMethod(setterName, paramType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
