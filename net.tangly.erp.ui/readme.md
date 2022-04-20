---
title: "Readme"
date: 2019-05-01 weight: 10 draft: false
---

# ERP User Interface Component

![Apache License 2.0](https://img.shields.io/badge/license-Apache%202-blue.svg)
![Maven Central](https://img.shields.io/maven-central/v/net.tangly/erp/ui.svg)
[![javadoc](https://javadoc.io/badge2/net.tangly/bus/javadoc.svg)](https://javadoc.io/doc/net.tangly.erp/ui)
![Bitbucket Pipelines](https://img.shields.io/bitbucket/pipelines/tangly-team/tangly-os.svg)
![Sonar Coverage](https://img.shields.io/sonar/https/sonarcloud.io/tangly-os-at-tangly.net/coverage.svg)
![Bitbucket open issues](https://img.shields.io/bitbucket/issues-raw/tangly/tangly-os.svg)

## Purpose

The **tangly erp ui** bounded domain component provides regular business model abstractions for business applications written in Java. The component is using records and
requires **Java 16 or higher**.

**Try it out**.

## Download and Documentation

The library can be included in Maven as

```xml

<dependency>
    <groupId>net.tangly.erp</groupId>
    <artifactId>ui</artifactId>
    <version>0.2.7</version>
</dependency>
```

The library can be included in Gradle as

```groovy
    implementation "net.tangly.erp:ui:0.2.7"
```

The documentation can be found under [Business documentation](https://tangly-team.bitbucket.io/docs/erp/ui/)

For any further questions and discussion, you can use the forum [tangly-OS-Components](https://groups.google.com/g/tangly-os-components)

## Run as an Application

### Run Locally

The gradle script compiles, packages, and runs the application with an embedded web server.

```shell
    gradle run
```

You can run the generated application locally.

```shell
  gradle build installDist -Pvaadin.productionMode // <1>
  net.tangly.erp.ui/build/install/net.tangly.erp.ui/bin/net.tangly.erp.ui // <2>
```
<1> The production mode parameter is mandatory.
Otherwise, the distribution frontend is not built and the application cannot run without JavaScript build tools.

<2> Head to http://localhost:8080/erp/. The default port is 8080 and application starts with persistent storage.

### Run in a Docker Image

The prerequisite is that the application was build locally for production mode as described above. The first step is to build the Docker image with the provided configuration.

```shell
  ./gradlew net.tangly.erp.ui:installDist -Pvaadin.productionMode
  docker build -t tanglyllc/tangly-erp:latest net.tangly.erp.ui/
  docker push tanglyllc/tangly-erp:latest
```

We currently push to [Docker Hub](https://hub.docker.com/).

To run the built image use the following commands.

```shell
  docker run --rm -ti -p 8080:8080 -e PORT=8080  --mount type=bind,source=/var/tangly-erp,target=/var/tangly-erp tanglyllc/tangly-erp:latest
```

The user under which the erp application shall not have root privileges.

The port of the application is configured through the port environment variable.
This approach is mandatory if the image is deployed in [Heroku](https://www.heroku.com/).

### Run in the Cloud

#### Heroku Cloud

An application must once be created to host the docker image.

```shell
   heroku create tangly-erp --region eu
```

The erp application can be access over [tangly-erp](https://tangly-erp.herokuapp.com/erp/).
The git repository is [git](https://git.heroku.com/tangly-erp.git).
Update the heroku remote in git to point to this heroku git repository.

The following instructions build the docker image locally and publish it onto the heroku application using heroku commands.

```shell
  heroku login
  heroku container:login // <1>
  cd net.tangly.erp.ui
  heroku container:push web // <2>
  heroku container:release web // <3>
```
<1> login in the heroku docker image registry using docker CLI
<2> build the docker image locally and push it to heroku
<3> Release the image

The following instructions the docker image and publish using docker commands.
```shell
  heroku login
  heroku container:login // <1>
  docker build -t tanglyllc/tangly-erp:latest net.tangly.erp.ui/
  docker tag tanglyllc/tangly-erp:latest registry.heroku.com/tangly-erp/web // <2>
  docker push registry.heroku.com/tangly-erp/web // <3>
  heroku container:release web
```
<1> login in the heroku docker image registry using docker CLI
<2> tag the docker image
<3> push an existing image to the heroku registry


## Contribution

You are welcome to contribute to the product with pull requests on Bitbucket. You can download the source files from the
[bitbucket git repository](https://bitbucket.org/tangly-team/tangly-os.git) and build the library with the provided gradle configuration file.

If you find a bug or want to request a feature, please use the [issue tracker](https://bitbucket.org/tangly-team/tangly-os/issues).

## License

The source code is licensed under [Apache license 2.0](https://www.apache.org/licenses/LICENSE-2.0).

The documentation and examples are licensed under [Creative Common (CC Attribution 4.0 International)](https://creativecommons.org/licenses/by/4.0/).

## Awesome Sponsors and Developers

Corporate sponsors are

* [tangly llc](https://www.tangly.net)

Individual developers are

* [Marcel Baumann](https://linkedin.com/in/marcelbaumann)

