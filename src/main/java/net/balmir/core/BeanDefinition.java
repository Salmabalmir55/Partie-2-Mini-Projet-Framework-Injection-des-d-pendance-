package net.balmir.core;


import java.util.*;

public class BeanDefinition {
    private String id;
    private String className;
    private String scope;
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, String> constructorArgs = new HashMap<>();
    private InjectionType injectionType;

    public enum InjectionType {
        CONSTRUCTOR, SETTER, FIELD
    }

    public BeanDefinition() {
        this.scope = "singleton";
        this.injectionType = InjectionType.SETTER;
    }

    public BeanDefinition(String id, String className) {
        this.id = id;
        this.className = className;
        this.scope = "singleton";
        this.injectionType = InjectionType.SETTER;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    public Map<String, String> getConstructorArgs() { return constructorArgs; }
    public void setConstructorArgs(Map<String, String> constructorArgs) { this.constructorArgs = constructorArgs; }
    public InjectionType getInjectionType() { return injectionType; }
    public void setInjectionType(InjectionType injectionType) { this.injectionType = injectionType; }
}
