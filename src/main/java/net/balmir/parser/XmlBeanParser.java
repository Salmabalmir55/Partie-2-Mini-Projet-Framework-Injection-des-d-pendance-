package net.balmir.parser;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.balmir.core.BeanDefinition;
import net.balmir.core.BeanFactory;

import java.io.File;
import java.util.List;

@XmlRootElement(name = "beans")
class BeansConfig {
    private List<BeanElement> bean;
    @XmlElement(name = "bean")
    public List<BeanElement> getBean() { return bean; }
    public void setBean(List<BeanElement> bean) { this.bean = bean; }
}

class BeanElement {
    private String id;
    private String className;
    private String scope;
    private List<PropertyElement> property;
    private List<ConstructorArgElement> constructorArg;
    @XmlAttribute
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    @XmlAttribute public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    @XmlAttribute public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    @XmlElement(name = "property") public List<PropertyElement> getProperty() { return property; }
    public void setProperty(List<PropertyElement> property) { this.property = property; }
    @XmlElement(name = "constructor-arg") public List<ConstructorArgElement> getConstructorArg() { return constructorArg; }
    public void setConstructorArg(List<ConstructorArgElement> constructorArg) { this.constructorArg = constructorArg; }
}

class PropertyElement {
    private String name;
    private String value;
    private String ref;
    @XmlAttribute public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    @XmlAttribute public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    @XmlAttribute public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
}

class ConstructorArgElement {
    private String index;
    private String value;
    private String ref;
    @XmlAttribute public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }
    @XmlAttribute public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    @XmlAttribute public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
}

public class XmlBeanParser {
    private BeanFactory beanFactory;

    public XmlBeanParser(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void loadBeans(String configFile) {
        try {
            JAXBContext context = JAXBContext.newInstance(BeansConfig.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            BeansConfig config = (BeansConfig) unmarshaller.unmarshal(new File(configFile));

            for (BeanElement beanElement : config.getBean()) {
                BeanDefinition definition = new BeanDefinition();
                definition.setId(beanElement.getId());
                definition.setClassName(beanElement.getClassName());
                definition.setScope(beanElement.getScope() != null ? beanElement.getScope() : "singleton");

                if (beanElement.getConstructorArg() != null && !beanElement.getConstructorArg().isEmpty()) {
                    definition.setInjectionType(BeanDefinition.InjectionType
                            .CONSTRUCTOR);
                    for (ConstructorArgElement arg : beanElement.getConstructorArg()) {
                        String value = arg.getValue() != null ? arg.getValue() : "#{" + arg.getRef() + "}";
                        definition.getConstructorArgs().put(arg.getIndex(), value);
                    }
                } else if (beanElement.getProperty() != null && !beanElement.getProperty().isEmpty()) {
                    definition.setInjectionType(BeanDefinition.InjectionType.SETTER);
                    for (PropertyElement prop : beanElement.getProperty()) {
                        Object value = prop.getValue() != null ? prop.getValue() : "#{" + prop.getRef() + "}";
                        definition.getProperties().put(prop.getName(), value);
                    }
                }

                beanFactory.registerBeanDefinition(definition.getId(), definition);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing XML", e);
        }
    }
}
