package net.balmir.core;

import net.balmir.parser.AnnotationBeanParser;
import net.balmir.parser.XmlBeanParser;

public class ApplicationContext {
    private BeanFactory beanFactory;

    public ApplicationContext(String configLocation) {
        this.beanFactory = new BeanFactory();
        if (configLocation.endsWith(".xml")) {
            XmlBeanParser xmlParser = new XmlBeanParser(beanFactory);
            xmlParser.loadBeans(configLocation);
        }
    }

    public ApplicationContext(Class<?> configClass) {
        this.beanFactory = new BeanFactory();
        AnnotationBeanParser annotationParser = new AnnotationBeanParser(beanFactory);
        annotationParser.scanAndRegister(configClass.getPackage().getName());
    }

    public ApplicationContext(String basePackage, boolean scan) {
        this.beanFactory = new BeanFactory();
        AnnotationBeanParser annotationParser = new AnnotationBeanParser(beanFactory);
        annotationParser.scanAndRegister(basePackage);
    }

    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        String beanName = requiredType.getSimpleName();
        String lowerCaseName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);

        try {
            return (T) beanFactory.getBean(lowerCaseName);
        } catch (RuntimeException e) {
            try {
                return (T) beanFactory.getBean(beanName);
            } catch (RuntimeException e2) {
                for (String name : beanFactory.getBeanDefinitions().keySet()) {
                    Object bean = beanFactory.getBean(name);
                    if (requiredType.isInstance(bean)) {
                        return (T) bean;
                    }
                }
                throw new RuntimeException("Bean non trouvé pour le type: " + requiredType.getName());
            }
        }
    }
}