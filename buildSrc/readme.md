# Purpose

The definition of plugins to configure a multimodule Gradle project uses the current good practices for a Gradle project.

We use the newer central configuration feature for all plugins and libraries used in the project.

# Convention Plugins

Java Common Conventions::
Configuration and dependencies used in all Java modules
Java Library Conventions::
Configuration and dependencies specific to Java library modules
Java Application Conventions::
Configuration and dependencies specific to Java applications

# Commands

The set of Gradle command line special commands we seldom use:

* To publish all modules in local maven repository         `./gradlew publishMavenJavaPublicationToMavenLocal`
* To publish all modules in maven central repository       `./gradlew publishMavenJavaPublicationToMavenRepository -Pmode=prod`
* To generate static and aggregate code coverage metrics   `./gradlew test testCodeCoverageReport testAggregateTestReport`
* To detect vulnerabilities                                `./gradlew ossIndexAudit --info -Pmode=int`
* To detect newer versions of libraries                    `./gradlew dependencyUpdates -Drevision=release`
* To update sonar cloud                                    `./gradlew sonarqube -Dsonar.login=<login token>`
* To create a production release                           `./gradlew net.tangly.erp.ui:installDist -Pvaadin.productionMode`
* To check licenses of dependencies                        `./gradlew checkLicense`

# Tips

To execute the bash profile commands you shall run `source ~/.bash_profile`.

