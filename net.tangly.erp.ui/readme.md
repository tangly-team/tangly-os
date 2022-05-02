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
requires **Java 17 or higher**.

**Try it out**.

## Download and Documentation

Include the library in your Maven configuration as:

```xml

<dependency>
    <groupId>net.tangly.erp</groupId>
    <artifactId>ui</artifactId>
    <version>0.2.7</version>
</dependency>
```

Include the library in your Gradle configuration as:

```groovy
    implementation "net.tangly.erp:ui:0.2.7"
```

The documentation can be found under [Business documentation](https://tangly-team.bitbucket.io/docs/erp/ui/)

For any further questions and discussion, you can use the forum [tangly-OS-Components](https://groups.google.com/g/tangly-os-components)

## Run as an Application

### Run Locally with Gradle

The gradle script compiles, packages, and runs the application with an embedded web server. _Run from the project root_.

```shell
    gradle run
```

### Run locally as compiled application

You can build and run the generated Java Vaadin application locally. _Run from the project root_.

```shell
  ./gradlew build installDist -Pvaadin.productionMode // <1>
  net.tangly.erp.ui/build/install/net.tangly.erp.ui/bin/net.tangly.erp.ui  // <2>
```

### Run locally as fatjar application

You can build and run the generated Java Vaadin application as fat jar locally. _Run from the project root_.

```shell
  ./gradlew build installShadowDist -Pvaadin.productionMode // <3>
  net.tangly.erp.ui/build/install/net.tangly.erp.ui-shadow/bin/net.tangly.erp.ui // <2>
  java $JVM_OPTS -jar net.tangly.erp.ui/build/install/net.tangly.erp.ui-shadow/lib/net.tangly.erp.ui-all.jar // <3>
```

- <1> The production mode parameter is mandatory.
  Otherwise, the distribution frontend is not built, and the application cannot run without JavaScript build tools.
- <2> Head to http://localhost:8080/erp/. The default port is 8080, and the application starts with in-memory storage.
  The environment variables are set in the run script.
- <3> Start the application with java command. You are responsible to provide the JVM options as define below.

The JVM options to run the application are

```shell
JVM_OPTS="--enable-preview --add-exports=java.base/jdk.internal.misc=ALL_UNNAMED \
          --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED -XX:+ShowCodeDetailsInExceptionMessages -Xmx256m"
```

### Run in a Docker Image

The prerequisite is that the application was built locally for production mode as described above. The first step is to build the Docker image with the provided configuration.
_Run from the project root_.

```shell
  ./gradlew net.tangly.erp.ui:installDist -Pvaadin.productionMode
  docker build -t tanglyllc/tangly-erp:latest net.tangly.erp.ui/
  docker push tanglyllc/tangly-erp:latest
```

We currently push to [Docker Hub](https://hub.docker.com/) repository.
The image is accessible unde [tangly-erp Docker Image](https://hub.docker.com/r/tanglyllc/tangly-erp).

To run the built image use the following commands.

```shell
  docker run --rm -ti -p 8080:8080 -e PORT=8080 -e m=true  -v /var/tangly-erp:/var/tangly-erp tanglyllc/tangly-erp:latest
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

The erp application can be accessed over [tangly-erp](https://tangly-erp.herokuapp.com/erp/).
The git repository is [git](https://git.heroku.com/tangly-erp.git).
Update the heroku remote in git to point to this heroku git repository.

These instructions build the docker image locally and publish it into the heroku application using heroku commands.

```shell
  heroku login
  heroku container:login // <1>
  cd net.tangly.erp.ui
  heroku container:push web // <2>
  heroku container:release web // <3>
```

- <1> login in the heroku docker image registry using docker CLI
- <2> build the locally build docker image to heroku. You can also download an image from a repository and push it to Heroku.
- <3> Release the image to deploy it on Heroku Cloud.

These instructions create and publish the docker image using docker commands.

```shell
  heroku login
  heroku container:login // <1>
  docker build -t tanglyllc/tangly-erp:latest net.tangly.erp.ui/
  docker tag tanglyllc/tangly-erp:latest registry.heroku.com/tangly-erp/web // <2>
  docker push registry.heroku.com/tangly-erp/web // <3>
  heroku container:release web
```

- <1> login in the heroku docker image registry using docker command line interface CLI
- <2> tag the docker image
- <3> push an existing image to the heroku registry

We deliberately decided not to provide the instructions to deploy a fat jar in docker.
Fat jar applications are inherently bloaded executable files.
You should always pursue small executable size when working with images, especially if the images are executed in a public cloud.

## Contribution

You are welcome to contribute to the product with pull requests on Bitbucket. You can download the source files from the
[bitbucket git repository](https://bitbucket.org/tangly-team/tangly-os.git) and build the library with the provided gradle configuration file.

If you find a bug or request a new feature, please use the [issue tracker](https://bitbucket.org/tangly-team/tangly-os/issues).

## License

The source code is licensed under [Apache license 2.0](https://www.apache.org/licenses/LICENSE-2.0).

The documentation and examples are licensed under [Creative Common (CC Attribution 4.0 International)](https://creativecommons.org/licenses/by/4.0/).

## Awesome Sponsors and Developers

Corporate sponsors are

* [tangly llc](https://www.tangly.net)

Individual developers are

* [Marcel Baumann](https://linkedin.com/in/marcelbaumann)
