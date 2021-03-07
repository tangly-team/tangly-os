---
title: "Readme"
date: 2020-06-01
lastmod: 2020-06-01
weight: 10
draft: false
---

# Business Model Types

![Apache License 2.0](https://img.shields.io/badge/license-Apache%202-blue.svg)
![Maven Central](https://img.shields.io/maven-central/v/net.tangly/ui.svg)
[![javadoc](https://javadoc.io/badge2/net.tangly/ports/javadoc.svg)](https://javadoc.io/doc/net.tangly/ui)
![Bitbucket Pipelines](https://img.shields.io/bitbucket/pipelines/tangly-team/tangly-os.svg)
![Sonar Coverage](https://img.shields.io/sonar/https/sonarcloud.io/tangly-os-at-tangly.net/coverage.svg)
![Bitbucket open issues](https://img.shields.io/bitbucket/issues-raw/tangly/tangly-os.svg)

## Purpose

The **tangly ui erp** component provides a Vaadin based user interfaces for enterprise applications written in Java.
The component is using records and requires **Java 14 or higher**.


Currently, we are moving at the bleeding edge of our components. Therefore to build and execute the ERP application please

```shell
    mvn jetty:run
```

The initial username and password are 'administrator' and 'aeon'.

The file _application.properties_ in _.../main/resources_ directory tailors the configuration of the application, mainly the location of data.
The file contains comments for new users.

The Maven POM file is used to build the application and run it because the Gradle Jetty plugin crashes.

## Download and Documentation

The library can be included in Maven as

```xml
    <dependency>
      <groupId>net.tangly</groupId>
      <artifactId>ui</artifactId>
      <version>0.2.6</version>
    </dependency>
```

The library can be included in Gradle as

```groovy
    implementation "net.tangly:ui:0.2.6"
```

The documentation can be found under [Business documentation](https://tangly-team.bitbucket.io/docs/ui/)

For any further question and discussion you can use the forum [tangly-OS-Components](https://groups.google.com/g/tangly-os-components)

## Contribution

You are welcome to contribute to the product with pull requests on Bitbucket. You can download the source files from the
[bitbucket git repository](https://bitbucket.org/tangly-team/tangly-os.git) and build  the library with the provided gradle configuration file.

If you find a bug or want to request a feature, please use the [issue tracker](https://bitbucket.org/tangly-team/tangly-os/issues).

## License

The source code is licensed under [Apache license 2.0](https://www.apache.org/licenses/LICENSE-2.0).

The documentation and examples are licensed under [Creative Common (CC Attribution 4.0 International)](https://creativecommons.org/licenses/by/4.0/).

## Awesome Sponsors and Developers

Corporate sponsors are

* [tangly llc](https://www.tangly.net)

Individual developers are

* [Marcel Baumann](https://linkedin.com/in/marcelbaumann)

