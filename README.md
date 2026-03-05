# Selenium Automation Framework

A lightweight **Java Selenium automation framework** designed for
building scalable UI test automation with clean abstractions, reusable
components, and flexible reporting.

------------------------------------------------------------------------

# Overview

This framework provides a structured way to build Selenium UI automation
using reusable components and a lightweight runner.

Main capabilities:

-   Selenium WebDriver integration
-   Cross‑browser support
-   Page abstraction using **Container / Element**
-   Suite execution
-   HTML / XML / JSON reporting
-   Screenshot capture
-   Headless browser support
-   CI/CD friendly output formats

------------------------------------------------------------------------

# Architecture

Framework components:

Test Suite\
│\
├── TestRunner\
│\
├── Connector (WebDriver management)\
│\
├── Config (framework configuration)\
│\
├── Container (page abstraction)\
│\
└── Element (UI element wrapper)

This separation keeps tests clean and maintainable.

------------------------------------------------------------------------

# Key Components

## Config

Responsible for framework configuration:

-   driver selection
-   headless execution
-   screenshot location
-   framework properties

Example:

``` java
Config config = new Config().initDefaultProperties()
    .setProperty("driver", "chrome")
    .setProperty("headless", "true");
```

------------------------------------------------------------------------

## Connector

Handles WebDriver lifecycle:

-   driver initialization
-   browser control
-   navigation

Example:

``` java
connector.getDriver().get("https://google.com");
```

------------------------------------------------------------------------

## Container

Represents a **page or page section**.

Encapsulates:

-   element lookup
-   waiting utilities
-   component logic

Example:

``` java
public class LoginPage extends Container {

    Element username = $(By.id("username"));
    Element password = $(By.id("password"));
    Element loginBtn = $(By.id("login"));

    public void login(String user, String pass) {
        username.setValue(user);
        password.setValue(pass);
        loginBtn.click();
    }
}
```

------------------------------------------------------------------------

## Element

Wrapper around Selenium WebElement.

Provides helper methods:

-   click()
-   setValue()
-   waitVisible()
-   waitClickable()

Example:

``` java
Element searchBox = $(By.name("q"));
searchBox.setValue("selenium");
```

------------------------------------------------------------------------

# Running Tests

## Build Project

``` bash
mvn clean install
```

------------------------------------------------------------------------

## Run Tests

``` bash
mvn test
```

------------------------------------------------------------------------

## Run Jar (legacy mode)

``` bash
mvn clean compile assembly:single
java -jar target/sftselenium-jar-with-dependencies.jar
```

------------------------------------------------------------------------

# Example Test

``` java
public class TestUI extends BasedTestUI {

    private final String BASE_URL = "https://www.google.com/";

    public TestUI() {
        super(new Config().initDefaultProperties());
    }

    @Test
    public void TestGoogleImages() throws Exception {

        connector.getDriver().get(BASE_URL + "/imghp");

        waitPageLoad(BASE_URL + ".*");

        body.wait(1);

        config.createSnapshot();

        body.wait(1);
    }
}
```

------------------------------------------------------------------------

# Reporting

The framework supports multiple report formats.

### HTML

Human readable execution report.

Shows:

-   passed tests
-   failed tests
-   execution time
-   screenshots

------------------------------------------------------------------------

### XML

Machine readable format used by CI systems:

-   Jenkins
-   GitHub Actions
-   TeamCity

------------------------------------------------------------------------

### Cucumber JSON

Compatible with Cucumber reporting tools.

------------------------------------------------------------------------

### TM4J JSON

Supports export for **TM4J (Test Management for Jira)**.

Example:

``` java
@DisplayName(value="Verify Login", key="QA-123")
```

------------------------------------------------------------------------

# Screenshots

Snapshots can be captured during test execution.

Example:

``` java
config.createSnapshot();
```

Useful for:

-   debugging
-   CI artifacts
-   test reports

------------------------------------------------------------------------

# Project Structure

    selenium
    │
    ├── src/com/softigent/sftselenium
    │
    │   ├── Config.java
    │   ├── Connector.java
    │   ├── Container.java
    │   ├── Element.java
    │   ├── TestRunner.java
    │   ├── TestSuiteRunner.java
    │   └── Reports
    │
    ├── v2/
    │   ├── pom.xml
    │   └── tests
    │
    └── README.md

------------------------------------------------------------------------

# Dependencies

Main libraries used:

-   Selenium 4
-   WebDriverManager
-   JUnit / TestNG
-   Apache POI
-   CSV utilities
-   PDFBox
-   Gson

These allow building tests with:

-   data‑driven inputs
-   file validation
-   API integration
-   document validation

------------------------------------------------------------------------

# CI/CD Integration

Example GitHub Actions pipeline:

``` yaml
name: Selenium Tests

on: [push]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: 17

      - name: Run Tests
        run: mvn test
```

------------------------------------------------------------------------

# Best Practices

Recommended usage:

-   Use **Page Objects via Container**
-   Keep selectors inside page classes
-   Keep assertions in test classes
-   Avoid raw WebDriver calls in tests

------------------------------------------------------------------------

# Contributing

Pull requests are welcome.

Suggested improvements:

-   additional report types
-   better CI integration
-   new browser support
-   improved wait utilities

------------------------------------------------------------------------

# License

MIT License
