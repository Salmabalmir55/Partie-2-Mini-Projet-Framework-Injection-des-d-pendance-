#  Mini Framework IOC

> Framework d'injection de dépendances développé en Java, inspiré de Spring IOC.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue?style=flat-square&logo=apachemaven)
![JAXB](https://img.shields.io/badge/JAXB-4.0-green?style=flat-square)
![JUnit](https://img.shields.io/badge/JUnit-5.10-red?style=flat-square&logo=junit5)

---

##  Table des matières

- [Description](#-description)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Types d'injection](#-types-dinjection)
- [Configuration par annotations](#-configuration-par-annotations)
- [Configuration par XML](#-configuration-par-xml)
- [Gestion des Scopes](#-gestion-des-scopes)
- [Référence des annotations](#-référence-des-annotations)
- [Dépendances Maven](#-dépendances-maven)
- [Tests](#-tests)
- [Workflow GitHub](#-workflow-github)

---

##  Description

Ce framework permet de gérer automatiquement les dépendances entre les composants d'une application Java.  
Il offre **deux modes de configuration** (annotations ou XML) et **trois types d'injection** de dépendances.

Il reproduit les mécanismes fondamentaux d'un conteneur **IoC (Inversion of Control)** :
- Scan et enregistrement automatique des beans
- Résolution et injection des dépendances
- Gestion du cycle de vie (singleton / prototype)

---

##  Fonctionnalités

| Fonctionnalité | Statut |
|---|---|
| Injection par constructeur | 
| Injection par setter | 
| Injection par field (réflexion) | 
| Configuration par annotations | 
| Configuration par XML (JAXB/OXM) | 
| Scope singleton | 
| Scope prototype | 
| Scan automatique des packages | 

---

##  Architecture

```
src/main/java/net/balmir/
├── annotations/
│   ├── Component.java
│   ├── Autowired.java
│   ├── Qualifier.java
│   └── Scope.java
├── core/
│   ├── ApplicationContext.java
│   ├── BeanDefinition.java
│   └── BeanFactory.java
├── injector/
│   ├── ConstructorInjector.java
│   ├── SetterInjector.java
│   └── FieldInjector.java
├── parser/
│   ├── AnnotationBeanParser.java
│   └── XmlBeanParser.java
├── dao/
│   ├── UserDao.java
│   └── UserDaoImpl.java
├── service/
│   ├── UserService.java
│   └── UserServiceImpl.java
└── Main.java
```

| Package | Rôle |
|---|---|
| `annotations/` | Annotations personnalisées : `@Component`, `@Autowired`, `@Qualifier`, `@Scope` |
| `core/` | Noyau du framework : `ApplicationContext`, `BeanFactory`, `BeanDefinition` |
| `injector/` | Stratégies d'injection : Constructeur, Setter, Field |
| `parser/` | Parseurs de configuration : Annotations et XML (JAXB) |
| `dao/` & `service/` | Exemples de composants métier utilisant le framework |

---

##  Prérequis

- **Java** : 17 ou supérieur
- **Maven** : 3.6 ou supérieur
- **IDE** : VS Code, IntelliJ IDEA ou Eclipse

---

##  Installation

```bash
# 1. Cloner le dépôt
git clone https://github.com/<votre-username>/Mini-Framework-IOC.git
cd Mini-Framework-IOC

# 2. Compiler le projet
mvn clean compile

# 3. Exécuter l'application
mvn exec:java -Dexec.mainClass="net.balmir.Main"

# 4. Lancer les tests
mvn test
```

---

##  Types d'injection

###  Injection par Field (Attribut)

Accès direct à l'attribut privé par réflexion Java. La plus concise.

```java
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    @Qualifier("userDao")      // Sélection précise du bean
    private UserDao userDao;   // Accès direct via Field

    @Override
    public String getUser(int id) {
        return userDao.findById(id);
    }
}
```

### 🔹 Injection par Setter

Le conteneur appelle le setter `@Autowired` après instanciation.

```java
@Component
public class UserServiceImpl implements UserService {

    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;   // Injection via setter
    }

    @Override
    public String getUser(int id) {
        return userDao.findById(id);
    }
}
```

### 🔹 Injection par Constructeur

Recommandée : les dépendances sont `final` et garanties non-nulles.

```java
@Component
public class UserServiceImpl implements UserService {

    private final UserDao userDao;   // final = immuable

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;      // Injection via constructeur
    }

    @Override
    public String getUser(int id) {
        return userDao.findById(id);
    }
}
```

---

##  Configuration par Annotations

### Étape 1 — Déclarer un bean

```java
package net.balmir.dao;

import net.balmir.annotations.Component;

@Component("userDao")
public class UserDaoImpl implements UserDao {

    @Override
    public String findById(int id) {
        return "Utilisateur " + id;
    }
}
```

### Étape 2 — Injecter la dépendance

```java
package net.balmir.service;

import net.balmir.annotations.*;

@Component("userService")
@Scope("singleton")
public class UserServiceImpl implements UserService {

    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;

    @Override
    public String getUser(int id) {
        return userDao.findById(id) + " - Service";
    }
}
```

### Étape 3 — Démarrer le contexte

```java
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext(Main.class);
        UserService service = (UserService) context.getBean("userService");

        System.out.println(service.getUser(42));
        // Output: Utilisateur 42 - Service
    }
}
```

---

##  Configuration par XML

### `src/main/resources/application-context.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <!-- Bean simple -->
    <bean id="userDao" class="net.balmir.dao.UserDaoImpl"/>

    <!-- Bean avec injection par référence -->
    <bean id="userService" class="net.balmir.service.UserServiceImpl">
        <property name="userDao" ref="userDao"/>
    </bean>

    <!-- Bean avec valeurs primitives -->
    <bean id="dataSource" class="net.balmir.DataSource">
        <property name="url"      value="jdbc:mysql://localhost:3306/db"/>
        <property name="username" value="root"/>
        <property name="password" value="secret"/>
    </bean>
</beans>
```

### Utilisation

```java
ApplicationContext context = new ApplicationContext("application-context.xml");
UserService service = (UserService) context.getBean("userService");
System.out.println(service.getUser(1));
```

---

##  Gestion des Scopes

| Scope | Comportement | Cas d'usage |
|---|---|---|
| `singleton` | Une seule instance partagée dans tout le contexte | Services, repositories, DAOs |
| `prototype` | Nouvelle instance à chaque appel de `getBean()` | Builders, objets avec état |

```java
@Component("reportGenerator")
@Scope("prototype")   // Nouvelle instance à chaque injection
public class ReportGenerator {
    private final UUID id = UUID.randomUUID();

    public UUID getId() { return id; }
}

// Vérification :
ReportGenerator r1 = (ReportGenerator) ctx.getBean("reportGenerator");
ReportGenerator r2 = (ReportGenerator) ctx.getBean("reportGenerator");
assert !r1.getId().equals(r2.getId()); // true : instances différentes
```

---

##  Référence des annotations

| Annotation | Cible | Description |
|---|---|---|
| `@Component("nom")` | Classe | Déclare la classe comme bean géré par le conteneur |
| `@Autowired` | Field / Setter / Constructeur | Marque un point d'injection de dépendance |
| `@Qualifier("nom")` | Field / Paramètre | Spécifie le bean à injecter (levée d'ambiguïté) |
| `@Scope("singleton"\|"prototype")` | Classe | Définit le scope du bean (défaut : singleton) |

---

##  Dépendances Maven

```xml
<dependencies>
    <!-- JAXB pour le parsing XML (OXM : Object-XML Mapping) -->
    <dependency>
        <groupId>jakarta.xml.bind</groupId>
        <artifactId>jakarta.xml.bind-api</artifactId>
        <version>4.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.sun.xml.bind</groupId>
        <artifactId>jaxb-impl</artifactId>
        <version>4.0.3</version>
    </dependency>

    <!-- Tests unitaires -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

##  Tests

```bash
# Exécuter tous les tests
mvn test

# Exécuter une classe de test spécifique
mvn test -Dtest=BeanFactoryTest

# Exécuter une méthode de test spécifique
mvn test -Dtest=BeanFactoryTest#testSingletonScope

# Rapport de couverture (si JaCoCo configuré)
mvn verify
```

**Couverture des tests :**
- `BeanFactoryTest` — cycle de vie des beans (singleton / prototype)
- `AnnotationParserTest` — scan et détection des composants
- `XmlParserTest` — chargement et parsing de `application-context.xml`
- `InjectorTest` — vérification des 3 types d'injection

---

##  Workflow GitHub

```bash
# Initialiser et pousser le projet
git init
git add .
git commit -m "feat: implémentation initiale du Mini Framework IOC"
git remote add origin https://github.com/<username>/Mini-Framework-IOC.git
git branch -M main
git push -u origin main

# Workflow de développement
git checkout -b feature/xml-parser
# ... modifications ...
git add .
git commit -m "feat: ajout du parser XML avec JAXB"
git push origin feature/xml-parser
# Créer une Pull Request sur GitHub
```

---

*Mini Framework IOC — net.balmir — Java 17 · Maven · JAXB · JUnit 5*