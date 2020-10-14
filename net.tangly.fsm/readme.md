---
title: "Readme"
date: 2019-05-01
lastmod: 2020-05-01
weight: 10
draft: false
---

# Finite State Machine

![Apache License 2.0](https://img.shields.io/badge/license-Apache%202-blue.svg)
![Maven Central](https://img.shields.io/maven-central/v/net.tangly/fsm.svg)
[![javadoc](https://javadoc.io/badge2/net.tangly/fsm/javadoc.svg)](https://javadoc.io/doc/net.tangly/fsm)
![Bitbucket Pipelines](https://img.shields.io/bitbucket/pipelines/tangly-team/tangly-os.svg)
![Sonar Coverage](https://img.shields.io/sonar/https/sonarcloud.io/tangly-os-at-tangly.net/coverage.svg)
![Bitbucket open issues](https://img.shields.io/bitbucket/issues-raw/tangly/tangly-os.svg)

## Purpose

The **tangly fsm** is a finite state machine library. You can use it in productive projects and academic assignments.
The component is using records and requires **Java 14 or higher**.

The library provides

* Definition of hierarchical state machine descriptions. The machine states and transitions are generic classes. You provide an enumeration for the
  set of states, and an enumeration for the set of events triggering the machine. The builder pattern is used to create complex state machine 
  definition declaratively,
* Builder approach to construct finite state machine declaration - either classical state machines or hierarchical state machines -
* Lambda expressions based on standard API functional interfaces are used for guards and actions
  * Guard are bi-predicate lambda expressions with the context and event as parameters
  * Actions are bi-function lambda expressions with the context and event as parameters 
* A runtime engine processing events on a finite state machine description. Multiple instances of the same description can be instantiated. The class 
  owning the state machine is passed as context to all guards and actions,
* Support classes to implement listeners and logging are provided. 
* Documentation helper can generate a graphical representation of a state machine using the graph dot language. Various output formats are supported
  * Table representation
  * Dot graph representation
  * PlantUML UML finite hierarchical state machine representation

## Download and Documentation

The library can be included in Maven as

```xml
    <dependency>
      <groupId>net.tangly</groupId>
      <artifactId>fsm</artifactId>
      <version>0.2.6</version>
    </dependency>
```    

The library can be included in Gradle as

```groovy
    implementation "net.tangly:fsm:0.2.6"
```
 
The documentation can be found under [FSM documentation](https://tangly-team.bitbucket.io/docs/fsm/)

For any further question and discussion you can use the forum [tangly-OS-Components](https://groups.google.com/d/forum/tangly-os-components)
        
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
