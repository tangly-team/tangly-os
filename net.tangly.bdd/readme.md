---
title: "Readme"
date: 2019-05-01
lastmod: 2020-05-01
weight: 10
draft: false
---

# Behavior Driven Development _BDD_ JUnit 5 Library

![Apache License 2.0](https://img.shields.io/badge/license-Apache%202-blue.svg)
![Maven Central](https://img.shields.io/maven-central/v/net.tangly/bdd.svg)
![Bitbucket Pipelines](https://img.shields.io/bitbucket/pipelines/tangly-team/tangly-os.svg)
![Sonar Coverage](https://img.shields.io/sonar/https/sonarcloud.io/tangly-os-at-tangly.net/coverage.svg)
![Bitbucket open issues](https://img.shields.io/bitbucket/issues-raw/tangly/tangly-os.svg)

## Purpose

A behavior driven development library that provides a custom extension based on JUnit 5 Jupiter Extension Model. This library can be used to create
and run stories and behaviors a.k.a BDD specification tests.

The library is developer first approach. Developers write acceptance tests to validate the functions they have implemented. The generated
report provides a living documentation of the features and allow customers to validate the acceptance criteria.

The library is compact and integrate with JUnit 5 without disrupting TDD approaches and continuous integration pipelines.

## Download and Documentation

You need to add JUnit 5 dependencies before using this library. If you are using Maven or Gradle  add the following dependency after declaring the
 JUnit 5 dependency. We recommend using __assertJ__ assertion library to write more legible conditions.

The library can be included in Maven as

```xml
    <dependency>
      <groupId>net.tangly</groupId>
      <artifactId>bdd</artifactId>
      <version>0.2.4</version>
    </dependency>
```    

The library can be included in Gradle as

```groovy
    testImplementation "net.tangly:bdd:0.2.4"
```

The documentation can be found under [BDD documentation](https://tangly-team.bitbucket.io)

For any further question and discussion you can use the forum [tangly-OS-Components](https://groups.google.com/d/forum/tangly-os-components)

## Contribution

You are welcome to contribute to the project with pull requests on Bitbucket. You can download the source files from the 
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

