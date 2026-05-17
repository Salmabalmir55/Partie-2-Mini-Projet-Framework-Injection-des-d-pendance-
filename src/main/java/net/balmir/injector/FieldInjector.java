package net.balmir.injector;


import net.balmir.core.BeanDefinition;
import net.balmir.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.Map;

public class FieldInjector {
    private BeanFactory beanFactory;

    public FieldInjector(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void inject(Object bean, BeanDefinition definition) throws Exception {
        for (Map.Entry<String, Object> entry : definition.getProperties().entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue instanceof String &&
                    ((String) fieldValue).startsWith("#{") &&
                    ((String) fieldValue).endsWith("}")) {
                String refBeanName = ((String) fieldValue).substring(2, ((String) fieldValue).length() - 1);
                fieldValue = beanFactory.getBean(refBeanName);
            }

            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(bean, fieldValue);
        }
    }
}