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

    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    public <T> T getBean(Class<T> requiredType) {
        return (T) beanFactory.getBean(requiredType.getSimpleName());
    }
}
