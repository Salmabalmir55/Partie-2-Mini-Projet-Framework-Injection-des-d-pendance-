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
            System.out.println("Scan du package: " + basePackage);

            List<Class<?>> componentClasses = scanComponents(basePackage);

            System.out.println("Classes trouvées: " + componentClasses.size());

            for (Class<?> clazz : componentClasses) {
                System.out.println("  - " + clazz.getName());
            }

            for (Class<?> clazz : componentClasses) {
                registerBean(clazz);
            }

            for (Class<?> clazz : componentClasses) {
                injectDependencies(clazz);
            }

        } catch (Exception e) {
            System.err.println("Erreur scan: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur scan annotations", e);
        }
    }

    private List<Class<?>> scanComponents(String basePackage) throws ClassNotFoundException {
        List<Class<?>> components = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');

        System.out.println("Package path: " + packagePath);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(packagePath);

        System.out.println("Resource URL: " + resource);

        if (resource == null) {
            System.err.println("Resource introuvable pour: " + packagePath);
            return components;
        }

        File directory = new File(resource.getFile());

        if (!directory.exists()) {
            System.err.println("Directory n'existe pas: " + directory.getAbsolutePath());
            return components;
        }

        System.out.println("Scan du dossier: " + directory.getAbsolutePath());

        for (File file : directory.listFiles()) {
            System.out.println("  Fichier trouvé: " + file.getName());

            if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().replace(".class", "");
                System.out.println("    Classe: " + className);

                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Component.class)) {
                    System.out.println("      -> @Component trouvé !");
                    components.add(clazz);
                }
            } else if (file.isDirectory()) {
                System.out.println("  Sous-dossier: " + file.getName());
                List<Class<?>> subComponents = scanComponents(basePackage + "." + file.getName());
                components.addAll(subComponents);
            }
        }

        return components;
    }

    private void registerBean(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        String beanName = component.value().isEmpty() ?
                clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1) :
                component.value();

        System.out.println("Enregistrement bean: " + beanName + " -> " + clazz.getName());

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
                System.out.println("Injection dans " + beanName + "." + field.getName());

                field.setAccessible(true);
                Object dependency;

                if (field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    System.out.println("  Qualifier: " + qualifier.value());
                    dependency = beanFactory.getBean(qualifier.value());
                } else {
                    dependency = beanFactory.getBean(field.getType().getSimpleName());
                }

                field.set(bean, dependency);
                System.out.println("  Injection OK");
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