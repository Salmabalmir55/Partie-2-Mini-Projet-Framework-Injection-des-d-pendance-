package net.balmir.core;

import net.balmir.injector.ConstructorInjector;
import net.balmir.injector.FieldInjector;
import net.balmir.injector.SetterInjector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    private Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    private Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();

    public void registerBeanDefinition(String name, BeanDefinition definition) {
        beanDefinitions.put(name, definition);
    }

    public Object getBean(String name) {
        BeanDefinition definition = beanDefinitions.get(name);
        if (definition == null) {
            throw new RuntimeException("Bean non trouvé: " + name);
        }

        if ("singleton".equals(definition.getScope())) {
            if (singletonBeans.containsKey(name)) {
                return singletonBeans.get(name);
            }
            Object bean = createBean(definition);
            singletonBeans.put(name, bean);
            return bean;
        } else {
            return createBean(definition);
        }
    }

    private Object createBean(BeanDefinition definition) {
        try {
            Object bean;

            switch (definition.getInjectionType()) {
                case CONSTRUCTOR:
                    bean = new ConstructorInjector(this).createBean(definition);
                    break;
                case SETTER:
                    Class<?> clazzSetter = Class.forName(definition.getClassName());
                    bean = clazzSetter.getDeclaredConstructor().newInstance();
                    new SetterInjector(this).inject(bean, definition);
                    break;
                case FIELD:
                    Class<?> clazzField = Class.forName(definition.getClassName());
                    bean = clazzField.getDeclaredConstructor().newInstance();
                    new FieldInjector(this).inject(bean, definition);
                    break;
                default:
                    Class<?> clazz = Class.forName(definition.getClassName());
                    bean = clazz.getDeclaredConstructor().newInstance();
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Erreur création bean: " + definition.getClassName(), e);
        }
    }

    public Map<String, BeanDefinition> getBeanDefinitions() {
        return beanDefinitions;
    }
}