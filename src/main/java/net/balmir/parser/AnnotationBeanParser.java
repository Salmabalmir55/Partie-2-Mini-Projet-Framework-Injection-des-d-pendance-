package net.balmir.parser;

import net.balmir.annotations.Autowired;
import net.balmir.annotations.Component;
import net.balmir.annotations.Qualifier;
import net.balmir.annotations.Scope;
import net.balmir.core.BeanDefinition;
import net.balmir.core.BeanFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnnotationBeanParser {
    private BeanFactory beanFactory;

    public AnnotationBeanParser(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void scanAndRegister(String basePackage) {
        try {
            List<Class<?>> componentClasses = scanComponents(basePackage);

            for (Class<?> clazz : componentClasses) {
                registerBean(clazz);
            }

            for (Class<?> clazz : componentClasses) {
                injectDependencies(clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur scan annotations", e);
        }
    }

    private List<Class<?>> scanComponents(String basePackage) throws ClassNotFoundException {
        List<Class<?>> components = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(packagePath);

        if (resource != null) {
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                for (File file : directory.listFiles()) {
                    if (file.getName().endsWith(".class")) {
                        String className = basePackage + "." + file.getName().replace(".class", "");
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            components.add(clazz);
                        }
                    }
                }
            }
        }
        return components;
    }

    private void registerBean(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        String beanName = component.value().isEmpty() ?
                clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1) :
                component.value();

        BeanDefinition definition = new BeanDefinition(beanName, clazz.getName());

        if (clazz.isAnnotationPresent(Scope.class)) {
            Scope scope = clazz.getAnnotation(Scope.class);
            definition.setScope(scope.value());
        }

        beanFactory.registerBeanDefinition(beanName, definition);
    }

    private void injectDependencies(Class<?> clazz) throws Exception {
        String beanName = getBeanName(clazz);
        Object bean = beanFactory.getBean(beanName);

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                Object dependency;

                if (field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    dependency = beanFactory.getBean(qualifier.value());
                } else {
                    dependency = beanFactory.getBean(field.getType().getSimpleName());
                }

                field.set(bean, dependency);
            }
        }
    }

    private String getBeanName(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        return clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
    }
}
