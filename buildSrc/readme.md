# Purpose

The definition of plugins to configure a multimodule gradle project uses the current good practices for Gradle project.

We use the newer central configuratio feature for all plugins and libraries used in the project.

# Convention Plugins

Java Common Conventions::
Configuration and dependencies used in all Java modules
Java Library Conventions::
Configuration and dependencies specific to Java library modules
Java Application Conventions::
Configuration and dependencies specific to Java applications

# Commands

The set of gradle command line special commands we seldom use:

* To publish all modules in local maven repository         `gradle publishMavenJavaPublicationToMavenLocal`
* To publish all module in maven central repository        `gradle publishMavenJavaPublicationToMavenRepository -Pmode=prod`
* To generate static and aggregate code coverage metrics   `gradle build jacocoReport jacocoTestReport -Pmode=int`
* To detect vulnerabilities                                `gradle ossIndexAudit --info -Pmode=int`
* To detect newer versions of libraries                    `gradle dependencyUpdates -Drevision=release`
* To update sonar cloud                                    `gradle sonarqube -Dsonar.login=<login token>`
* To create a production release                           `gradle build -Pvaadin.productionMode`

# Tips

To execute the bash profile commands you shall run `source ~/.bash_profile`.

